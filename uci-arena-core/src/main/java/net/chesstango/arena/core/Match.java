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
import net.chesstango.gardel.epd.EPD;
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
    private final PGN pgn;
    private final SimpleMoveDecoder simpleMoveDecoder = new SimpleMoveDecoder();

    @Setter
    @Accessors(chain = true)
    private boolean debug;

    @Setter
    @Accessors(chain = true)
    private MatchListener matchListener;

    @Setter(AccessLevel.PACKAGE)
    @Accessors(chain = true)
    private MatchTimeOut matchTimeOut;

    @Setter(AccessLevel.PACKAGE)
    @Accessors(chain = true)
    private MatchResult matchResult;

    @Setter(AccessLevel.PACKAGE)
    @Accessors(chain = true)
    private String mathId;

    @Setter(AccessLevel.PACKAGE)
    @Accessors(chain = true)
    private Game game;

    public Match(Controller white, Controller black, MatchType matchType, PGN pgn) {
        this.white = white;
        this.black = black;
        this.matchType = matchType;
        this.pgn = pgn;
    }

    public MatchResult play() {
        return play(UUID.randomUUID().toString());
    }

    public MatchResult play(String mathId) {
        try {

            this.mathId = mathId;

            this.game = Game.from(pgn);

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

        Controller currentController = Color.WHITE.equals(game.getPosition().getCurrentTurn()) ? white : black;

        if (matchListener != null) {
            matchListener.notifyNewGame(game, white, black);
        }

        // Reset MatchType
        matchType.reset();

        final FEN startPosition = game.getInitialFEN();

        final List<String> executedMovesStr = new ArrayList<>();

        game.getHistory()
                .iteratorReverse()
                .forEachRemaining(gameHistoryRecord -> {
                    executedMovesStr.add(gameHistoryRecord.playedMove().coordinateEncoding());
                });

        try {
            while (game.getStatus().isInProgress()) {
                String moveStr = retrieveBestMove(currentController, startPosition, executedMovesStr);

                Move move = simpleMoveDecoder.decode(game.getPossibleMoves(), moveStr);

                if (move == null) {
                    printDebug(createPGN(), System.err);
                    throw new RuntimeException(String.format("No move found %s", moveStr));
                }

                log.trace("[{}] {} plays move {}", mathId, currentController.getEngineName(), moveStr);

                move.executeMove();

                executedMovesStr.add(moveStr);

                currentController = (currentController == white ? black : white);

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
            printDebug(createPGN(), System.err);
            throw new RuntimeException("Game is still in progress.");
        }

        PGN pgnGame = createPGN();

        if (debug) {
            printDebug(pgnGame, System.out);
        }

        List<SearchResponse> whiteSearches = visitEngineController(white);

        List<SearchResponse> blackSearches = visitEngineController(black);

        return new MatchResult(pgnGame, whiteSearches, blackSearches);
    }


    private void startNewGame() {
        white.startNewGame();
        black.startNewGame();
    }

    private String retrieveBestMove(Controller controller, FEN startPosition, List<String> moves) {
        if (FEN.START_POSITION.equals(startPosition)) {
            controller.send_ReqPosition(UCIRequest.position(moves));
        } else {
            controller.send_ReqPosition(UCIRequest.position(startPosition.toString(), moves));
        }

        RspBestMove bestMove = matchType.requestBestMove(controller, controller == white);

        return bestMove.getBestMove();
    }

    private void printDebug(PGN pgn, PrintStream printStream) {
        printStream.println(pgn);

        printStream.println("--------------------------------------------------------------------------------");

        printMoveExecution(printStream);

        printStream.println("--------------------------------------------------------------------------------");

        printEPDExecution(pgn, printStream);

        printStream.println("--------------------------------------------------------------------------------");
    }

    private PGN createPGN() {
        PGN pgnGame = game.toPGN();
        pgnGame.setEvent(mathId);
        pgnGame.setWhite(white.getEngineName());
        pgnGame.setBlack(black.getEngineName());

        int searchFrom = pgn.getPgnMoves().size();
        pgnGame.setTag("ArenaSearch", Integer.toString(searchFrom));

        if (matchTimeOut != null) {
            Controller winner = matchTimeOut.getController() == white ? black : white;
            pgnGame.setResult(winner == white ? PGN.Result.WHITE_WINS : PGN.Result.BLACK_WINS);
            pgnGame.setTermination(PGN.Termination.TIME_FORFEIT);
        } else {
            pgnGame.setTermination(PGN.Termination.NORMAL);
        }

        return pgnGame;
    }

    private void addEvaluation() {
        /*
        final boolean whiteMovesFirst = pgn.getFen() == null || "w".equals(pgn.getFen().getActiveColor());
        int i = 0;
        for (PGNMove pgnMove : pgnResult.getPgnMoves()) {
            if (i >= searchFrom) {
                // White turn first
                if (whiteMovesFirst) {

                }
            }
            i++;
        }
         */
    }

    private void printMoveExecution(PrintStream printStream) {
        GameDebugEncoder encoder = new GameDebugEncoder();

        printStream.println(encoder.encode(game));
    }

    private void printEPDExecution(PGN pgn, PrintStream printStream) {
        pgn.toEPD()
                .map(EPD::toString)
                .forEach(printStream::println);
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
