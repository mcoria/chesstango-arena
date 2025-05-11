package net.chesstango.uci.arena;

import net.chesstango.board.Game;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.fen.FENParser;
import net.chesstango.engine.Tango;
import net.chesstango.search.dummy.Dummy;
import net.chesstango.uci.arena.matchtypes.MatchByDepth;
import net.chesstango.uci.engine.UciTango;
import net.chesstango.uci.gui.Controller;
import net.chesstango.uci.gui.ControllerTango;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Mauricio Coria
 */
public class MatchTest {

    private Controller smartEngine;

    private Controller dummyEngine;

    @BeforeEach
    public void setup() {
        smartEngine = new ControllerTango(new UciTango())
                .overrideEngineName("Smart");

        dummyEngine = new ControllerTango(new UciTango(new Tango(new Dummy())))
                .overrideEngineName("Dummy");

        smartEngine.startEngine();
        dummyEngine.startEngine();
    }

    @AfterEach
    public void tearDown() {
        smartEngine.send_ReqQuit();
        dummyEngine.send_ReqQuit();
    }

    @Test
    public void testCompete() {
        Match match = new Match(smartEngine, dummyEngine, new MatchByDepth(3));

        match.setFen(FEN.of(FENParser.INITIAL_FEN));

        match.compete();

        MatchResult result = match.createResult();

        // Deberia ganar el engine smartEngine
        assertEquals(smartEngine, result.getEngineWhite());
        assertEquals(smartEngine, result.getWinner());
    }

    @Test
    public void testPlay() {
        Match match = new Match(smartEngine, dummyEngine, new MatchByDepth(3));
        //match.setDebugEnabled(true);

        MatchResult matchResult = match.play(FEN.of(FENParser.INITIAL_FEN));

        assertNotNull(matchResult);

        // Deberia ganar el engine smartEngine
        assertEquals(smartEngine, matchResult.getEngineWhite());
    }

    @Test
    public void testCreateResult01() {
        Match match = new Match(smartEngine, dummyEngine, new MatchByDepth(1));

        match.setFen(FEN.of("8/P7/5Q1k/3p3p/3P2P1/1P1BP3/5P2/3K4 b - - 5 48"));
        match.setGame(Game.fromFEN("8/P7/5Q1k/3p3p/3P2P1/1P1BP3/5P2/3K4 b - - 5 48"));

        MatchResult result = match.createResult();

        assertEquals(smartEngine, result.getEngineWhite());
        assertEquals(dummyEngine, result.getEngineBlack());
        assertEquals(smartEngine, result.getWinner());
    }

    @Test
    public void testCreateResult02() {
        Match match = new Match(smartEngine, dummyEngine, new MatchByDepth(1));

        match.setFen(FEN.of("3k4/5p2/1p1bp3/3p2p1/3P3P/5q1K/p7/8 w - - 0 48"));
        match.setGame(Game.fromFEN("3k4/5p2/1p1bp3/3p2p1/3P3P/5q1K/p7/8 w - - 0 48"));

        MatchResult result = match.createResult();

        assertEquals(smartEngine, result.getEngineWhite());
        assertEquals(dummyEngine, result.getEngineBlack());
        assertEquals(dummyEngine, result.getWinner());
    }


    @Test
    public void testCreateResultDraw01() {
        Match match = new Match(smartEngine, dummyEngine, new MatchByDepth(1));

        match.setFen(FEN.of("6Q1/P7/7k/3p3p/3P3P/1P1BP3/5P2/3K4 b - - 5 48"));
        match.setGame(Game.fromFEN("6Q1/P7/7k/3p3p/3P3P/1P1BP3/5P2/3K4 b - - 5 48"));

        MatchResult result = match.createResult();

        assertEquals(smartEngine, result.getEngineWhite());
        assertEquals(dummyEngine, result.getEngineBlack());
        assertNull(result.getWinner());
    }

    @Test
    public void testCreateResultDraw02() {
        Match match = new Match(smartEngine, dummyEngine, new MatchByDepth(1));

        match.setFen(FEN.of("3k4/5p2/1p1bp3/3p3p/3P3P/7K/p7/6q1 w - - 5 48"));
        match.setGame(Game.fromFEN("3k4/5p2/1p1bp3/3p3p/3P3P/7K/p7/6q1 w - - 5 48"));

        MatchResult result = match.createResult();

        assertEquals(smartEngine, result.getEngineWhite());
        assertEquals(dummyEngine, result.getEngineBlack());
        assertNull(result.getWinner());
    }

}
