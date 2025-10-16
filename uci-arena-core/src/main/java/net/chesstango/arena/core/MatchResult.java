package net.chesstango.arena.core;

import net.chesstango.engine.SearchResponse;
import net.chesstango.gardel.pgn.PGN;

import java.io.Serializable;
import java.util.List;

/**
 * @author Mauricio Coria
 */
public record MatchResult(PGN pgn,
                          List<SearchResponse> whiteSearches,
                          List<SearchResponse> blackSearches) implements Serializable {
}
