package net.chesstango.arena.core.matchtypes;

import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.uci.gui.Controller;
import net.chesstango.goyeneche.requests.ReqGo;
import net.chesstango.goyeneche.responses.RspBestMove;

/**
 * @author Mauricio Coria
 */
public class MatchByDepth implements MatchType {
    public final ReqGo reqGo;

    public MatchByDepth(int depth) {
        this.reqGo = UCIRequest.goDepth(depth);
    }

    @Override
    public RspBestMove retrieveBestMoveFromController(Controller currentTurn, boolean isWhite) {
        return currentTurn.send_ReqGo(reqGo);
    }

    @Override
    public String toString() {
        return "MatchByDepth{" + "reqGo=" + reqGo + '}';
    }
}
