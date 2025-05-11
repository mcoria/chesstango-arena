package net.chesstango.uci.gui;


import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.uci.proxy.ProxyConfigLoader;
import net.chesstango.uci.proxy.UciProxy;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


/**
 * @author Mauricio Coria
 */
public class ControllerProxyIntegrationTest {

    @Test
    public void test_Proxy() {
        UciProxy engine = new UciProxy(ProxyConfigLoader.loadEngineConfig("Spike"));

        ControllerAbstract client = new ControllerProxy(engine);

        client.send_ReqUci();

        //assertEquals("Ralf Schäfer und Volker Böhm", client.getEngineAuthor());
        assertEquals("Spike 1.4", client.getEngineName());

        client.send_ReqIsReady();

        client.send_ReqUciNewGame();

        client.send_ReqPosition(UCIRequest.position(Collections.emptyList()));

        RspBestMove bestmove = client.send_ReqGo(UCIRequest.goDepth(1));

        assertNotNull(bestmove);

        client.send_ReqQuit();
    }


}
