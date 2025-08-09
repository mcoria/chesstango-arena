package net.chesstango.uci.gui;


import net.chesstango.goyeneche.requests.ReqGo;
import net.chesstango.goyeneche.requests.ReqPosition;
import net.chesstango.goyeneche.responses.RspBestMove;

/**
 * Controller interface for managing UCI (Universal Chess Interface) chess engine interactions.
 * Provides methods to control engine lifecycle, send commands and receive responses.
 *
 * @author Mauricio Coria
 */
public interface Controller {

    /**
     * Opens the connection to the chess engine.
     */
    void open();

    /**
     * Closes the connection to the chess engine.
     */
    void close();

    /**
     * Sends UCI initialization request to the engine.
     */
    void send_ReqUci();

    /**
     * Sends isready request to verify engine is ready to accept commands.
     */
    void send_ReqIsReady();

    /**
     * Sends ucinewgame request to initialize a new game.
     */
    void send_ReqUciNewGame();

    /**
     * Sends position request to set the current board position.
     *
     * @param ReqPosition The position request containing board state
     */
    void send_ReqPosition(ReqPosition ReqPosition);

    /**
     * Sends go request to start engine calculation and returns best move found.
     *
     * @param ReqGo The go request containing search parameters
     * @return Response containing the best move found by engine
     */
    RspBestMove send_ReqGo(ReqGo ReqGo);

    /**
     * Sends stop request to stop current calculation.
     */
    void send_ReqStop();

    /**
     * Sends quit request to terminate the engine.
     */
    void send_ReqQuit();

    /**
     * Starts the engine by opening connection and initializing UCI protocol.
     */
    default void startEngine() {
        open();
        send_ReqUci();
        send_ReqOptions();
        send_ReqIsReady();
    }

    default void send_ReqOptions() {
    }

    /**
     * Stops the engine by sending quit command and closing connection.
     */
    default void stopEngine() {
        send_ReqQuit();
        close();
    }

    /**
     * Initializes a new game by sending ucinewgame command.
     */
    default void startNewGame() {
        send_ReqUciNewGame();
        send_ReqIsReady();
    }

    /**
     * Gets the name of the chess engine.
     *
     * @return Engine name
     */
    String getEngineName();

    /**
     * Gets the author of the chess engine.
     *
     * @return Engine author
     */
    String getEngineAuthor();

    /**
     * Overrides the engine name.
     *
     * @param name New engine name to set
     * @return This controller instance
     */
    Controller overrideEngineName(String name);

    /**
     * Overrides the default go request parameters.
     *
     * @param ReqGo The go request to use as default
     * @return This controller instance
     */
    Controller overrideReqGo(ReqGo ReqGo);

    /**
     * Accepts a visitor for the controller.
     *
     * @param controllerVisitor The visitor to accept
     */
    void accept(ControllerVisitor controllerVisitor);
}
