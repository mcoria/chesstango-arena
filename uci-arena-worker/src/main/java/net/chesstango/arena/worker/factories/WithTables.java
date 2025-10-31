package net.chesstango.arena.worker.factories;

import net.chesstango.arena.worker.ControllerFactory;
import net.chesstango.uci.gui.Controller;

import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
public class WithTables implements Supplier<Controller> {
    private final String POLYGLOT_FILE;
    private final String SYZYGY_DIRECTORY;


    public WithTables() {
        POLYGLOT_FILE = System.getenv("POLYGLOT_FILE");
        SYZYGY_DIRECTORY = System.getenv("SYZYGY_DIRECTORY");
    }

    @Override
    public Controller get() {
        return ControllerFactory.createTangoControllerCustomConfig(config -> {
            /*
            config.setSearch(
                    AlphaBetaBuilder.createDefaultBuilderInstance()
                            .withGameEvaluator(Evaluator.getInstance())
                            .withStatistics()
                            .build()
            );
             */
            config.setPolyglotFile(POLYGLOT_FILE);
            config.setSyzygyDirectory(SYZYGY_DIRECTORY);
        });
    }
}
