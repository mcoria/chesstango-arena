package net.chesstango.uci.arena.matchtypes;

import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.uci.gui.Controller;
import net.chesstango.goyeneche.requests.ReqGo;
import net.chesstango.goyeneche.requests.ReqGoDepth;
import net.chesstango.goyeneche.responses.RspBestMove;

/**
 * @author Mauricio Coria
 */
public class MatchByDepth implements MatchType {
    public final ReqGo ReqGo;

    public MatchByDepth(int depth) {
        this.ReqGo = UCIRequest.goDepth(depth);
    }

    @Override
    public RspBestMove retrieveBestMoveFromController(Controller currentTurn, boolean isWhite) {
        return currentTurn.send_ReqGo(ReqGo);
    }
}
