package net.chesstango.tools.worker.match.factories;

import net.chesstango.tools.worker.match.ControllerFactory;
import net.chesstango.uci.gui.Controller;

import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
public class WithTables implements Supplier<Controller> {

    @Override
    public Controller get() {
        return ControllerFactory.createTangoControllerCustomConfig(config -> {
            config.setPolyglotFile("C:/java/projects/chess/chess-utils/books/openings/polyglot-collection/komodo.bin");
            config.setSyzygyDirectory("C:/java/projects/chess/chess-utils/books/syzygy/3-4-5");
        });
    }
}
