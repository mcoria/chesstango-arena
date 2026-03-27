package net.chesstango.uci.gui;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.goyeneche.requests.*;
import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.goyeneche.responses.UCIResponse;

/**
 * @author Mauricio Coria
 */
@Slf4j
class StateWaitRspBestMove implements StateWaitRsp {
    private final ControllerAbstract controllerAbstract;

    private volatile UCIResponse response;

    StateWaitRspBestMove(ControllerAbstract controllerAbstract) {
        this.controllerAbstract = controllerAbstract;
    }


    @Override
    public synchronized void do_bestMove(RspBestMove rspBestMove) {
        response = rspBestMove;
        notifyAll();
    }

    @Override
    public synchronized UCIResponse waitResponse(UCIRequest request) {
        try {
            boolean stopSent = false;
            while (response == null) {
                if (stopSent) {
                    wait(1000);
                    if (response == null) {
                        throw new NoResponseException(String.format("Engine %s has not provided any response after sending: %s",
                                controllerAbstract.getEngineName(),
                                request));
                    }
                } else {
                    long timeOut = calcTimeOut((ReqGo) request);
                    wait(timeOut);
                    controllerAbstract.send_ReqStop();
                    stopSent = true;
                }
            }
            return response;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    long calcTimeOut(ReqGo cmdGo) {
        return switch (cmdGo) {
            case ReqGoInfinite reqGoInfinite -> 1000 * 60 * 10;     // 10 minutes
            case ReqGoTime reqGoTime -> reqGoTime.getTimeOut();
            case ReqGoDepth reqGoDepth -> 1000 * 60 * 2;            // 2 minutes
            case ReqGoFast reqGoFast -> Math.max(reqGoFast.getWTime(), reqGoFast.getBTime()) + 1000;
            default -> 1000;
        };
    }
}
