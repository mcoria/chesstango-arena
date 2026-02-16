package net.chesstango.arena.core.reports;

import net.chesstango.arena.core.MatchResult;
import net.chesstango.engine.SearchByTreeResult;
import net.chesstango.reports.tree.SummaryReport;
import net.chesstango.search.SearchResult;

import java.io.PrintStream;
import java.util.*;

/**
 * Este reporte resume las sessiones de engine Tango
 *
 * @author Mauricio Coria
 */
public class MatchesSearchesByTreeSummaryReport {
    private enum BreakType {
        NONE,
        COLOR,      // Resta implementar
        GAMES
    }

    private final SummaryReport searchesSummaryReport = new SummaryReport();

    private BreakType breakType = BreakType.NONE;

    public MatchesSearchesByTreeSummaryReport printReport(PrintStream output) {
        searchesSummaryReport.printReport(output);
        return this;
    }


    public MatchesSearchesByTreeSummaryReport withMathResults(List<MatchResult> matchResults) {
        Set<String> engineNames = new HashSet<>();

        matchResults.stream().map(MatchResult::pgn).forEach(pgn -> {
            engineNames.add(pgn.getWhite());
            engineNames.add(pgn.getBlack());
        });

        engineNames.forEach(engineName -> {
            List<SearchResult> searchesWhite = matchResults.stream()
                    .filter(matchResult -> Objects.equals(matchResult.pgn().getWhite(), engineName))
                    .map(MatchResult::whiteSearches)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .filter(SearchByTreeResult.class::isInstance)
                    .map(SearchByTreeResult.class::cast)
                    .map(SearchByTreeResult::searchResult)
                    .toList();

            List<SearchResult> searchesBlack = matchResults.stream()
                    .filter(matchResult -> Objects.equals(matchResult.pgn().getBlack(), engineName))
                    .map(MatchResult::blackSearches)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .filter(SearchByTreeResult.class::isInstance)
                    .map(SearchByTreeResult.class::cast)
                    .map(SearchByTreeResult::searchResult)
                    .toList();


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

        return this;
    }


    public MatchesSearchesByTreeSummaryReport withNodesVisitedStatistics() {
        searchesSummaryReport.withNodesVisitedStatistics();
        return this;
    }

    public MatchesSearchesByTreeSummaryReport withCutoffStatistics() {
        searchesSummaryReport.withCutoffStatistics();
        return this;
    }

    public MatchesSearchesByTreeSummaryReport withTranspositionStatistics() {
        //searchesSummaryReport.withTranspositionStatistics();
        return this;
    }

    public MatchesSearchesByTreeSummaryReport breakByColor() {
        this.breakType = BreakType.COLOR;
        return this;
    }

    public MatchesSearchesByTreeSummaryReport breakByGame() {
        this.breakType = BreakType.GAMES;
        return this;
    }

}
