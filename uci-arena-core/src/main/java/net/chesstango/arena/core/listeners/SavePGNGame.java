package net.chesstango.arena.core.listeners;

import net.chesstango.board.Game;
import net.chesstango.board.moves.Move;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.gardel.pgn.PGNStringEncoder;
import net.chesstango.arena.core.MatchResult;
import net.chesstango.uci.gui.Controller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Mauricio Coria
 */
public class SavePGNGame implements MatchListener {
    @Override
    public void notifyNewGame(Game game, Controller white, Controller black) {
    }

    @Override
    public void notifyMove(Game game, Move move) {
    }

    @Override
    public void notifyEndGame(Game game, MatchResult matchResult) {
        save(matchResult.pgn());
    }


    protected static synchronized void save(PGN pgn) {
        PGNStringEncoder encoder = new PGNStringEncoder();
        String encodedGame = encoder.encode(pgn);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("./matches.pgn", true));
            writer.append(encodedGame);
            writer.append("\n\n");
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
