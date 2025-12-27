package net.chesstango.arena.core;

import net.chesstango.arena.core.matchtypes.MatchByDepth;
import net.chesstango.board.Game;
import net.chesstango.engine.Config;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.fen.FENParser;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.search.dummy.Dummy;
import net.chesstango.uci.engine.UciTango;
import net.chesstango.uci.gui.Controller;
import net.chesstango.uci.gui.ControllerTango;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author Mauricio Coria
 */
public class MatchIntegrationTest {

    public static final String SMART = "Smart";
    public static final String DUMMY = "Dummy";

    private Controller smartEngine;

    private Controller dummyEngine;

    @BeforeEach
    public void setup() {
        smartEngine = new ControllerTango(new UciTango())
                .overrideEngineName(SMART);


        dummyEngine = new ControllerTango(
                new UciTango(new Config()
                        .setSyncSearch(true)
                        .setSearch(new Dummy())
                )
        ).overrideEngineName(DUMMY);

        smartEngine.startEngine();
        dummyEngine.startEngine();
    }

    @AfterEach
    public void tearDown() {
        smartEngine.send_ReqQuit();
        dummyEngine.send_ReqQuit();
    }

    @Test
    public void testPlay() {
        Match match = new Match(smartEngine, dummyEngine, FEN.of(FENParser.INITIAL_FEN), new MatchByDepth(3));
        //match.setPrintPGN(true);

        MatchResult matchResult = match.play();

        assertNotNull(matchResult);

        PGN pgn = matchResult.pgn();

        // Deberia ganar el engine smartEngine
        assertEquals(SMART, pgn.getWhite());
        assertEquals(DUMMY, pgn.getBlack());
        assertEquals(PGN.Result.WHITE_WINS, pgn.getResult());
    }
}
