package net.chesstango.arena.core.reports;

import net.chesstango.arena.core.MatchResult;
import net.chesstango.engine.SearchByTreeResult;
import net.chesstango.reports.summary.PrintCutoffStatics;
import net.chesstango.reports.summary.PrintNodesVisitedStatistics;
import net.chesstango.reports.summary.SearchesByTreeSummaryModel;
import net.chesstango.search.SearchResult;

import java.io.PrintStream;
import java.util.*;

/**
 * Este reporte resume las sessiones de engine Tango
 *
 * @author Mauricio Coria
 */
public class SearchesByTreeSummaryReport {
    private final List<SearchesByTreeSummaryModel> searchesByTreeSummaryModels = new ArrayList<>();
    private boolean printNodesVisitedStatistics;
    private boolean printCutoffStatistics;
    private boolean breakByColor;
    private PrintStream out;

    public SearchesByTreeSummaryReport printReport(PrintStream output) {
        out = output;
        print();
        return this;
    }


    public SearchesByTreeSummaryReport withMathResults(List<MatchResult> matchResults) {
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


            addReportAggregator(engineName, searchesWhite, searchesBlack);

        });

        return this;
    }


    public void addReportAggregator(String engineName, List<SearchResult> searchesWhite, List<SearchResult> searchesBlack) {
        if (breakByColor) {
            if (!searchesWhite.isEmpty()) {
                searchesByTreeSummaryModels.add(SearchesByTreeSummaryModel.collectStatics(String.format("%s white", engineName), searchesWhite));
            }
            if (!searchesBlack.isEmpty()) {
                searchesByTreeSummaryModels.add(SearchesByTreeSummaryModel.collectStatics(String.format("%s black", engineName), searchesBlack));
            }
        } else {
            List<SearchResult> searches = new ArrayList<>();
            searches.addAll(searchesWhite);
            searches.addAll(searchesBlack);

            if (!searches.isEmpty()) {
                searchesByTreeSummaryModels.add(SearchesByTreeSummaryModel.collectStatics(engineName, searches));
            }
        }
    }

    private void print() {
        if (printNodesVisitedStatistics) {
            new PrintNodesVisitedStatistics(out, searchesByTreeSummaryModels)
                    .printNodesVisitedStaticsByType()
                    .printNodesVisitedStatics()
                    .printNodesVisitedStaticsAvg();
        }


        if (printCutoffStatistics) {
            new PrintCutoffStatics(out, searchesByTreeSummaryModels)
                    .printCutoffStatics();
        }
    }


    public SearchesByTreeSummaryReport withNodesVisitedStatistics() {
        this.printNodesVisitedStatistics = true;
        return this;
    }

    public SearchesByTreeSummaryReport withCutoffStatistics() {
        this.printCutoffStatistics = true;
        return this;
    }

    public SearchesByTreeSummaryReport breakByColor() {
        this.breakByColor = true;
        return this;
    }

}
