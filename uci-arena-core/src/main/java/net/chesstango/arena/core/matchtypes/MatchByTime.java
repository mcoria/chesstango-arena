package net.chesstango.arena.core.matchtypes;

import net.chesstango.goyeneche.requests.ReqGo;
import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.uci.gui.Controller;

/**
 * @author Mauricio Coria
 */
public class MatchByTime implements MatchType {
    public final ReqGo reqGo;

    public MatchByTime(int timeOut) {
        this.reqGo = UCIRequest.goTime(timeOut);
    }

    @Override
    public void reset() {

    }

    @Override
    public RspBestMove retrieveBestMoveFromController(Controller controller, boolean isWhite) {
        return controller.send_ReqGo(reqGo);
    }


    @Override
    public String toString() {
        return "MatchByTime{" + "reqGo=" + reqGo + '}';
    }
}
