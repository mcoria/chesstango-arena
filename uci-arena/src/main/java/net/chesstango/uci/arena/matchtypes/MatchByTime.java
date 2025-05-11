package net.chesstango.uci.arena.matchtypes;

import net.chesstango.goyeneche.requests.ReqGo;
import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.uci.gui.Controller;

/**
 * @author Mauricio Coria
 */
public class MatchByTime implements MatchType {
    public final ReqGo ReqGo;

    public MatchByTime(int timeOut) {
        this.ReqGo = UCIRequest.goTime(timeOut);
    }

    @Override
    public RspBestMove retrieveBestMoveFromController(Controller currentTurn, boolean isWhite) {
        return currentTurn.send_ReqGo(ReqGo);
    }
}
