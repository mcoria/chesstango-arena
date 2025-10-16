package net.chesstango.arena.core.reports;


import net.chesstango.arena.core.MatchResult;
import net.chesstango.engine.SearchByTreeResult;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.reports.SearchesByTreeReport;
import net.chesstango.search.SearchResult;

import java.io.PrintStream;
import java.util.List;

/**
 * Por cada juego de Tango muestra estadísticas de cada arbol de búsqueda.
 *
 * @author Mauricio Coria
 */
public class SearchesByTreeFromMatchesReport {
    private final SearchesByTreeReport searchesByTreeReport = new SearchesByTreeReport();

    public SearchesByTreeFromMatchesReport printReport(PrintStream out) {
        searchesByTreeReport.printReport(out);
        return this;
    }

    public SearchesByTreeFromMatchesReport withMathResults(List<MatchResult> matchResult) {
        matchResult.stream()
                .filter(result -> result.whiteSearches() != null)
                .forEach(result -> {
                    PGN pgn = result.pgn();
                    String engineName = pgn.getWhite();

                    List<SearchResult> whiteSearches = result.whiteSearches()
                            .stream()
                            .filter(searchResponse -> searchResponse instanceof SearchByTreeResult)
                            .map(searchResponse -> (SearchByTreeResult) searchResponse)
                            .map(SearchByTreeResult::getSearchResult)
                            .toList();


                    searchesByTreeReport.addReportAggregator(String.format("%s - %s", engineName, pgn.getEvent()), whiteSearches);
                });

        matchResult.stream()
                .filter(result -> result.blackSearches() != null)
                .forEach(result -> {
                    PGN pgn = result.pgn();
                    String engineName = pgn.getBlack();

                    List<SearchResult> blackSearches = result.blackSearches()
                            .stream()
                            .filter(searchResponse -> searchResponse instanceof SearchByTreeResult)
                            .map(searchResponse -> (SearchByTreeResult) searchResponse)
                            .map(SearchByTreeResult::getSearchResult)
                            .toList();

                    searchesByTreeReport.addReportAggregator(String.format("%s - %s", engineName, pgn.getEvent()), blackSearches);
                });
        return this;
    }



    public SearchesByTreeFromMatchesReport withCutoffStatistics() {
        searchesByTreeReport.withCutoffStatistics();
        return this;
    }

    public SearchesByTreeFromMatchesReport withNodesVisitedStatistics() {
        searchesByTreeReport.withNodesVisitedStatistics();
        return this;
    }


    public SearchesByTreeFromMatchesReport withEvaluationReport() {
        searchesByTreeReport.withEvaluationReport();
        return this;
    }

    public SearchesByTreeFromMatchesReport withPrincipalVariation() {
        searchesByTreeReport.withPrincipalVariation();
        return this;
    }
}
