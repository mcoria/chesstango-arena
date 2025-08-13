package net.chesstango.uci.arena;

import net.chesstango.board.Game;
import net.chesstango.engine.Tango;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.fen.FENParser;
import net.chesstango.gardel.pgn.PGN;
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

    public static final String SMART = "Smart";
    public static final String DUMMY = "Dummy";

    private Controller smartEngine;

    private Controller dummyEngine;

    @BeforeEach
    public void setup() {
        smartEngine = new ControllerTango(new UciTango())
                .overrideEngineName(SMART);

        dummyEngine = new ControllerTango(new UciTango(config -> {
            config.setSearch(new Dummy());
            return Tango.open(config);
        }))
                .overrideEngineName(DUMMY);

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

    @Test
    public void testCreateResult01() {
        Match match = new Match(smartEngine, dummyEngine, FEN.of("8/P7/5Q1k/3p3p/3P2P1/1P1BP3/5P2/3K4 b - - 5 48"), new MatchByDepth(1));

        match.setGame(Game.from(FEN.of("8/P7/5Q1k/3p3p/3P2P1/1P1BP3/5P2/3K4 b - - 5 48")));

        MatchResult result = match.createResult();

        PGN pgn = result.pgn();

        // Deberia ganar el engine smartEngine
        assertEquals(SMART, pgn.getWhite());
        assertEquals(DUMMY, pgn.getBlack());
        assertEquals(PGN.Result.WHITE_WINS, pgn.getResult());
    }

    @Test
    public void testCreateResult02() {
        Match match = new Match(smartEngine, dummyEngine, FEN.of("3k4/5p2/1p1bp3/3p2p1/3P3P/5q1K/p7/8 w - - 0 48"), new MatchByDepth(1));

        match.setGame(Game.from(FEN.of("3k4/5p2/1p1bp3/3p2p1/3P3P/5q1K/p7/8 w - - 0 48")));

        MatchResult result = match.createResult();

        PGN pgn = result.pgn();

        // Deberia ganar el engine smartEngine
        assertEquals(SMART, pgn.getWhite());
        assertEquals(DUMMY, pgn.getBlack());
        assertEquals(PGN.Result.BLACK_WINS, pgn.getResult());
    }


    @Test
    public void testCreateResultDraw01() {
        Match match = new Match(smartEngine, dummyEngine, FEN.of("6Q1/P7/7k/3p3p/3P3P/1P1BP3/5P2/3K4 b - - 5 48"), new MatchByDepth(1));

        match.setGame(Game.from(FEN.of("6Q1/P7/7k/3p3p/3P3P/1P1BP3/5P2/3K4 b - - 5 48")));

        MatchResult result = match.createResult();

        PGN pgn = result.pgn();

        // Deberia ganar el engine smartEngine
        assertEquals(SMART, pgn.getWhite());
        assertEquals(DUMMY, pgn.getBlack());
        assertEquals(PGN.Result.DRAW, pgn.getResult());
    }

    @Test
    public void testCreateResultDraw02() {
        Match match = new Match(smartEngine, dummyEngine, FEN.of("3k4/5p2/1p1bp3/3p3p/3P3P/7K/p7/6q1 w - - 5 48"), new MatchByDepth(1));
        
        match.setGame(Game.from(FEN.of("3k4/5p2/1p1bp3/3p3p/3P3P/7K/p7/6q1 w - - 5 48")));

        MatchResult result = match.createResult();

        PGN pgn = result.pgn();

        // Deberia ganar el engine smartEngine
        assertEquals(SMART, pgn.getWhite());
        assertEquals(DUMMY, pgn.getBlack());
        assertEquals(PGN.Result.DRAW, pgn.getResult());
    }

}
