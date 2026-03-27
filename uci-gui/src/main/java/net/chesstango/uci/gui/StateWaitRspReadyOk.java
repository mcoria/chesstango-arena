package net.chesstango.uci.gui;

import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.RspReadyOk;
import net.chesstango.goyeneche.responses.UCIResponse;

/**
 * @author Mauricio Coria
 */
class StateWaitRspReadyOk implements StateWaitRsp {
    private static final long TIME_OUT = 10000;

    private final ControllerAbstract controllerAbstract;

    private volatile UCIResponse response;

    StateWaitRspReadyOk(ControllerAbstract controllerAbstract) {
        this.controllerAbstract = controllerAbstract;
    }

    @Override
    public synchronized void do_readyOk(RspReadyOk rspReadyOk) {
        response = rspReadyOk;
        notifyAll();
    }

    @Override
    public synchronized UCIResponse waitResponse(UCIRequest request) {
        try {
            if (response == null) {
                wait(TIME_OUT);
                if (response == null) {
                    throw new NoResponseException(String.format("Engine %s has not provided any response after sending: %s",
                            controllerAbstract.getEngineName(),
                            request));
                }
            }
            return response;
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
