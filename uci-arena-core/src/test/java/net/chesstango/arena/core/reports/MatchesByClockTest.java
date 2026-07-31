package net.chesstango.arena.core.reports;

import net.chesstango.arena.core.MatchResult;
import net.chesstango.gardel.pgn.PGN;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MatchesByClockTest {

    /**
     * Tests the {@code withMathResults} method in the {@code MatchesByClock} class.
     * This test processes a real PGN string simulating practical scenarios.
     */
    @Test
    void testWithMathResults_singlePGN() {
        // Arrange
        MatchesByClock matchesByClock = new MatchesByClock();

        String pgnString =
                """
                        [Event "SCCT - Unique v150"]
                        [Site "?"]
                        [Date "2023.02.07"]
                        [Round "?"]
                        [White "Engine1"]
                        [Black "Engine2"]
                        [Result "*"]
                        [ECO "C16"]
                        [PlyCount "18"]
                        [EventDate "2022.??.??"]
                        
                        1. e4 {[%emt 0:02:00.500]} e6 {[%emt 0:01:30.400]}
                        2. d4 {[%emt 0:01:45.500]} d5 {[%emt 0:01:20.600]} *
                        """;

        PGN pgn = PGN.from(pgnString);
        MatchResult matchResult = new MatchResult(pgn, null, null);

        // Act
        matchesByClock.withMathResults(List.of(matchResult));

        // Assert
        assertEquals(2, matchesByClock.getReportRowModels().size());

        MatchesByClock.ReportRowModel engine1Row = matchesByClock.getReportRowModels().stream()
                .filter(row -> row.getEngineName().equals("Engine1"))
                .findFirst()
                .orElse(null);
        assertNotNull(engine1Row);
        assertEquals("00:03:46", engine1Row.getElapsedTimeTotal());

        MatchesByClock.ReportRowModel engine2Row = matchesByClock.getReportRowModels().stream()
                .filter(row -> row.getEngineName().equals("Engine2"))
                .findFirst()
                .orElse(null);
        assertNotNull(engine2Row);
        assertEquals("00:02:51", engine2Row.getElapsedTimeTotal());
    }

    /**
     * Tests for correct computation of elapsed times across multiple moves for engines.
     */
    @Test
    void testWithMathResults_multiplePGNs() {
        // Arrange
        MatchesByClock matchesByClock = new MatchesByClock();

        String pgnString1 =
                """
                        [Event "SCCT - Unique v150"]
                        [Site "?"]
                        [Date "2023.02.07"]
                        [Round "?"]
                        [White "Engine1"]
                        [Black "Engine2"]
                        [Result "*"]
                        [ECO "C16"]
                        [PlyCount "10"]
                        [EventDate "2022.??.??"]
                        
                        1. e4 {[%emt 0:01:00.500]} e5 {[%emt 0:00:10.250]}
                        2. Nf3 {[%emt 0:02:00.500]} Nc6 {[%emt 0:00:20.250]} *
                        """;

        PGN pgn1 = PGN.from(pgnString1);
        MatchResult matchResult1 = new MatchResult(pgn1, null, null);

        String pgnString2 =
                """
                        [Event "SCCT - Unique v150"]
                        [Site "?"]
                        [Date "2023.02.07"]
                        [Round "?"]
                        [White "Engine2"]
                        [Black "Engine1"]
                        [Result "*"]
                        [ECO "C16"]
                        [PlyCount "10"]
                        [EventDate "2022.??.??"]
                        
                        1. e4 {[%emt 0:00:30.500]} e5 {[%emt 0:03:00.250]}
                        2. Nf3 {[%emt 0:00:40.500]} Nc6 {[%emt 0:04:00.250]} *
                        """;

        PGN pgn2 = PGN.from(pgnString2);
        MatchResult matchResult2 = new MatchResult(pgn2, null, null);


        // Act
        matchesByClock.withMathResults(List.of(matchResult1, matchResult2));

        // Assert
        assertEquals(2, matchesByClock.getReportRowModels().size());

        MatchesByClock.ReportRowModel engine1Row = matchesByClock.getReportRowModels().stream()
                .filter(row -> row.getEngineName().equals("Engine1"))
                .findFirst()
                .orElse(null);
        assertNotNull(engine1Row);
        assertEquals("00:10:01", engine1Row.getElapsedTimeTotal());

        MatchesByClock.ReportRowModel engine2Row = matchesByClock.getReportRowModels().stream()
                .filter(row -> row.getEngineName().equals("Engine2"))
                .findFirst()
                .orElse(null);
        assertNotNull(engine2Row);
        assertEquals("00:01:41", engine2Row.getElapsedTimeTotal());
    }

    /**
     * Verifies that no results are added when a PGN string lacks elapsed time information.
     */
    @Test
    void testWithMathResults_noElapsedTimeInPGN() {
        // Arrange
        MatchesByClock matchesByClock = new MatchesByClock();

        String pgnString =
                """
                        [Event "SCCT - Unique v150"]
                        [Site "?"]
                        [Date "2023.02.07"]
                        [Round "?"]
                        [White "Engine1"]
                        [Black "Engine2"]
                        [Result "*"]
                        [ECO "C16"]
                        [PlyCount "18"]
                        [EventDate "2022.??.??"]
                        
                        1. e4 e6 2. d4 d5 3. Nc3 Bb4 4. e5 Qd7 5. a3 Bxc3+ 6. bxc3 b6 7. Qg4 f5 8. Qg3 Bb7 9. a4 Nc6 *
                        """;

        PGN pgn = PGN.from(pgnString);
        MatchResult matchResult = new MatchResult(pgn, null, null);

        // Act
        matchesByClock.withMathResults(List.of(matchResult));

        // Assert
        assertTrue(matchesByClock.getReportRowModels().isEmpty());
    }
}