package net.chesstango.uci.gui;

import net.chesstango.goyeneche.UCIGui;
import net.chesstango.goyeneche.responses.*;

/**
 * @author Mauricio Coria
 */
class StateWaitRspReadyOk implements UCIGui {
    private final ControllerAbstract controllerAbstract;

    StateWaitRspReadyOk(ControllerAbstract controllerAbstract) {
        this.controllerAbstract = controllerAbstract;
    }

    @Override
    public void do_readyOk(RspReadyOk rspReadyOk) {
        controllerAbstract.responseReceived(rspReadyOk);
    }
}
