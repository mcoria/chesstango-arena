package net.chesstango.arena.core.matchtypes;

import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.uci.gui.Controller;

import java.io.Serializable;

/**
 * @author Mauricio Coria
 */
public interface MatchType extends Serializable {

    RspBestMove retrieveBestMoveFromController(Controller currentTurn, boolean isWhite);
}
