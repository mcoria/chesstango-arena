package net.chesstango.arena.worker.factories;

import net.chesstango.arena.worker.ControllerFactory;
import net.chesstango.evaluation.Evaluator;
import net.chesstango.search.builders.AlphaBetaBuilder;
import net.chesstango.uci.gui.Controller;

import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
public class WithTables implements Supplier<Controller> {

    @Override
    public Controller get() {
        return ControllerFactory.createTangoControllerCustomConfig(config -> {
            config.setSearch(
                    AlphaBetaBuilder.createDefaultBuilderInstance()
                            .withGameEvaluator(Evaluator.getInstance())
                            .withStatistics()
                            .build());
            config.setPolyglotFile("C:/java/projects/chess/chess-utils/books/openings/polyglot-collection/komodo.bin");
            config.setSyzygyDirectory("C:/java/projects/chess/chess-utils/books/syzygy/3-4-5");
        });
    }
}
