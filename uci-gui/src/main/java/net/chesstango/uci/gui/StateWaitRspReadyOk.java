package net.chesstango.uci.gui;

import net.chesstango.goyeneche.UCIGui;
import net.chesstango.goyeneche.responses.*;

/**
 * @author Mauricio Coria
 */
class StateWaitRspReadyOk implements UCIGui {
    private final ControllerAbstract controllerAbstract;

    public StateWaitRspReadyOk(ControllerAbstract controllerAbstract) {
        this.controllerAbstract = controllerAbstract;
    }

    @Override
    public void do_uciOk(RspUciOk rspUciOk) {
    }

    @Override
    public void do_option(RspOption rspOption) {

    }

    @Override
    public void do_readyOk(RspReadyOk rspReadyOk) {
        controllerAbstract.responseReceived(rspReadyOk);
    }

    @Override
    public void do_bestMove(RspBestMove rspBestMove) {
    }

    @Override
    public void do_info(RspInfo rspInfo) {
    }

    @Override
    public void do_id(RspId rspId) {
    }
}
