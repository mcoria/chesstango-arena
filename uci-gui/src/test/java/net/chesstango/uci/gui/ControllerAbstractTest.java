package net.chesstango.uci.gui;

import net.chesstango.goyeneche.UCIService;
import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.goyeneche.responses.UCIResponse;
import net.chesstango.goyeneche.stream.UCIOutputStreamGuiExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class ControllerAbstractTest {

    ControllerAbstract controller;

    @Mock
    UCIService service;

    @BeforeEach
    void setUp() {
        controller = new ControllerAbstract(service) {
            @Override
            public void accept(ControllerVisitor controllerVisitor) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        controller.setEngineName("TestEngine");
    }

    @Test
    void test_NoResponseException() {
        controller.timeOut = 1000;
        try {
            controller.open();

            NoResponseException controllerException = assertThrows(NoResponseException.class, () -> controller.send_ReqUci(), "No exception thrown");

            assertEquals("Engine TestEngine has not provided any response after sending: uci", controllerException.getMessage());

        } finally {
            controller.close();
        }

        verify(service).open();
        verify(service).accept(UCIRequest.uci());
        verify(service).close();
    }

    @Test
    void test_ThinkingTooMuch() {
        controller.timeOut = 1000;

        doAnswer(_ -> {
            CompletableFuture.runAsync(() -> {
                try {
                    for (int i = 0; i < 10; i++) {
                        Thread.sleep(500);
                        controller.messageExecutor.do_info(UCIResponse.info("Hello, world!"));
                    }
                    controller.messageExecutor.do_bestMove(UCIResponse.bestMove("a2a3"));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            return null;
        }).when(service).accept(UCIRequest.goFast(1000, 0, 1000, 0));

        RspBestMove response = null;
        try {
            controller.open();

            response = controller.send_ReqGo(UCIRequest.goFast(1000, 0, 1000, 0));

        } finally {
            controller.close();
        }

        assertEquals("a2a3", response.getBestMove());

        verify(service).open();
        verify(service).accept(UCIRequest.goFast(1000, 0, 1000, 0));
        verify(service).setOutputStream(any(UCIOutputStreamGuiExecutor.class));
        verify(service).close();
    }
}
