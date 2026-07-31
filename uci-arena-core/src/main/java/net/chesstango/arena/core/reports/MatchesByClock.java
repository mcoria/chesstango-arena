package net.chesstango.arena.core.reports;

import lombok.Getter;
import net.chesstango.arena.core.MatchResult;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.pgn.PGNMove;

import java.io.PrintStream;
import java.time.Duration;
import java.util.*;

/**
 * Este reporte resume las sessiones de engine Tango
 *
 * @author Mauricio Coria
 */
public class MatchesByClock {

    @Getter
    private final List<ReportRowModel> reportRowModels = new ArrayList<>();

    private final Comparator<? super ReportRowModel> theComparator = Comparator.comparing(row -> row.engineName);

    private PrintStream out;

    public MatchesByClock withMathResults(List<MatchResult> matchResult) {

        Map<String, Duration> thinkingTimeByEngine = new HashMap<>();

        matchResult
                .stream()
                .map(MatchResult::pgn)
                .forEach(pgn -> {
                    String whiteEngine = pgn.getWhite();
                    String blackEngine = pgn.getBlack();

                    FEN initialFEN = pgn.getFen() == null ? FEN.START_POSITION : pgn.getFen();
                    boolean turn = "w".equals(initialFEN.getActiveColor());

                    for (PGNMove pgnMove : pgn.getPgnMoves()) {
                        String currentEngine = turn ? whiteEngine : blackEngine;
                        Optional<String> elapsedTime = pgnMove.getCommand(PGNMove.ELAPSED_MOVE_TIME_COMMAND);
                        if (elapsedTime.isPresent()) {
                            Duration thinkingTime = thinkingTimeByEngine.getOrDefault(currentEngine, Duration.ZERO);

                            String[] parts = elapsedTime.get().split(":");
                            long hours = Long.parseLong(parts[0]);
                            long minutes = Long.parseLong(parts[1]);

                            parts = parts[2].split("\\.");

                            long seconds = Long.parseLong(parts[0]);
                            long millis = Long.parseLong(parts[1]);

                            Duration elapsedTimeDuration = Duration
                                    .ofHours(hours)
                                    .plusMinutes(minutes)
                                    .plusSeconds(seconds)
                                    .plusMillis(millis);

                            thinkingTimeByEngine.put(currentEngine, thinkingTime.plus(elapsedTimeDuration));
                        }
                        turn = !turn;
                    }
                });

        thinkingTimeByEngine.forEach((engineName, thinkingTime) -> {
            ReportRowModel row = new ReportRowModel();
            row.engineName = engineName;
            row.elapsedTimeTotal = String.format("%02d:%02d:%02d",
                    thinkingTime.toHoursPart(),
                    thinkingTime.toMinutesPart(),
                    thinkingTime.toSecondsPart()
            );
            reportRowModels.add(row);
        });

        return this;
    }

    public MatchesByClock printReport(PrintStream output) {
        out = output;
        print();
        return this;
    }

    private void print() {
        out.print("\n Clock report \n");
        out.print(" _____________________________________________________\n");
        out.print("|ENGINE NAME                        |   ELAPSED TIME %|\n");

        reportRowModels
                .stream()
                .sorted(theComparator)
                .forEach(row -> {
                    out.printf("|%34s |        %8s |\n", row.engineName, row.elapsedTimeTotal);
                });
        out.print(" -----------------------------------------------------\n");
    }

    @Getter
    public static class ReportRowModel {
        String engineName;
        String elapsedTimeTotal;
    }
}
