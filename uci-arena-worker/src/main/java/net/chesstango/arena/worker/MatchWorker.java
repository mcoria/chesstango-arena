package net.chesstango.arena.worker;

import lombok.extern.slf4j.Slf4j;
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

        MatchResult result = getMatchResult(matchRequest, whiteController, blackController);

        // Sets engine names and match result attributes
        return new MatchResponse()
                .setWhiteEngineName(result.pgn().getWhite())
                .setBlackEngineName(result.pgn().getBlack())
                .setMatchResult(result)
                .setMatchId(matchRequest.getMatchId())
                .setSessionId(matchRequest.getSessionId());
    }

    private static MatchResult getMatchResult(MatchRequest matchRequest, Controller whiteController, Controller blackController) {
        Match match = null;

        if (matchRequest.getFen() != null) {
            match = new Match(whiteController, blackController, matchRequest.getMatchType(), matchRequest.getFen());
        } else if (matchRequest.getPgn() != null) {
            match = new Match(whiteController, blackController, matchRequest.getMatchType(), matchRequest.getPgn());
        } else {
            throw new IllegalArgumentException("FEN or PGN must be provided");
        }

        return match.play(matchRequest.getMatchId());
    }

}
