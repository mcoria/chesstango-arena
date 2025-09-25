package net.chesstango.arena.worker;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.arena.core.Match;
import net.chesstango.arena.core.MatchResult;
import net.chesstango.arena.core.matchtypes.MatchType;
import net.chesstango.uci.gui.Controller;

import java.util.function.Function;

/**
 * @author Mauricio Coria
 */
@Slf4j
class MatchWorker implements Function<MatchRequest, MatchResponse> {

    private final ControllerProvider controllerProvider;

    MatchWorker(ControllerProvider controllerProvider) {
        this.controllerProvider = controllerProvider;
    }

    @Override
    public MatchResponse apply(MatchRequest matchRequest) {
        Controller whiteController = controllerProvider.getController(matchRequest.getWhiteEngine());

        Controller blackController = controllerProvider.getController(matchRequest.getBlackEngine());

        MatchType matchType = matchRequest.getMatchType();

        FEN fen = matchRequest.getFen();

        Match match = new Match(whiteController, blackController, fen, matchType);

        MatchResult result = match.play(matchRequest.getMatchId());

        return new MatchResponse()
                .setWhiteEngineName(result.pgn().getWhite())
                .setBlackEngineName(result.pgn().getBlack())
                .setMatchResult(result)
                .setMatchId(matchRequest.getMatchId())
                .setSessionId(matchRequest.getSessionId());
    }

}
