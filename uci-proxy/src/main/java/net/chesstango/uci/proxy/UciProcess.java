package net.chesstango.uci.proxy;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import static java.lang.ProcessBuilder.Redirect.INHERIT;

/**
 * @author Mauricio Coria
 */
@Slf4j
class UciProcess {
    private final ProcessBuilder processBuilder;
    private Process process;

    InputStream inputStreamProcess;
    PrintStream outputStreamProcess;

    UciProcess(ProxyConfig config) {
        this.processBuilder = config.processBuilder();
    }

    synchronized void startProcess() {
        try {
            processBuilder.redirectError(INHERIT);
            process = processBuilder.start();
            inputStreamProcess = process.getInputStream();
            outputStreamProcess = new PrintStream(process.getOutputStream(), true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Espera que termine normalmente (ejemplo: recibe QUIT)
     * Si no muere envia SIGTERM
     * Si no muere envia SIGKILL
     */
    void stopProcess() {
        log.debug("stopProcess() invoked");

        boolean finished = false;
        try {
            finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                log.debug("stopProcess() sending SIGTERM");
                process.destroy();
                finished = process.waitFor(5, TimeUnit.SECONDS);
                if (!finished) {
                    log.debug("stopProcess() sending SIGKILL");
                }
            } else {
                log.debug("stopProcess(): process exited normally");
            }
        } catch (InterruptedException e) {
            log.error("stopProcess() interrupted", e);
        }
        log.debug("stopProcess() finished");
    }

    void waitProcessStart() {
        int counter = 0;
        try {
            do {
                counter++;
                synchronized (this) {
                    this.wait(100);
                }
            } while (outputStreamProcess == null && counter < 10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (outputStreamProcess == null) {
            throw new RuntimeException("Process has not started yet");
        }
    }
}
