package net.chesstango.uci.arena.matchtypes;

import net.chesstango.goyeneche.requests.ReqGoFast;
import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.uci.gui.Controller;

import java.time.Duration;
import java.time.Instant;

/**
 * @author Mauricio Coria
 */
public class MatchByClock implements MatchType {
    private final int inc;
    private int wTime;
    private int bTime;

    public MatchByClock(int time, int inc) {
        this.wTime = time;
        this.bTime = time;
        this.inc = inc;
    }


    @Override
    public RspBestMove retrieveBestMoveFromController(Controller currentTurn, boolean isWhite) {
        ReqGoFast goCmd = UCIRequest.goFast(wTime, bTime, inc, inc);

        Instant start = Instant.now();

        RspBestMove bestMove = currentTurn.send_ReqGo(goCmd);

        long timeElapsed = Duration.between(start, Instant.now()).toMillis();

        if (isWhite) {
            wTime -= (int) timeElapsed;
            wTime += inc;
            if (wTime < 0) {
                throw new RuntimeException("White time out");
            }
        } else {
            bTime -= (int) timeElapsed;
            bTime += inc;
            if (bTime < 0) {
                throw new RuntimeException("Black time out");
            }
        }

        return bestMove;
    }

    @Override
    public String toString() {
        return "MatchByClock{" +
                "wTime=" + wTime +
                ", bTime=" + bTime +
                ", inc=" + inc +
                '}';
    }

}
