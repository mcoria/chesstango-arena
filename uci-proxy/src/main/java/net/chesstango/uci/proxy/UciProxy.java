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
import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class UciProxy implements UCIService {

    private final UCIActiveStreamReader pipe;
    private final UciProcess uciProcess;
    private final String proxyName;

    private UCIOutputStream responseOutputStream;
    private Thread readingPipeThread;


    /**
     * Para que Spike pueda leer sus settings, el working directory debe ser el del ejecutable.
     * Los settings generales para todos los engines se controlan desde EngineManagement -> UCI en Arena.
     */
    public UciProxy(ProxyConfig config) {
        this.pipe = new UCIActiveStreamReader();
        this.uciProcess = new UciProcess(config);
        this.proxyName = config.getName();
    }


    @Override
    public void accept(UCICommand message) {
        if (uciProcess.outputStreamProcess == null) {
            uciProcess.waitProcessStart();
        }

        log.trace("{} >> {}", proxyName, message);

        uciProcess.outputStreamProcess.println(message);
    }

    @Override
    public void open() {
        uciProcess.startProcess();

        Supplier<String> stringSupplier = new StringSupplier(new InputStreamReader(uciProcess.inputStreamProcess));

        stringSupplier = new StringActionSupplier(stringSupplier, line -> log.trace("{} << {}", proxyName, line));

        pipe.setInputStream(new UCIInputStreamFromStringAdapter(stringSupplier));
        pipe.setOutputStream(responseOutputStream);

        readingPipeThread = new Thread(this::readFromProcess);
        readingPipeThread.start();
    }

    @Override
    public void close() {
        pipe.stopReading();

        uciProcess.closeProcessIO();

        try {
            readingPipeThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        uciProcess.stopProcess();
    }

    @Override
    public void setOutputStream(UCIOutputStream output) {
        this.responseOutputStream = output;
    }

    private void readFromProcess() {
        log.debug("readFromPipe(): start reading engine output");
        pipe.run();
        log.debug("readFromPipe():end reading engine output");
    }
}
