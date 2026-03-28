package net.chesstango.uci.proxy;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
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
public class UciMain implements Runnable, AutoCloseable {

    private final UciProxy uciProxy;

    private final UCIActiveStreamReader pipe;

    @Getter
    private volatile boolean isRunning;

    public static void main(String[] args) throws IOException {
        Path configFile = Path.of(args[0]);
        ProxyConfig config = ProxyConfigReader.readConfig(configFile);
        try (UciMain uciMain = new UciMain(config, System.in, System.out)) {
            uciMain.run();
        }
    }

    public UciMain(ProxyConfig config, InputStream in, PrintStream out) {
        this.uciProxy = new UciProxy(config);
        this.uciProxy.setUCIOutputStream(new UCIOutputStreamToStringAdapter(new StringConsumer(new OutputStreamWriter(out))));
        this.pipe = new UCIActiveStreamReader();
        this.pipe.setOutputStream(uciProxy::accept);
        this.pipe.setInputStream(new UCIInputStreamFromStringAdapter(new StringSupplier(new InputStreamReader(in))));
    }

    @Override
    public void run() {
        try {
            isRunning = true;

            pipe.run();

        } catch (RuntimeException e) {
            log.error("Error:", e);
            throw e;
        } finally {
            isRunning = false;
        }
    }

    @Override
    public void close() {
        uciProxy.close();
    }
}
