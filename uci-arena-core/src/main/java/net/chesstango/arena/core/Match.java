package net.chesstango.arena.core;

import lombok.AccessLevel;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.core.listeners.MatchListener;
import net.chesstango.arena.core.matchtypes.MatchTimeOut;
import net.chesstango.arena.core.matchtypes.MatchType;
import net.chesstango.board.Color;
import net.chesstango.board.Game;
import net.chesstango.board.Status;
import net.chesstango.board.moves.Move;
import net.chesstango.board.representations.GameDebugEncoder;
import net.chesstango.board.representations.move.SimpleMoveDecoder;
import net.chesstango.engine.SearchResponse;
import net.chesstango.engine.Session;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.uci.engine.UciTango;
import net.chesstango.uci.gui.Controller;
import net.chesstango.uci.gui.ControllerVisitor;
import net.chesstango.uci.proxy.UciProxy;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Mauricio Coria
 */
@Slf4j
public final class Match {
    private final Controller white;
    private final Controller black;
    private final MatchType matchType;
    private final Game game;
    private final SimpleMoveDecoder simpleMoveDecoder = new SimpleMoveDecoder();

    @Setter(AccessLevel.PACKAGE)
    private MatchTimeOut matchTimeOut;

    @Setter(AccessLevel.PACKAGE)
    private MatchResult matchResult;

    @Setter
    @Accessors(chain = true)
    private boolean printPGN;

    @Setter
    @Accessors(chain = true)
    private MatchListener matchListener;

    private String mathId;

    public Match(Controller white, Controller black, MatchType matchType, PGN pgn) {
        this.white = white;
        this.black = black;
        this.matchType = matchType;
        this.game = Game.from(pgn);
    }

    public MatchResult play() {
        return play(UUID.randomUUID().toString());
    }

    public MatchResult play(String mathId) {
        try {

            this.mathId = mathId;

            startNewGame();

            compete();

            return matchResult;

        } catch (RuntimeException e) {
            e.printStackTrace(System.err);

            log.error("Error playing game: {}", mathId);

            log.error("PGN: {}", createPGN());

            throw e;
        }
    }


    void compete() {
        log.info("[{}] WHITE={} BLACK={}", mathId, white.getEngineName(), black.getEngineName());

        final List<String> executedMovesStr = new ArrayList<>();

        Controller currentTurn = Color.WHITE.equals(game.getPosition().getCurrentTurn()) ? white : black;

        if (matchListener != null) {
            matchListener.notifyNewGame(game, white, black);
        }

        // Reset MatchType
        matchType.reset();

        try {

            FEN startPosition = game.getInitialFEN();

            while (game.getStatus().isInProgress()) {
                String moveStr = retrieveBestMove(currentTurn, startPosition, executedMovesStr);

                Move move = simpleMoveDecoder.decode(game.getPossibleMoves(), moveStr);

                if (move == null) {
                    printGameForDebug(System.err);
                    throw new RuntimeException(String.format("No move found %s", moveStr));
                }

                log.trace("[{}] {} plays move {}", mathId, currentTurn.getEngineName(), moveStr);

                move.executeMove();

                executedMovesStr.add(moveStr);

                currentTurn = (currentTurn == white ? black : white);

                if (matchListener != null) {
                    matchListener.notifyMove(game, move);
                }
            }

        } catch (MatchTimeOut e) {
            setMatchTimeOut(e);
        }

        matchResult = createResult();

        if (matchListener != null) {
            matchListener.notifyEndGame(game, matchResult);
        }
    }


    MatchResult createResult() {
        if (Status.DRAW_BY_FOLD_REPETITION.equals(game.getStatus())) {
            log.info("[{}] DRAW (por fold repetition)", mathId);

        } else if (Status.DRAW_BY_FIFTY_RULE.equals(game.getStatus())) {
            log.info("[{}] DRAW (por fold fiftyMoveRule)", mathId);

        } else if (Status.STALEMATE.equals(game.getStatus())) {
            log.info("[{}] DRAW", mathId);

        } else if (Status.MATE.equals(game.getStatus())) {
            if (Color.WHITE.equals(game.getPosition().getCurrentTurn())) {
                log.info("[{}] BLACK WON {}", mathId, black.getEngineName());
            } else {
                log.info("[{}] WHITE WON {}", mathId, white.getEngineName());
            }
        } else if (matchTimeOut != null) {
            // Loser controller
            if (matchTimeOut.getController() == white) {
                log.info("[{}] BLACK WON BY TIME OUT {}", mathId, black.getEngineName());
            } else {
                log.info("[{}] WHITE WON BY TIME OUT {}", mathId, white.getEngineName());
            }
        } else {
            printGameForDebug(System.err);
            throw new RuntimeException("Game is still in progress.");
        }

        if (printPGN) {
            printGameForDebug(System.out);
        }

        return new MatchResult(createPGN(), visitEngineController(white), visitEngineController(black));
    }


    private void startNewGame() {
        white.startNewGame();
        black.startNewGame();
    }

    private String retrieveBestMove(Controller currentTurn, FEN startPosition, List<String> moves) {
        if (FEN.START_POSITION.equals(startPosition)) {
            currentTurn.send_ReqPosition(UCIRequest.position(moves));
        } else {
            currentTurn.send_ReqPosition(UCIRequest.position(startPosition.toString(), moves));
        }

        RspBestMove bestMove = matchType.retrieveBestMove(currentTurn, currentTurn == white);

        return bestMove.getBestMove();
    }

    private void printGameForDebug(PrintStream printStream) {
        printStream.println(createPGN());

        printStream.println();

        printMoveExecution(printStream);

        printStream.println("--------------------------------------------------------------------------------");
    }

    private void printMoveExecution(PrintStream printStream) {
        GameDebugEncoder encoder = new GameDebugEncoder();

        printStream.println(encoder.encode(game));
    }

    private PGN createPGN() {
        PGN pgn = game.encode();
        pgn.setEvent(mathId);
        pgn.setWhite(white.getEngineName());
        pgn.setBlack(black.getEngineName());

        if (matchTimeOut != null) {
            Controller winner = matchTimeOut.getController() == white ? black : white;
            pgn.setResult(winner == white ? PGN.Result.WHITE_WINS : PGN.Result.BLACK_WINS);
            pgn.setTermination(PGN.Termination.TIME_FORFEIT);
        } else {
            pgn.setTermination(PGN.Termination.NORMAL);
        }

        return pgn;
    }

    /**
     * Extracts search results from controller's active session
     */
    private static List<SearchResponse> visitEngineController(Controller controller) {
        AtomicReference<Session> sessionRef = new AtomicReference<>();

        controller.accept(new ControllerVisitor() {
            @Override
            public void visit(UciTango uciTango) {
                sessionRef.set(uciTango.getSession());
            }

            @Override
            public void visit(UciProxy uciProxy) {
            }
        });

        Session session = sessionRef.get();

        return session != null ? session.getSearchResults() : null;
    }
}
