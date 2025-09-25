package net.chesstango.arena.core.listeners;

import net.chesstango.board.Game;
import net.chesstango.board.moves.Move;
import net.chesstango.arena.core.MatchResult;
import net.chesstango.uci.gui.Controller;

/**
 * @author Mauricio Coria
 */
public interface MatchListener {
    void notifyNewGame(Game game, Controller white, Controller black);

    void notifyMove(Game game, Move move);

    void notifyEndGame(Game game, MatchResult matchResult);
}
