package net.chesstango.uci.gui;

import net.chesstango.goyeneche.UCIGui;
import net.chesstango.goyeneche.responses.*;

/**
 * @author Mauricio Coria
 */
class StateWaitRspBestMove implements UCIGui {
    private final ControllerAbstract controllerAbstract;

    StateWaitRspBestMove(ControllerAbstract controllerAbstract) {
        this.controllerAbstract = controllerAbstract;
    }


    @Override
    public void do_bestMove(RspBestMove rspBestMove) {
        controllerAbstract.responseReceived(rspBestMove);
    }
}
