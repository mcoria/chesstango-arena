package net.chesstango.arena.core.reports;

import net.chesstango.arena.core.MatchResult;
import net.chesstango.engine.SearchResponse;
import net.chesstango.reports.engine.SearchManagerSummaryReport;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Predicate;

/**
 * Este reporte resume las sessiones de engine Tango
 *
 * @author Mauricio Coria
 */
public class MatchesBySearchManager {
    private enum BreakType {
        NONE,
        COLOR,      // Resta implementar
        GAMES
    }

    private final SearchManagerSummaryReport searchesSummaryReport = new SearchManagerSummaryReport();

    private BreakType breakType = BreakType.NONE;

    public MatchesBySearchManager printReport(PrintStream output) {
        searchesSummaryReport.printReport(output);
        return this;
    }


    public MatchesBySearchManager withMathResults(List<MatchResult> matchResults) {
        Set<String> engines = new HashSet<>();
        Set<String> events = new HashSet<>();

        matchResults
                .stream()
                .map(MatchResult::pgn)
                .forEach(pgn -> {
                    engines.add(pgn.getWhite());
                    engines.add(pgn.getBlack());
                    events.add(pgn.getEvent());
                });

        if (breakType == BreakType.COLOR || breakType == BreakType.NONE) {
            engines.forEach(engineName -> {
                // Extracts searches for engine playing white
                List<SearchResponse> searchesWhite = searchesWhite(matchResults, matchResult -> Objects.equals(matchResult.pgn().getWhite(), engineName));

                // Extracts searches for engine playing black
                List<SearchResponse> searchesBlack = searchesBlack(matchResults, matchResult -> Objects.equals(matchResult.pgn().getBlack(), engineName));


                // Adds searches to report based on break type
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
        } else if (breakType == BreakType.GAMES) {
            events.forEach(event -> {
                List<SearchResponse> searchesWhite = searchesWhite(matchResults, matchResult -> Objects.equals(matchResult.pgn().getEvent(), event));

                List<SearchResponse> searchesBlack = searchesBlack(matchResults, matchResult -> Objects.equals(matchResult.pgn().getEvent(), event));

                List<SearchResponse> searches = new ArrayList<>();
                searches.addAll(searchesWhite);
                searches.addAll(searchesBlack);

                if (!searches.isEmpty()) {
                    searchesSummaryReport.addSearchResponses(event, searches);
                }
            });
        }

        return this;
    }

    private List<SearchResponse> searchesWhite(List<MatchResult> matchResults, Predicate<MatchResult> filter) {
        return matchResults
                .stream()
                .filter(filter)
                .map(MatchResult::whiteSearches)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();
    }

    private List<SearchResponse> searchesBlack(List<MatchResult> matchResults, Predicate<MatchResult> filter) {
        return matchResults
                .stream()
                .filter(filter)
                .map(MatchResult::blackSearches)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .toList();
    }


    public MatchesBySearchManager breakByColor() {
        this.breakType = BreakType.COLOR;
        return this;
    }

    public MatchesBySearchManager breakByGame() {
        this.breakType = BreakType.GAMES;
        return this;
    }

}
