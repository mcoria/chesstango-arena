package net.chesstango.uci.gui;

import net.chesstango.uci.engine.UciTango;
import net.chesstango.uci.proxy.UciProxy;

/**
 * @author Mauricio Coria
 */
public interface ControllerVisitor {

    void visit(UciTango uciTango);

	void visit(UciProxy uciProxy);
}
