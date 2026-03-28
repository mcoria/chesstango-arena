package net.chesstango.uci.proxy;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.goyeneche.UCICommand;
import net.chesstango.goyeneche.UCIService;
import net.chesstango.goyeneche.stream.UCIActiveStreamReader;
import net.chesstango.goyeneche.stream.UCIInputStreamFromStringAdapter;
import net.chesstango.goyeneche.stream.UCIOutputStream;
import net.chesstango.goyeneche.stream.strings.StringActionSupplier;
import net.chesstango.goyeneche.stream.strings.StringSupplier;

import java.io.InputStreamReader;
import java.util.concurrent.CountDownLatch;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class UciProxy implements UCIService {

    private final UCIActiveStreamReader pipe;
    private final UciProcess uciProcess;
    private final String proxyName;
    private final UCIOutputStream responseOutputStream;
    private final CountDownLatch latch = new CountDownLatch(1);

    private Thread readingPipeThread;


    /**
     * Para que Spike pueda leer sus settings, el working directory debe ser el del ejecutable.
     * Los settings generales para todos los engines se controlan desde EngineManagement -> UCI en Arena.
     */
    public UciProxy(ProxyConfig config, UCIOutputStream output) {
        this.pipe = new UCIActiveStreamReader();
        this.uciProcess = new UciProcess(config);
        this.proxyName = config.getName();
        this.responseOutputStream = output;
        startReadingProcess();
    }


    @Override
    public void accept(UCICommand message) {
        if (uciProcess.outputStreamProcess == null) {
            uciProcess.waitProcessStart();
        }

        log.trace("{} >> {}", proxyName, message);

        uciProcess.outputStreamProcess.println(message);
    }

    private void startReadingProcess() {
        uciProcess.startProcess();

        StringSupplier stringSupplier = new StringSupplier(new InputStreamReader(uciProcess.inputStreamProcess));

        StringActionSupplier stringActionSupplierSupplier = new StringActionSupplier(stringSupplier, line -> log.trace("{} << {}", proxyName, line));

        pipe.setInputStream(new UCIInputStreamFromStringAdapter(stringActionSupplierSupplier));

        pipe.setOutputStream(responseOutputStream);

        readingPipeThread = new Thread(this::readFromProcess);

        readingPipeThread.start();
    }

    private void readFromProcess() {
        latch.countDown();
        log.debug("{} Start reading engine output", proxyName);
        pipe.run();
        log.debug("{} Stop reading engine output", proxyName);
    }

    @Override
    public void close() {
        uciProcess.stopProcess();
        try {
            latch.await();
            readingPipeThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
