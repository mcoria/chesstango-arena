package net.chesstango.uci.gui;

import net.chesstango.goyeneche.UCIGui;
import net.chesstango.goyeneche.responses.*;

/**
 * @author Mauricio Coria
 */
class StateWaitRspUciOk implements UCIGui {
    private final ControllerAbstract controllerAbstract;

    public StateWaitRspUciOk(ControllerAbstract controllerAbstract) {
        this.controllerAbstract = controllerAbstract;
    }

    @Override
    public void do_uciOk(RspUciOk rspUciOk) {
        controllerAbstract.responseReceived(rspUciOk);
    }

    @Override
    public void do_option(RspOption rspOption) {

    }

    @Override
    public void do_readyOk(RspReadyOk rspReadyOk) {
    }

    @Override
    public void do_bestMove(RspBestMove rspBestMove) {
    }

    @Override
    public void do_info(RspInfo rspInfo) {
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

}
