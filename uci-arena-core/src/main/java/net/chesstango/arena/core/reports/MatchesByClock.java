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

        Set<String> engines = new HashSet<>();

        Map<String, Duration> thinkingTimeByEngine = new HashMap<>();
        Map<String, Duration> remainingTimeByEngine = new HashMap<>();

        matchResult
                .stream()
                .map(MatchResult::pgn)
                .forEach(pgn -> {
                    String whiteEngine = pgn.getWhite();
                    String blackEngine = pgn.getBlack();

                    Optional<String> whiteRemainingTime = Optional.empty();
                    Optional<String> blackRemainingTime = Optional.empty();

                    engines.add(whiteEngine);
                    engines.add(blackEngine);

                    FEN initialFEN = pgn.getFen() == null ? FEN.START_POSITION : pgn.getFen();
                    boolean turn = "w".equals(initialFEN.getActiveColor());
                    for (PGNMove pgnMove : pgn.getPgnMoves()) {

                        /**
                         * Ger the elapsed time for the current move
                         */
                        Optional<String> elapsedTime = pgnMove.getCommand(PGNMove.ELAPSED_MOVE_TIME_COMMAND);
                        if (elapsedTime.isPresent()) {
                            String currentEngine = turn ? whiteEngine : blackEngine;

                            Duration thinkingTime = thinkingTimeByEngine.getOrDefault(currentEngine, Duration.ZERO);

                            Duration elapsedTimeDuration = getTimeDuration(elapsedTime.get());

                            thinkingTimeByEngine.put(currentEngine, thinkingTime.plus(elapsedTimeDuration));
                        }

                        /**
                         * Track the last remaining time for the current engine
                         */
                        Optional<String> remainingTime = pgnMove.getCommand(PGNMove.CLOCK_COMMAND);
                        if (remainingTime.isPresent()) {
                            if (turn) {
                                whiteRemainingTime = remainingTime;
                            } else {
                                blackRemainingTime = remainingTime;
                            }
                        }

                        turn = !turn;
                    }

                    if (whiteRemainingTime.isPresent()) {
                        Duration remainingTime = remainingTimeByEngine.getOrDefault(whiteEngine, Duration.ZERO);
                        Duration remainingTimeDuration = getTimeDuration(whiteRemainingTime.get());
                        remainingTimeByEngine.put(whiteEngine, remainingTime.plus(remainingTimeDuration));
                    }
                    if (blackRemainingTime.isPresent()) {
                        Duration remainingTime = remainingTimeByEngine.getOrDefault(blackEngine, Duration.ZERO);
                        Duration remainingTimeDuration = getTimeDuration(blackRemainingTime.get());
                        remainingTimeByEngine.put(blackEngine, remainingTime.plus(remainingTimeDuration));
                    }
                });

        engines.forEach(engineName -> {
            Duration thinkingTime = thinkingTimeByEngine.getOrDefault(engineName, Duration.ZERO);
            Duration remainingTime = remainingTimeByEngine.getOrDefault(engineName, Duration.ZERO);

            ReportRowModel row = new ReportRowModel();
            row.engineName = engineName;
            row.elapsedTimeTotal = String.format("%02d:%02d:%02d:%02d",
                    thinkingTime.toDaysPart(),
                    thinkingTime.toHoursPart(),
                    thinkingTime.toMinutesPart(),
                    thinkingTime.toSecondsPart()
            );
            row.remainingTimeTotal = String.format("%02d:%02d:%02d:%02d",
                    remainingTime.toDaysPart(),
                    remainingTime.toHoursPart(),
                    remainingTime.toMinutesPart(),
                    remainingTime.toSecondsPart()
            );

            reportRowModels.add(row);
        });

        return this;
    }

    private static Duration getTimeDuration(String elapsedTime) {
        String[] parts = elapsedTime.split(":");
        long hours = Long.parseLong(parts[0]);
        long minutes = Long.parseLong(parts[1]);

        parts = parts[2].split("\\.");

        long seconds = Long.parseLong(parts[0]);
        long millis = Long.parseLong(parts[1]);

        return Duration
                .ofHours(hours)
                .plusMinutes(minutes)
                .plusSeconds(seconds)
                .plusMillis(millis);
    }

    public MatchesByClock printReport(PrintStream output) {
        out = output;
        print();
        return this;
    }

    private void print() {
        out.print("\n Clock report \n");
        out.print(" ______________________________________________________________________\n");
        out.print("|ENGINE NAME                        |   ELAPSED TIME |  REMAINING TIME |\n");

        reportRowModels
                .stream()
                .sorted(theComparator)
                .forEach(row -> {
                    out.printf("|%34s |    %11s |     %11s |\n", row.engineName, row.elapsedTimeTotal, row.remainingTimeTotal);
                });
        out.print(" ----------------------------------------------------------------------\n");
    }

    @Getter
    public static class ReportRowModel {
        String engineName;
        String elapsedTimeTotal;
        String remainingTimeTotal;
    }
}
