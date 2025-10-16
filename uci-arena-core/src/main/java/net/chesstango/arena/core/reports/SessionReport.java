package net.chesstango.arena.core.reports;

import net.chesstango.engine.SearchByTreeResult;
import net.chesstango.search.SearchResult;
import net.chesstango.arena.core.reports.sessionreport_ui.PrintCutoffStatics;
import net.chesstango.arena.core.reports.sessionreport_ui.PrintNodesVisitedStatistics;
import net.chesstango.arena.core.MatchResult;

import java.io.PrintStream;
import java.util.*;

/**
 * Este reporte resume las sessiones de engine Tango
 *
 * @author Mauricio Coria
 */
public class SessionReport {
    private final List<SessionReportModel> sessionReportModels = new ArrayList<>();
    private boolean printNodesVisitedStatistics;
    private boolean printCutoffStatistics;
    private boolean breakByColor;
    private PrintStream out;

    public SessionReport printReport(PrintStream output) {
        out = output;
        print();
        return this;
    }


    public SessionReport withMathResults(List<MatchResult> matchResults) {
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


            if (breakByColor) {
                if (!searchesWhite.isEmpty()) {
                    sessionReportModels.add(SessionReportModel.collectStatics(String.format("%s white", engineName), searchesWhite));
                }
                if (!searchesBlack.isEmpty()) {
                    sessionReportModels.add(SessionReportModel.collectStatics(String.format("%s black", engineName), searchesBlack));
                }
            } else {
                List<SearchResult> searches = new ArrayList<>();
                searches.addAll(searchesWhite);
                searches.addAll(searchesBlack);

                if (!searches.isEmpty()) {
                    sessionReportModels.add(SessionReportModel.collectStatics(engineName, searches));
                }
            }
        });

        return this;
    }

    private void print() {
        if (printNodesVisitedStatistics) {
            new PrintNodesVisitedStatistics(out, sessionReportModels)
                    .printNodesVisitedStaticsByType()
                    .printNodesVisitedStatics()
                    .printNodesVisitedStaticsAvg();
        }


        if (printCutoffStatistics) {
            new PrintCutoffStatics(out, sessionReportModels)
                    .printCutoffStatics();
        }
    }


    public SessionReport withNodesVisitedStatistics() {
        this.printNodesVisitedStatistics = true;
        return this;
    }

    public SessionReport withCutoffStatistics() {
        this.printCutoffStatistics = true;
        return this;
    }

    public SessionReport breakByColor() {
        this.breakByColor = true;
        return this;
    }

}
