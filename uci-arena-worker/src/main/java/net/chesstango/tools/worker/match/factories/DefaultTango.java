package net.chesstango.tools.worker.match.factories;

import net.chesstango.tools.worker.match.ControllerFactory;
import net.chesstango.uci.gui.Controller;

import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
public class DefaultTango implements Supplier<Controller> {

    @Override
    public Controller get() {
        return ControllerFactory.createTangoController();
    }
}
