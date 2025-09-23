package net.chesstango.uci.proxy;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

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

    void startProcess() {
        try {
            synchronized (this) {
                processBuilder.redirectError(INHERIT);
                process = processBuilder.start();
                inputStreamProcess = process.getInputStream();
                outputStreamProcess = new PrintStream(process.getOutputStream(), true);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void stopProcess() {
        log.debug("stopProcess() invoked");

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
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

    void closeProcessIO() {
        try {
            outputStreamProcess.close();
            inputStreamProcess.close();
        } catch (IOException e) {
            log.error("Error:", e);
        }
    }
}
