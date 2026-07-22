package net.chesstango.arena.core.matchtypes;

import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.uci.gui.Controller;

import java.io.Serializable;
import java.time.Duration;

/**
 * @author Mauricio Coria
 */
public interface MatchType extends Serializable {

    RspBestMove requestBestMove(Controller controller, boolean whiteTurn);

    void reset();

    Duration getTimeRemaining(boolean b);
}
