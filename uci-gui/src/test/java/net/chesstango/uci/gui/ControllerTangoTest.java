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
    public void test_Tango() {
        UciTango service = new UciTango();

        ControllerAbstract client = new ControllerTango(service);

        client.open();

        client.send_ReqUci();

        assertEquals("Mauricio Coria", client.getEngineAuthor());
        assertTrue(client.getEngineName().startsWith("Tango"));

        client.send_ReqIsReady();

        client.send_ReqUciNewGame();

        client.send_ReqPosition(UCIRequest.position(Collections.emptyList()));

        RspBestMove bestmove = client.send_ReqGo(UCIRequest.goDepth(1));

        assertNotNull(bestmove);

        client.send_ReqQuit();

        client.close();
    }


}
