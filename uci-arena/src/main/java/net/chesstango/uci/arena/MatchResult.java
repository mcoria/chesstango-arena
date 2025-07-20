package net.chesstango.uci.arena;

import lombok.Getter;
import net.chesstango.engine.Session;
import net.chesstango.engine.Tango;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.uci.gui.Controller;
import net.chesstango.uci.gui.ControllerVisitor;
import net.chesstango.uci.proxy.UciProxy;
import net.chesstango.uci.engine.UciTango;

import java.util.function.Consumer;

/**
 * @author Mauricio Coria
 */
@Getter
public class MatchResult {
    private final String mathId;
    private final PGN pgn;
    private final Controller engineWhite;
    private final Controller engineBlack;
    private final Controller winner;

    private Session sessionWhite;
    private Session sessionBlack;

    public MatchResult(String mathId, PGN pgn, Controller engineWhite, Controller engineBlack, Controller winner) {
        this.mathId = mathId;
        this.pgn = pgn;
        this.engineWhite = engineWhite;
        this.engineBlack = engineBlack;
        this.winner = winner;
        this.sessionWhite = null;
        this.sessionBlack = null;

        discoverEngineController(engineWhite, session -> this.sessionWhite = session);
        discoverEngineController(engineBlack, session -> this.sessionBlack = session);
    }

    private static void discoverEngineController(Controller controller, Consumer<Session> sessionSetter) {
        controller.accept(new ControllerVisitor() {

            @Override
            public void visit(UciTango uciTango) {
                Tango tango = uciTango.getTango();
                sessionSetter.accept(tango.getCurrentSession());
            }

            @Override
            public void visit(UciProxy uciProxy) {
            }
        });
    }
}
