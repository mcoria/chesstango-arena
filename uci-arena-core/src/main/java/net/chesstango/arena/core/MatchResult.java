package net.chesstango.arena.core;

import net.chesstango.gardel.pgn.PGN;
import net.chesstango.search.SearchResult;

import java.io.Serializable;
import java.util.List;

/**
 * @author Mauricio Coria
 */
public record MatchResult(PGN pgn, List<SearchResult> whiteSearches, List<SearchResult> blackSearches) implements Serializable {
}
