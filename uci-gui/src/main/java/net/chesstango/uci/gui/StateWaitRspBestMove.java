package net.chesstango.uci.gui;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.goyeneche.UCIGui;
import net.chesstango.goyeneche.responses.RspBestMove;
import net.chesstango.goyeneche.responses.RspInfo;

/**
 * @author Mauricio Coria
 */
@Slf4j
class StateWaitRspBestMove implements UCIGui {
    private final ControllerAbstract controllerAbstract;

    StateWaitRspBestMove(ControllerAbstract controllerAbstract) {
        this.controllerAbstract = controllerAbstract;
    }


    @Override
    public void do_bestMove(RspBestMove rspBestMove) {
        controllerAbstract.responseReceived(rspBestMove);
    }

    @Override
    public void do_info(RspInfo rspInfo) {
        controllerAbstract.rspInfoReceived(rspInfo);
    }
}
