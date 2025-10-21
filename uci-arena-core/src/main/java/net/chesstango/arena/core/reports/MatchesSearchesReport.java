package net.chesstango.arena.core.reports;

import net.chesstango.arena.core.MatchResult;
import net.chesstango.engine.SearchResponse;
import net.chesstango.reports.engine.SearchManagerSummaryReport;

import java.io.PrintStream;
import java.util.*;

/**
 * Este reporte resume las sessiones de engine Tango
 *
 * @author Mauricio Coria
 */
public class MatchesSearchesReport {
    private enum BreakType {
        NONE,
        COLOR,      // Resta implementar
        GAMES
    }

    private final SearchManagerSummaryReport searchesSummaryReport = new SearchManagerSummaryReport();

    private BreakType breakType = BreakType.NONE;

    public MatchesSearchesReport printReport(PrintStream output) {
        searchesSummaryReport.printReport(output);
        return this;
    }


    public MatchesSearchesReport withMathResults(List<MatchResult> matchResults) {
        Set<String> engineNames = new HashSet<>();

        matchResults.stream().map(MatchResult::pgn).forEach(pgn -> {
            engineNames.add(pgn.getWhite());
            engineNames.add(pgn.getBlack());
        });

        engineNames.forEach(engineName -> {
            List<SearchResponse> searchesWhite = matchResults.stream()
                    .filter(matchResult -> Objects.equals(matchResult.pgn().getWhite(), engineName))
                    .map(MatchResult::whiteSearches)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .toList();

            List<SearchResponse> searchesBlack = matchResults.stream()
                    .filter(matchResult -> Objects.equals(matchResult.pgn().getBlack(), engineName))
                    .map(MatchResult::blackSearches)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .toList();


            if (breakType == BreakType.COLOR) {
                if (!searchesWhite.isEmpty()) {
                    searchesSummaryReport.addSearchResponses(String.format("%s white", engineName), searchesWhite);
                }
                if (!searchesBlack.isEmpty()) {
                    searchesSummaryReport.addSearchResponses(String.format("%s black", engineName), searchesBlack);
                }
            } else if (breakType == BreakType.NONE) {
                List<SearchResponse> searches = new ArrayList<>();
                searches.addAll(searchesWhite);
                searches.addAll(searchesBlack);

                if (!searches.isEmpty()) {
                    searchesSummaryReport.addSearchResponses(engineName, searches);
                }
            }
        });

        return this;
    }


    public MatchesSearchesReport breakByColor() {
        this.breakType = BreakType.COLOR;
        return this;
    }

    public MatchesSearchesReport breakByGame() {
        this.breakType = BreakType.GAMES;
        return this;
    }

}
