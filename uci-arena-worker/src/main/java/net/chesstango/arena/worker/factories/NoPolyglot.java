package net.chesstango.arena.worker.factories;

import net.chesstango.arena.worker.ControllerFactory;
import net.chesstango.uci.gui.Controller;

import java.nio.file.Path;
import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
public class NoPolyglot implements Supplier<Controller> {
    private final String SYZYGY_PATH;


    public NoPolyglot() {
        SYZYGY_PATH = System.getenv("SYZYGY_PATH");
    }

    @Override
    public Controller get() {
        return ControllerFactory.createTangoControllerCustomConfig(config -> {
            config.setSyzygyPath(Path.of(SYZYGY_PATH));
        });
    }
}
