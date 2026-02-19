package net.chesstango.arena.core.reports;

import net.chesstango.arena.core.MatchResult;
import net.chesstango.engine.SearchByTreeResult;
import net.chesstango.reports.search.SummaryReport;
import net.chesstango.search.SearchResult;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Predicate;

/**
 * Este reporte resume las sessiones de engine Tango
 *
 * @author Mauricio Coria
 */
public class MatchesByTreeSummaryReport {
    private enum BreakType {
        NONE,
        COLOR,      // Resta implementar
        GAMES
    }

    private final SummaryReport searchesSummaryReport = new SummaryReport();

    private BreakType breakType = BreakType.NONE;

    public MatchesByTreeSummaryReport printReport(PrintStream output) {
        searchesSummaryReport.printReport(output);
        return this;
    }


    public MatchesByTreeSummaryReport withMathResults(List<MatchResult> matchResults) {
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
                List<SearchResult> searchesWhite = searchesWhite(matchResults, matchResult -> Objects.equals(matchResult.pgn().getWhite(), engineName));
                List<SearchResult> searchesBlack = searchesBlack(matchResults, matchResult -> Objects.equals(matchResult.pgn().getBlack(), engineName));

                if (breakType == BreakType.COLOR) {
                    if (!searchesWhite.isEmpty()) {
                        searchesSummaryReport.addSearchesByTreeSummaryModel(String.format("%s white", engineName), searchesWhite);
                    }
                    if (!searchesBlack.isEmpty()) {
                        searchesSummaryReport.addSearchesByTreeSummaryModel(String.format("%s black", engineName), searchesBlack);
                    }
                } else if (breakType == BreakType.NONE) {
                    List<SearchResult> searches = new ArrayList<>();
                    searches.addAll(searchesWhite);
                    searches.addAll(searchesBlack);

                    if (!searches.isEmpty()) {
                        searchesSummaryReport.addSearchesByTreeSummaryModel(engineName, searches);
                    }
                }
            });
        } else if (breakType == BreakType.GAMES) {
            events.forEach(event -> {
                List<SearchResult> searchesWhite = searchesWhite(matchResults, matchResult -> Objects.equals(matchResult.pgn().getEvent(), event));

                List<SearchResult> searchesBlack = searchesBlack(matchResults, matchResult -> Objects.equals(matchResult.pgn().getEvent(), event));

                List<SearchResult> searches = new ArrayList<>();
                searches.addAll(searchesWhite);
                searches.addAll(searchesBlack);

                if (!searches.isEmpty()) {
                    searchesSummaryReport.addSearchesByTreeSummaryModel(event, searches);
                }
            });
        }

        return this;
    }


    private List<SearchResult> searchesWhite(List<MatchResult> matchResults, Predicate<MatchResult> filter) {
        return matchResults.stream()
                .filter(filter)
                .map(MatchResult::whiteSearches)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(SearchByTreeResult.class::isInstance)
                .map(SearchByTreeResult.class::cast)
                .map(SearchByTreeResult::searchResult)
                .toList();
    }

    private List<SearchResult> searchesBlack(List<MatchResult> matchResults, Predicate<MatchResult> filter) {
        return matchResults.stream()
                .filter(filter)
                .map(MatchResult::blackSearches)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(SearchByTreeResult.class::isInstance)
                .map(SearchByTreeResult.class::cast)
                .map(SearchByTreeResult::searchResult)
                .toList();
    }


    public MatchesByTreeSummaryReport withNodesVisitedStatistics() {
        searchesSummaryReport.withNodesVisitedStatistics();
        return this;
    }

    public MatchesByTreeSummaryReport withCutoffStatistics() {
        searchesSummaryReport.withCutoffStatistics();
        return this;
    }

    public MatchesByTreeSummaryReport withTranspositionStatistics() {
        searchesSummaryReport.withTranspositionStatistics();
        return this;
    }

    public MatchesByTreeSummaryReport breakByColor() {
        this.breakType = BreakType.COLOR;
        return this;
    }

    public MatchesByTreeSummaryReport breakByGame() {
        this.breakType = BreakType.GAMES;
        return this;
    }

}
