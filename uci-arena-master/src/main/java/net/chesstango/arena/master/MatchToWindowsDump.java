package net.chesstango.arena.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.core.MatchResult;
import net.chesstango.engine.SearchByTreeResult;
import net.chesstango.evaluation.Evaluator;
import net.chesstango.search.SearchResult;
import net.chesstango.search.SearchResultByDepth;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Por cada juego de Tango muestra estadísticas de cada arbol de búsqueda.
 *
 * @author Mauricio Coria
 */
@Slf4j
public class MatchToWindowsDump {

    public void dump(List<MatchResult> matchResult) {
        List<SearchResult> searchResultList = new LinkedList<>();
        searchResultList.addAll(searchesWhite(matchResult));
        searchResultList.addAll(searchesBlack(matchResult));


        try (PrintWriter writer = new PrintWriter(new FileWriter("evaluations.txt", true))) {
            for (SearchResult searchResult : searchResultList) {
                int lastEvaluation = searchResult
                        .getSearchResultByDepths()
                        .getLast()
                        .getBestEvaluation();
                if (searchResult.getSearchResultByDepths().size() == 7 &&
                        !(lastEvaluation == Evaluator.WON || lastEvaluation == Evaluator.LOST)) {

                    Optional<String> evals = searchResult.getSearchResultByDepths()
                            .stream()
                            .map(SearchResultByDepth::getBestEvaluation)
                            .map(eval -> String.format("%10d", eval))
                            .reduce((a, b) -> a + ", " + b);


                    writer.printf("%s%n", evals.orElse(""));
                }
            }
        } catch (java.io.IOException e) {
            log.error("Error writing to file", e);
        }
    }

    private List<SearchResult> searchesWhite(List<MatchResult> matchResults) {
        return matchResults.stream()
                .map(MatchResult::whiteSearches)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(SearchByTreeResult.class::isInstance)
                .map(SearchByTreeResult.class::cast)
                .map(SearchByTreeResult::searchResult)
                .toList();
    }

    private List<SearchResult> searchesBlack(List<MatchResult> matchResults) {
        return matchResults.stream()
                .map(MatchResult::blackSearches)
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .filter(SearchByTreeResult.class::isInstance)
                .map(SearchByTreeResult.class::cast)
                .map(SearchByTreeResult::searchResult)
                .toList();
    }
}
