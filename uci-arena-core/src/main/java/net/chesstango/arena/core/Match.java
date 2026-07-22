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
import net.chesstango.engine.SearchByTreeResult;
import net.chesstango.engine.SearchResponse;
import net.chesstango.engine.Session;
import net.chesstango.gardel.epd.EPD;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.gardel.pgn.PGNMove;
import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.search.SearchResult;
import net.chesstango.uci.engine.UciTango;
import net.chesstango.uci.gui.Controller;
import net.chesstango.uci.gui.ControllerVisitor;
import net.chesstango.uci.proxy.UciProxy;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static net.chesstango.gardel.pgn.PGNMove.*;

/**
 * @author Mauricio Coria
 */
@Slf4j
public final class Match {
    private final Controller white;
    private final Controller black;
    private final MatchType matchType;
    private final PGN pgnMatch;
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

    private List<Clocks> clocks;

    public Match(Controller white, Controller black, MatchType matchType, PGN pgn) {
        this.white = white;
        this.black = black;
        this.matchType = matchType;
        this.pgnMatch = pgn;
    }

    public MatchResult play() {
        return play(UUID.randomUUID().toString());
    }

    public MatchResult play(String mathId) {
        try {

            this.mathId = mathId;

            this.game = Game.from(pgnMatch);

            this.clocks = new ArrayList<>();

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
                Instant start = Instant.now();

                String moveStr = retrieveBestMove(currentController, startPosition, executedMovesStr);

                Duration elapsedTime = Duration.between(start, Instant.now());

                Duration timeRemaining = matchType.getTimeRemaining(currentController == white);

                clocks.add(new Clocks(elapsedTime, timeRemaining));

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

        List<SearchResponse> whiteSearches = visitEngineController(white);

        List<SearchResponse> blackSearches = visitEngineController(black);

        attachEvaluations(pgnGame, whiteSearches, blackSearches);

        attachClocks(pgnGame);

        if (debug) {
            printDebug(pgnGame, System.out);
        }

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

    private void printDebug(PGN pgnGame, PrintStream printStream) {
        printStream.println(pgnGame);

        printStream.println("--------------------------------------------------------------------------------");

        printMoveExecution(printStream);

        printStream.println("--------------------------------------------------------------------------------");

        printEPDExecution(pgnGame, printStream);

        printStream.println("--------------------------------------------------------------------------------");
    }

    private PGN createPGN() {
        PGN pgnGame = game.toPGN();
        pgnGame.setEvent(mathId);
        pgnGame.setSite(getComputerName());
        pgnGame.setDate(getToday());
        pgnGame.setWhite(white.getEngineName());
        pgnGame.setBlack(black.getEngineName());

        int searchFrom = pgnMatch.getPgnMoves().size();
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

    private void attachEvaluations(PGN pgnGame, List<SearchResponse> whiteSearches, List<SearchResponse> blackSearches) {
        final int searchFrom = pgnMatch.getPgnMoves().size();
        boolean whiteTurn = pgnMatch.getFen() == null || "w".equals(pgnMatch.getFen().getActiveColor());
        int pgnMoveCounter = 0;
        int whiteMoveCounter = 0;
        int blackMoveCounter = 0;
        for (PGNMove pgnMove : pgnGame.getPgnMoves()) {
            if (pgnMoveCounter >= searchFrom) {
                SearchResponse searchResponse = null;

                if (whiteTurn && whiteSearches != null && whiteMoveCounter < whiteSearches.size()) {
                    searchResponse = whiteSearches.get(whiteMoveCounter);
                    whiteMoveCounter++;
                }

                if (!whiteTurn && blackSearches != null && blackMoveCounter < blackSearches.size()) {
                    searchResponse = blackSearches.get(blackMoveCounter);
                    blackMoveCounter++;
                }

                if (searchResponse instanceof SearchByTreeResult searchByTreeResult) {
                    SearchResult searchResult = searchByTreeResult.searchResult();
                    Integer evaluation = searchResult.getBestEvaluation();
                    pgnMove.putCommand(EVAL_COMMAND, evaluation == null ? "" : evaluation.toString());
                }
            }
            pgnMoveCounter++;
            whiteTurn = !whiteTurn;
        }
    }

    private void attachClocks(PGN pgnGame) {
        final int searchFrom = pgnMatch.getPgnMoves().size();
        int pgnMoveCounter = 0;
        int moveCounter = 0;
        for (PGNMove pgnMove : pgnGame.getPgnMoves()) {
            if (pgnMoveCounter >= searchFrom) {
                Clocks moveClocks = clocks.get(moveCounter);

                if (moveClocks.elapsedTime() != null) {
                    Duration elapsedTime = moveClocks.elapsedTime();
                    String elapsedTimeStr = String.format("%02d:%02d:%02d",
                            elapsedTime.toHoursPart(),
                            elapsedTime.toMinutesPart(),
                            elapsedTime.toSecondsPart()
                    );
                    pgnMove.putCommand(ELAPSED_MOVE_TIME_COMMAND, elapsedTimeStr);
                }

                if (moveClocks.timeRemaining() != null) {
                    Duration timeRemaining = moveClocks.timeRemaining();
                    String timeRemainingStr = String.format("%02d:%02d:%02d",
                            timeRemaining.toHoursPart(),
                            timeRemaining.toMinutesPart(),
                            timeRemaining.toSecondsPart()
                    );
                    pgnMove.putCommand(CLOCK_COMMAND, timeRemainingStr);
                }

                moveCounter++;
            }
            pgnMoveCounter++;
        }
    }

    private void printMoveExecution(PrintStream printStream) {
        GameDebugEncoder encoder = new GameDebugEncoder();

        printStream.println(encoder.encode(game));
    }

    private void printEPDExecution(PGN pgnGame, PrintStream printStream) {
        pgnGame.toEPD()
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

    private static String getComputerName() {
        Map<String, String> env = System.getenv();
        if (env.containsKey("COMPUTERNAME"))
            return env.get("COMPUTERNAME");
        else return env.getOrDefault("HOSTNAME", "Unknown Computer");
    }

    private String getToday() {
        String pattern = "yyyy.MM.dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }

    record Clocks(Duration elapsedTime, Duration timeRemaining) {
    }
}
