package net.chesstango.uci.arena.listeners;

import net.chesstango.board.Game;
import net.chesstango.board.moves.Move;
import net.chesstango.uci.arena.MatchResult;
import net.chesstango.uci.gui.Controller;

/**
 * @author Mauricio Coria
 */
public interface MatchListener {
    void notifyNewGame(Game game, Controller white, Controller black);

    void notifyMove(Game game, Move move);

    void notifyEndGame(Game game, MatchResult matchResult);
}
