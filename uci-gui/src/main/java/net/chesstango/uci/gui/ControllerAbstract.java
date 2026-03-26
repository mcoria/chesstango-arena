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

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * @author Mauricio Coria
 */
@Slf4j
public abstract class ControllerAbstract implements Controller {

    private static final long DEFAULT_TIMEOUT = 120 * 1000;

    private final UCIService service;

    private volatile UCIGui currentState;

    private volatile UCIResponse response;


    long timeOut = DEFAULT_TIMEOUT;

    @Setter
    private String engineName;

    @Setter
    private String engineAuthor;

    private ReqGo cmdGo;

    private List<ReqSetOption> reqSetOptions;

    private Instant startTimeOutInstant;

    final UCIGui messageExecutor;


    public ControllerAbstract(UCIService service) {
        this.messageExecutor = new UCIGui() {
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
        this.startTimeOutInstant = Instant.now();
        log.trace("[{}] gui >> {}", engineName, request);
        service.accept(request);

        try {
            /**
             * Luego de service.accept(request), el mismo thread puede llamar a responseReceived y no bloquearse.
             * Por lo tanto esperamos solo si todavia no recibimos resuesta.
             */
            while (response == null) {
                wait(timeOut + 100);
                if (response == null) {
                    long timeDiff = Duration.between(startTimeOutInstant, Instant.now()).toMillis();
                    if (timeDiff > timeOut) {
                        throw new NoResponseException(String.format("Engine %s has not provided any response after sending: %s", engineName, request));
                    } else {
                        log.trace("[{}] gui: {} - No response yet", engineName, request);
                    }
                }
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        log.trace("[{}] gui << {}", engineName, response);

        return response;
    }

    synchronized void responseReceived(UCIResponse response) {
        this.currentState = new StateNoWaitRsp();
        this.response = response;
        notifyAll();
    }

    synchronized void rspInfoReceived(RspInfo rspInfo) {
        this.startTimeOutInstant = Instant.now();
        log.trace("[{}] gui << {}", engineName, rspInfo);
    }
}
