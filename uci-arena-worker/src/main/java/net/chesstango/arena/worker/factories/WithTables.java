package net.chesstango.arena.worker.factories;

import net.chesstango.arena.worker.ControllerFactory;
import net.chesstango.uci.gui.Controller;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author Mauricio Coria
 */
public class WithTables implements Supplier<Controller> {
    private final String POLYGLOT_FILE;
    private final String SYZYGY_PATH;


    public WithTables() {
        POLYGLOT_FILE = System.getenv("POLYGLOT_FILE");
        SYZYGY_PATH = System.getenv("SYZYGY_PATH");
    }

    @Override
    public Controller get() {
        return ControllerFactory.createTangoControllerCustomConfig(config -> {

            if (Objects.nonNull(SYZYGY_PATH)) {
                config.setPolyglotFile(Path.of(POLYGLOT_FILE));
            }

            if (Objects.nonNull(SYZYGY_PATH)) {
                Set<Path> syzygyDirs = Arrays
                        .stream(SYZYGY_PATH.split(File.pathSeparator))
                        .filter(dir -> !dir.isEmpty())
                        .map(Paths::get)
                        .collect(Collectors.toSet());
                config.setSyzygyDirs(syzygyDirs);
            }
        });
    }
}
