package net.chesstango.uci.gui;

import net.chesstango.uci.proxy.UciProxy;

/**
 * @author Mauricio Coria
 */
public class ControllerProxy extends ControllerAbstract {
    private final UciProxy uciProxy;

    public ControllerProxy(UciProxy uciProxy) {
        super(uciProxy);
        this.uciProxy = uciProxy;
    }

    @Override
    public void accept(ControllerVisitor controllerVisitor) {
        controllerVisitor.visit(uciProxy);
    }
}
