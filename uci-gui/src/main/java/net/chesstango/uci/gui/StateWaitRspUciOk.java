package net.chesstango.uci.gui;

import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.RspId;
import net.chesstango.goyeneche.responses.RspUciOk;
import net.chesstango.goyeneche.responses.UCIResponse;

/**
 * @author Mauricio Coria
 */
class StateWaitRspUciOk implements StateWaitRsp {
    private static final long TIME_OUT = 10000;

    private final ControllerAbstract controllerAbstract;

    private volatile UCIResponse response;

    StateWaitRspUciOk(ControllerAbstract controllerAbstract) {
        this.controllerAbstract = controllerAbstract;
    }

    @Override
    public synchronized void do_uciOk(RspUciOk rspUciOk) {
        response = rspUciOk;
        notifyAll();
    }


    @Override
    public void do_id(RspId rspId) {
        if (RspId.RspIdType.NAME.equals(rspId.getIdType()) && controllerAbstract.getEngineName() == null) {
            controllerAbstract.setEngineName(rspId.getText());
        }
        if (RspId.RspIdType.AUTHOR.equals(rspId.getIdType())) {
            controllerAbstract.setEngineAuthor(rspId.getText());
        }
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
