package net.chesstango.uci.proxy;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.goyeneche.UCIService;
import net.chesstango.goyeneche.stream.UCIActiveStreamReader;
import net.chesstango.goyeneche.stream.UCIInputStreamFromStringAdapter;
import net.chesstango.goyeneche.stream.UCIOutputStreamToStringAdapter;
import net.chesstango.goyeneche.stream.strings.StringConsumer;
import net.chesstango.goyeneche.stream.strings.StringSupplier;

import java.io.*;
import java.nio.file.Path;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class UciMain implements Runnable {
    private final UCIService service;
    private final InputStream in;

    private final PrintStream out;

    private final UCIActiveStreamReader pipe;
    private volatile boolean isRunning;

    public static void main(String[] args) {
        Path configFile = Path.of(args[0]);
        UciMain uciMain = null;
        try {
            ProxyConfig config = ProxyConfigReader.readConfig(configFile);
            uciMain = new UciMain(new UciProxy(config), System.in, System.out);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        uciMain.run();
    }

    public UciMain(UCIService service, InputStream in, PrintStream out) {
        this.service = service;
        this.in = in;
        this.out = out;
        this.pipe = new UCIActiveStreamReader();
        this.service.setOutputStream(new UCIOutputStreamToStringAdapter(new StringConsumer(new OutputStreamWriter(out))));
        this.pipe.setInputStream(new UCIInputStreamFromStringAdapter(new StringSupplier(new InputStreamReader(in))));
        this.pipe.setOutputStream(service::accept);
    }

    @Override
    public void run() {
        try {
            service.open();

            isRunning = true;

            pipe.run();

            isRunning = false;

            service.close();
        } catch (RuntimeException e) {
            log.error("Error:", e);
            throw e;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                log.error("Error:", e);
            }
            out.close();
        }
    }

    public boolean isRunning() {
        return isRunning;
    }
}
