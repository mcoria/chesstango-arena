package net.chesstango.arena.core.matchtypes;

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
    private final int time;
    private final int inc;


    // Current timers
    private int wTime;
    private int bTime;

    public MatchByClock(int time, int inc) {
        this.time = time;
        this.inc = inc;
    }


    @Override
    public void reset() {
        this.wTime = time;
        this.bTime = time;
    }

    @Override
    public RspBestMove retrieveBestMove(Controller controller, boolean whiteTurn) {
        ReqGoFast goCmd = UCIRequest.goFast(wTime, inc, bTime, inc);

        Instant start = Instant.now();

        RspBestMove bestMove = controller.send_ReqGo(goCmd);

        long timeElapsed = Duration.between(start, Instant.now()).toMillis();

        if (whiteTurn) {
            wTime -= (int) timeElapsed;
            wTime += inc;
            if (wTime < 0) {
                throw new MatchTimeOut("White time out", controller);
            }
        } else {
            bTime -= (int) timeElapsed;
            bTime += inc;
            if (bTime < 0) {
                throw new MatchTimeOut("Black time out", controller);
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
