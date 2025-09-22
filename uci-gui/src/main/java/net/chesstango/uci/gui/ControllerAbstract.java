package net.chesstango.uci.gui;


import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.goyeneche.UCIGui;
import net.chesstango.goyeneche.UCIService;
import net.chesstango.goyeneche.requests.ReqGo;
import net.chesstango.goyeneche.requests.ReqPosition;
import net.chesstango.goyeneche.requests.ReqSetOption;
import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.*;
import net.chesstango.goyeneche.stream.UCIOutputStreamGuiExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Mauricio Coria
 */
@Slf4j
public abstract class ControllerAbstract implements Controller {

    private final UCIService service;

    private volatile UCIGui currentState;
    private volatile UCIResponse response;

    @Setter
    private String engineName;

    @Setter
    private String engineAuthor;

    private ReqGo cmdGo;

    private List<ReqSetOption> reqSetOptions;

    public ControllerAbstract(UCIService service) {
        UCIGui messageExecutor = new UCIGui() {
            @Override
            public void do_uciOk(RspUciOk rspUciOk) {
                currentState.do_uciOk(rspUciOk);
            }

            @Override
            public void do_option(RspOption rspOption) {
                currentState.do_option(rspOption);
            }

            @Override
            public void do_readyOk(RspReadyOk rspReadyOk) {
                currentState.do_readyOk(rspReadyOk);
            }

            @Override
            public void do_bestMove(RspBestMove rspBestMove) {
                currentState.do_bestMove(rspBestMove);
            }

            @Override
            public void do_info(RspInfo rspInfo) {
                currentState.do_info(rspInfo);
            }

            @Override
            public void do_id(RspId rspId) {
                currentState.do_id(rspId);
            }
        };

        this.service = service;
        this.service.setOutputStream(new UCIOutputStreamGuiExecutor(messageExecutor));
    }

    @Override
    public void open() {
        service.open();
    }

    @Override
    public void close() {
        service.close();
    }

    @Override
    public void send_ReqUci() {
        sendRequestWaitResponse(new StateWaitRspUciOk(this), UCIRequest.uci());
    }

    @Override
    public void send_ReqIsReady() {
        sendRequestWaitResponse(new StateWaitRspReadyOk(this), UCIRequest.isready());
    }

    @Override
    public void send_ReqUciNewGame() {
        sendRequestNoWaitResponse(UCIRequest.ucinewgame());
    }

    @Override
    public void send_ReqPosition(ReqPosition cmdPosition) {
        sendRequestNoWaitResponse(cmdPosition);
    }

    @Override
    public RspBestMove send_ReqGo(ReqGo cmdGo) {
        return (RspBestMove) sendRequestWaitResponse(new StateWaitRspBestMove(this), this.cmdGo == null ? cmdGo : this.cmdGo);
    }

    @Override
    public void send_ReqStop() {
        sendRequestNoWaitResponse(UCIRequest.stop());
    }

    @Override
    public void send_ReqQuit() {
        sendRequestNoWaitResponse(UCIRequest.quit());
    }

    @Override
    public String getEngineName() {
        return engineName;
    }

    @Override
    public String getEngineAuthor() {
        return engineAuthor;
    }

    @Override
    public Controller overrideEngineName(String name) {
        this.engineName = name;
        return this;
    }

    @Override
    public Controller overrideReqGo(ReqGo cmdGo) {
        this.cmdGo = cmdGo;
        return this;
    }

    @Override
    public void send_ReqOptions() {
        if (reqSetOptions != null) {
            reqSetOptions.forEach(this::sendRequestNoWaitResponse);
        }
    }

    public Controller setOptionsCommands(List<ReqSetOption> reqSetOptions) {
        this.reqSetOptions = reqSetOptions;
        return this;
    }

    public synchronized void sendRequestNoWaitResponse(UCIRequest request) {
        this.response = null;
        this.currentState = new StateNoWaitRsp();
        service.accept(request);
    }

    public synchronized UCIResponse sendRequestWaitResponse(UCIGui newState, UCIRequest request) {
        this.response = null;
        this.currentState = newState;
        service.accept(request);
        try {
            /**
             * Luego de service.accept(request), el mismo thread puede llamar a responseReceived y no bloquearse.
             * Por lo tanto esperamos solo si todavia no recibimos resuesta.
             */
            if (response == null) {
                wait(20000);
            }
            if (response == null) {
                log.error("Engine {} has not provided any response after sending: {}", engineName, request);
                throw new RuntimeException("Perhaps engine has closed its output");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    public synchronized void responseReceived(UCIResponse response) {
        this.currentState = new StateNoWaitRsp();
        this.response = response;
        notifyAll();
    }

}
