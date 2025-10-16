package net.chesstango.arena.core.reports;

import net.chesstango.arena.core.MatchResult;
import net.chesstango.engine.SearchByTreeResult;
import net.chesstango.reports.summary.SearchesSummaryReport;
import net.chesstango.search.SearchResult;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Este reporte resume las sessiones de engine Tango
 *
 * @author Mauricio Coria
 */
public class MatchesSearchesByTreeSummaryReport {
    private final SearchesSummaryReport searchesSummaryReport = new SearchesSummaryReport();

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
                    .map(searchResponse -> (SearchByTreeResult) searchResponse)
                    .map(SearchByTreeResult::getSearchResult)
                    .toList();

            List<SearchResult> searchesBlack = matchResults.stream()
                    .filter(matchResult -> Objects.equals(matchResult.pgn().getBlack(), engineName))
                    .map(MatchResult::blackSearches)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .map(searchResponse -> (SearchByTreeResult) searchResponse)
                    .map(SearchByTreeResult::getSearchResult)
                    .toList();


            searchesSummaryReport.addSearchesByTreeSummaryModel(engineName, searchesWhite, searchesBlack);

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

    public MatchesSearchesByTreeSummaryReport breakByColor() {
        searchesSummaryReport.breakByColor();
        return this;
    }

}
