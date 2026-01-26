package net.chesstango.arena.core;

import net.chesstango.arena.core.matchtypes.MatchByDepth;
import net.chesstango.arena.core.matchtypes.MatchTimeOut;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.uci.gui.Controller;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

/**
 * @author Mauricio Coria
 */
@ExtendWith(MockitoExtension.class)
public class MatchTest {

    public static final String SMART = "Smart";
    public static final String DUMMY = "Dummy";

    @Mock
    private Controller smartEngine;

    @Mock
    private Controller dummyEngine;

    @BeforeEach
    public void setup() {
        when(smartEngine.getEngineName()).thenReturn(SMART);
        when(dummyEngine.getEngineName()).thenReturn(DUMMY);
    }

    @Test
    public void testCreateResultWhiteWins01() {
        Match match = new Match(smartEngine, dummyEngine, new MatchByDepth(1), PGN.from(FEN.of("8/P7/5Q1k/3p3p/3P2P1/1P1BP3/5P2/3K4 b - - 5 48")));

        MatchResult result = match.createResult();

        PGN pgn = result.pgn();

        // Deberia ganar el engine smartEngine
        assertEquals(SMART, pgn.getWhite());
        assertEquals(DUMMY, pgn.getBlack());
        assertEquals(PGN.Result.WHITE_WINS, pgn.getResult());
    }

    @Test
    public void testCreateBlackWinsResult01() {
        Match match = new Match(smartEngine, dummyEngine, new MatchByDepth(1), PGN.from(FEN.of("3k4/5p2/1p1bp3/3p2p1/3P3P/5q1K/p7/8 w - - 0 48")));

        MatchResult result = match.createResult();

        PGN pgn = result.pgn();

        // Deberia ganar el engine smartEngine
        assertEquals(SMART, pgn.getWhite());
        assertEquals(DUMMY, pgn.getBlack());
        assertEquals(PGN.Result.BLACK_WINS, pgn.getResult());
    }


    @Test
    public void testCreateResultDraw01() {
        Match match = new Match(smartEngine, dummyEngine, new MatchByDepth(1), PGN.from(FEN.of("6Q1/P7/7k/3p3p/3P3P/1P1BP3/5P2/3K4 b - - 5 48")));

        MatchResult result = match.createResult();

        PGN pgn = result.pgn();

        // Deberia ganar el engine smartEngine
        assertEquals(SMART, pgn.getWhite());
        assertEquals(DUMMY, pgn.getBlack());
        assertEquals(PGN.Result.DRAW, pgn.getResult());
    }

    @Test
    public void testCreateResultDraw02() {
        Match match = new Match(smartEngine, dummyEngine, new MatchByDepth(1), PGN.from(FEN.of("3k4/5p2/1p1bp3/3p3p/3P3P/7K/p7/6q1 w - - 5 48")));


        MatchResult result = match.createResult();

        PGN pgn = result.pgn();

        // Deberia ganar el engine smartEngine
        assertEquals(SMART, pgn.getWhite());
        assertEquals(DUMMY, pgn.getBlack());
        assertEquals(PGN.Result.DRAW, pgn.getResult());
    }


    @Test
    public void testCreateResultOngoing01() {
        Match match = new Match(smartEngine, dummyEngine, new MatchByDepth(1), PGN.from(FEN.START_POSITION));

        match.setMatchTimeOut(new MatchTimeOut("TimeOut", smartEngine));

        MatchResult result = match.createResult();

        PGN pgn = result.pgn();

        // Deberia ganar el engine smartEngine
        assertEquals(SMART, pgn.getWhite());
        assertEquals(DUMMY, pgn.getBlack());
        assertEquals(PGN.Result.BLACK_WINS, pgn.getResult());
        assertEquals(PGN.Termination.TIME_FORFEIT, pgn.getTermination());
    }

}
