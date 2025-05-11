package net.chesstango.uci.gui;


import net.chesstango.goyeneche.requests.ReqGo;
import net.chesstango.goyeneche.requests.ReqPosition;
import net.chesstango.goyeneche.responses.RspBestMove;

/**
 * @author Mauricio Coria
 */
public interface Controller {

    void open();

    void close();

    void send_ReqUci();

    void send_ReqIsReady();

    void send_ReqUciNewGame();

    void send_ReqPosition(ReqPosition ReqPosition);

    RspBestMove send_ReqGo(ReqGo ReqGo);

    void send_ReqStop();

    void send_ReqQuit();

    default void startEngine() {
        open();
        send_ReqUci();
        send_ReqIsReady();
    }

    default void stopEngine() {
        send_ReqQuit();
        close();
    }

    default void startNewGame() {
        send_ReqUciNewGame();
        send_ReqIsReady();
    }

    String getEngineName();

    String getEngineAuthor();

    Controller overrideEngineName(String name);

    Controller overrideReqGo(ReqGo ReqGo);

    void accept(ControllerVisitor controllerVisitor);
}
