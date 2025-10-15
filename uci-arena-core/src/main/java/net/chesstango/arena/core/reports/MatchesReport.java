package net.chesstango.arena.core.reports;

import lombok.Getter;
import net.chesstango.arena.core.MatchResult;
import net.chesstango.gardel.pgn.PGN;

import java.io.PrintStream;
import java.util.*;

/**
 * Este reporte resume el resultado de un conjunto de partidos
 *
 * @author Mauricio Coria
 */
public class MatchesReport {

    private final List<ReportRowModel> reportRowModels = new ArrayList<>();

    private PrintStream out;

    private Comparator<? super ReportRowModel> theComparator = Comparator.comparing(row -> row.engineName);

    public MatchesReport printReport(PrintStream output) {
        out = output;
        print();
        return this;
    }

    /**
     * @param matchResults
     */
    public MatchesReport withMatchResults(List<MatchResult> matchResults) {
        Set<String> engineNames = new HashSet<>();

        matchResults.stream().map(MatchResult::pgn).map(PGN::getWhite).forEach(engineNames::add);
        matchResults.stream().map(MatchResult::pgn).map(PGN::getBlack).forEach(engineNames::add);

        engineNames.stream().map(engineName -> createRowModel(engineName, matchResults)).forEach(reportRowModels::add);

        return this;
    }

    public MatchesReport sortBy(Comparator<ReportRowModel> comparator) {
        theComparator = comparator;
        return this;
    }


    private ReportRowModel createRowModel(String engineName, List<MatchResult> matchResult) {
        ReportRowModel row = new ReportRowModel();

        row.engineName = engineName;

        row.wonAsWhite = matchResult.stream().map(MatchResult::pgn).filter(pgn -> Objects.equals(pgn.getWhite(), engineName)).filter(pgn -> PGN.Result.WHITE_WINS == pgn.getResult()).count();
        row.lostAsWhite = matchResult.stream().map(MatchResult::pgn).filter(pgn -> Objects.equals(pgn.getWhite(), engineName)).filter(pgn -> PGN.Result.BLACK_WINS == pgn.getResult()).count();
        row.drawsAsWhite = matchResult.stream().map(MatchResult::pgn).filter(pgn -> Objects.equals(pgn.getWhite(), engineName)).filter(pgn -> PGN.Result.DRAW == pgn.getResult()).count();
        row.puntosAsWhite = row.wonAsWhite + 0.5 * row.drawsAsWhite;

        row.wonAsBlack = matchResult.stream().map(MatchResult::pgn).filter(pgn -> Objects.equals(pgn.getBlack(), engineName)).filter(pgn -> PGN.Result.BLACK_WINS == pgn.getResult()).count();
        row.lostAsBlack = matchResult.stream().map(MatchResult::pgn).filter(pgn -> Objects.equals(pgn.getBlack(), engineName)).filter(pgn -> PGN.Result.WHITE_WINS == pgn.getResult()).count();
        row.drawsAsBlack = matchResult.stream().map(MatchResult::pgn).filter(pgn -> Objects.equals(pgn.getBlack(), engineName)).filter(pgn -> PGN.Result.DRAW == pgn.getResult()).count();
        row.puntosAsBlack = row.wonAsBlack + 0.5 * row.drawsAsBlack;

        row.puntosTotal = row.puntosAsWhite + row.puntosAsBlack;
        row.playedGames = matchResult.stream().map(MatchResult::pgn).filter(pgn -> Objects.equals(pgn.getWhite(), engineName) || Objects.equals(pgn.getBlack(), engineName)).count();

        row.winPercentage = (row.puntosTotal / row.playedGames) * 100;

        return row;
    }


    private void print() {
        out.print(" _____________________________________________________________________________________________________________________________________________________________\n");
        out.print("|ENGINE NAME                        |   GAMES |WHITE WON|BLACK WON|WHITE LOST|BLACK LOST|WHITE DRAW|BLACK DRAW|WHITE POINTS|BLACK POINTS|TOTAL POINTS|   WIN %|\n");

        reportRowModels.stream()
                .sorted(theComparator)
                .forEach(row -> {
                    out.printf("|%34s |%8d |%8d |%8d |%9d |%9d |%9d |%9d |%11.1f |%11.1f |%11.1f | %6.1f |\n", row.engineName, row.playedGames, row.wonAsWhite, row.wonAsBlack, row.lostAsWhite, row.lostAsBlack, row.drawsAsWhite, row.drawsAsBlack, row.puntosAsWhite, row.puntosAsBlack, row.puntosTotal, row.winPercentage);
                });
        out.print(" -------------------------------------------------------------------------------------------------------------------------------------------------------------\n");
    }


    @Getter
    public static class ReportRowModel {
        String engineName;
        long wonAsWhite;
        long wonAsBlack;
        long lostAsWhite;
        long lostAsBlack;
        long drawsAsWhite;
        long drawsAsBlack;
        double puntosAsWhite;
        double puntosAsBlack;
        double puntosTotal;
        long playedGames;
        double winPercentage;
    }

}

