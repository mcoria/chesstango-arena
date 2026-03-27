package net.chesstango.uci.gui;

import net.chesstango.goyeneche.UCIGui;
import net.chesstango.goyeneche.requests.UCIRequest;
import net.chesstango.goyeneche.responses.UCIResponse;

/**
 * @author Mauricio Coria
 */
public interface StateWaitRsp extends UCIGui {


    UCIResponse waitResponse(UCIRequest request);

}
