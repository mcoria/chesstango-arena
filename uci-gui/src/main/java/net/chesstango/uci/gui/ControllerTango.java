package net.chesstango.uci.gui;

import net.chesstango.uci.engine.UciTango;

/**
 * @author Mauricio Coria
 */
public class ControllerTango extends ControllerAbstract {
    private final UciTango uciTango;

    public ControllerTango(UciTango uciTango) {
        super(uciTango);
        this.uciTango = uciTango;
    }

    @Override
    public void accept(ControllerVisitor controllerVisitor) {
        controllerVisitor.visit(uciTango);
    }
}
