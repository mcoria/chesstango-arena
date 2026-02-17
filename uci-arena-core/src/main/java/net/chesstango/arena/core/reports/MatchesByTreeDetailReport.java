package net.chesstango.arena.core.reports;


import net.chesstango.arena.core.MatchResult;
import net.chesstango.engine.SearchByTreeResult;
import net.chesstango.reports.tree.summary.DetailReport;
import net.chesstango.search.SearchResult;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Por cada juego de Tango muestra estadísticas de cada arbol de búsqueda.
 *
 * @author Mauricio Coria
 */
public class MatchesByTreeDetailReport {
    private final DetailReport searchesByTreeReport = new DetailReport();


    public MatchesByTreeDetailReport printReport(PrintStream out) {
        this.searchesByTreeReport.printReport(out);
        return this;
    }

    public MatchesByTreeDetailReport withMoveResults(List<SearchResult> searchResultList) {
        this.searchesByTreeReport.withMoveResults(searchResultList);
        return this;
    }

    public MatchesByTreeDetailReport setReportTitle(String reportTitle) {
        this.searchesByTreeReport.setReportTitle(reportTitle);
        return this;
    }

    public static List<SearchResult> filterByEngineName(String engineName, List<MatchResult> matchResult) {
        List<SearchResult> searchResultList = new LinkedList<>();

        matchResult.stream()
                .filter(result -> Objects.equals(result.pgn().getWhite(), engineName))
                .map(MatchResult::whiteSearches)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(searchResponse -> searchResponse instanceof SearchByTreeResult)
                .map(searchResponse -> (SearchByTreeResult) searchResponse)
                .map(SearchByTreeResult::searchResult)
                .forEach(searchResultList::add);


        matchResult.stream()
                .filter(result -> Objects.equals(result.pgn().getBlack(), engineName))
                .map(MatchResult::blackSearches)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(searchResponse -> searchResponse instanceof SearchByTreeResult)
                .map(searchResponse -> (SearchByTreeResult) searchResponse)
                .map(SearchByTreeResult::searchResult)
                .forEach(searchResultList::add);

        return searchResultList;
    }


    public MatchesByTreeDetailReport withCutoffStatistics() {
        searchesByTreeReport.withCutoffStatistics();
        return this;
    }

    public MatchesByTreeDetailReport withNodesVisitedStatistics() {
        searchesByTreeReport.withNodesVisitedStatistics();
        return this;
    }


    public MatchesByTreeDetailReport withEvaluationReport() {
        searchesByTreeReport.withEvaluationReport();
        return this;
    }

    public MatchesByTreeDetailReport withPrincipalVariationReport() {
        searchesByTreeReport.withPrincipalVariationReport();
        return this;
    }
}
