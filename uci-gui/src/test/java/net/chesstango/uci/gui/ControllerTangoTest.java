package net.chesstango.uci.gui;


import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.uci.engine.UciTango;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;


/**
 * @author Mauricio Coria
 */
public class ControllerTangoTest {

    @Test
    public void test_Tango() throws Exception {
        UciTango uciTango = new UciTango();

        try (ControllerAbstract client = new ControllerTango(uciTango)) {

            client.send_ReqUci();

            assertEquals("Mauricio Coria", client.getEngineAuthor());

            assertTrue(client.getEngineName().startsWith("Tango"));

            client.send_ReqIsReady();

            client.send_ReqUciNewGame();

            client.send_ReqPosition(UCIRequest.position(Collections.emptyList()));

            RspBestMove bestMove = client.send_ReqGo(UCIRequest.goDepth(1));

            assertNotNull(bestMove);

            client.send_ReqQuit();
        }
    }


}
