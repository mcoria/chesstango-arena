package net.chesstango.uci.arena;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.board.Color;
import net.chesstango.board.Game;
import net.chesstango.board.Status;
import net.chesstango.board.moves.Move;
import net.chesstango.board.representations.GameDebugEncoder;
import net.chesstango.board.representations.move.SimpleMoveDecoder;
import net.chesstango.engine.Session;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.fen.FENParser;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.search.SearchResult;
import net.chesstango.uci.arena.listeners.MatchListener;
import net.chesstango.uci.arena.matchtypes.MatchType;
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
    private final FEN fen;
    private final SimpleMoveDecoder simpleMoveDecoder = new SimpleMoveDecoder();

    private Game game;
    private MatchResult matchResult;

    @Setter
    @Accessors(chain = true)
    private boolean printPGN;

    @Setter
    @Accessors(chain = true)
    private MatchListener matchListener;

    private String mathId;

    public Match(Controller white, Controller black, FEN fen, MatchType matchType) {
        this.white = white;
        this.black = black;
        this.fen = fen;
        this.matchType = matchType;
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

            log.error("Error playing fen: {}", fen);

            log.error("PGN: {}", createPGN());

            throw e;
        }
    }


    void compete() {
        log.info("[{}] WHITE={} BLACK={}", mathId, white.getEngineName(), black.getEngineName());

        setGame(Game.from(fen));

        final List<String> executedMovesStr = new ArrayList<>();

        Controller currentTurn;

        if (Color.WHITE.equals(game.getPosition().getCurrentTurn())) {
            currentTurn = white;
        } else {
            currentTurn = black;
        }

        if (matchListener != null) {
            matchListener.notifyNewGame(game, white, black);
        }

        while (game.getStatus().isInProgress()) {

            String moveStr = retrieveBestMoveFromController(currentTurn, executedMovesStr);

            Move move = simpleMoveDecoder.decode(game.getPossibleMoves(), moveStr);

            if (move == null) {
                printGameForDebug(System.err);
                throw new RuntimeException(String.format("No move found %s", moveStr));
            }

            move.executeMove();

            executedMovesStr.add(moveStr);

            currentTurn = (currentTurn == white ? black : white);

            if (matchListener != null) {
                matchListener.notifyMove(game, move);
            }
        }

        matchResult = createResult();

        if (matchListener != null) {
            matchListener.notifyEndGame(game, matchResult);
        }
    }

    void setGame(Game game) {
        this.game = game;
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
            } else if (Color.BLACK.equals(game.getPosition().getCurrentTurn())) {
                log.info("[{}] WHITE WON {}", mathId, white.getEngineName());
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

    private String retrieveBestMoveFromController(Controller currentTurn, List<String> moves) {
        if (FEN.of(FENParser.INITIAL_FEN).equals(fen)) {
            currentTurn.send_ReqPosition(UCIRequest.position(moves));
        } else {
            currentTurn.send_ReqPosition(UCIRequest.position(fen.toString(), moves));
        }

        RspBestMove bestMove = matchType.retrieveBestMoveFromController(currentTurn, currentTurn == white);

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
        return pgn;
    }

    private static List<SearchResult> visitEngineController(Controller controller) {
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

        if (session != null) {
            return session.getSearches();
        }

        return null;
    }
}
