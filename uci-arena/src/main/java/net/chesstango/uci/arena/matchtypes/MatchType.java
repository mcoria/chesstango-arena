package net.chesstango.uci.arena.matchtypes;

import net.chesstango.uci.gui.Controller;
import net.chesstango.goyeneche.responses.RspBestMove;

/**
 * @author Mauricio Coria
 */
public interface MatchType {
    
    RspBestMove retrieveBestMoveFromController(Controller currentTurn, boolean isWhite);
}
