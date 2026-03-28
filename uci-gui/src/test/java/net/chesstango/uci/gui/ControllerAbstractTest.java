package net.chesstango.uci.gui;

import net.chesstango.goyeneche.UCIService;
import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.goyeneche.responses.UCIResponse;
import net.chesstango.goyeneche.stream.UCIOutputStreamGuiExecutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

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

    @Mock
    UCIService service;

    ControllerAbstract createController() {
        ControllerAbstract controller = new ControllerAbstract(service) {
            @Override
            public void accept(ControllerVisitor controllerVisitor) {
                throw new UnsupportedOperationException("Not supported yet.");
            }
        };
        controller.setEngineName("TestEngine");

        return controller;
    }

    @Test
    void test_NoResponseException() throws Exception {
        try (ControllerAbstract controller = createController()) {

            NoResponseException controllerException = assertThrows(NoResponseException.class, controller::send_ReqUci, "No exception thrown");

            assertEquals("Engine TestEngine has not provided any response after sending: uci", controllerException.getMessage());

        }

        verify(service).accept(UCIRequest.uci());
        verify(service).close();
    }

    @Test
    void test_ThinkingTooMuch() throws Exception {
        AtomicReference<UCIOutputStreamGuiExecutor> outputStream = new AtomicReference<>();

        doAnswer(invocationOnMock-> {
            outputStream.set((UCIOutputStreamGuiExecutor) invocationOnMock.getArgument(0));
            return null;
        }).when(service).setUCIOutputStream(any(UCIOutputStreamGuiExecutor.class));

        doAnswer(_ -> {
            CompletableFuture.runAsync(() -> {
                try {
                    Thread.sleep(2000);
                    outputStream.get().accept(UCIResponse.bestMove("a2a3"));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            return null;
        }).when(service).accept(UCIRequest.goFast(1000, 0, 3000, 0));

        RspBestMove response = null;

        try (ControllerAbstract controller = createController()) {
            response = controller.send_ReqGo(UCIRequest.goFast(1000, 0, 3000, 0));
        }

        assertEquals("a2a3", response.getBestMove());

        verify(service).setUCIOutputStream(any(UCIOutputStreamGuiExecutor.class));
        verify(service).accept(UCIRequest.goFast(1000, 0, 3000, 0));
        verify(service).accept(UCIRequest.stop());
        verify(service).close();
    }
}
