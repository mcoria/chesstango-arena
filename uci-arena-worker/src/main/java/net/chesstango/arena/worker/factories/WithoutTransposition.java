package net.chesstango.arena.worker.factories;

import net.chesstango.arena.worker.ControllerFactory;
import net.chesstango.evaluation.Evaluator;
import net.chesstango.search.builders.AlphaBetaBuilder;
import net.chesstango.uci.gui.Controller;

import java.util.function.Supplier;

/**
 * @author Mauricio Coria
 */
public class WithoutTransposition implements Supplier<Controller> {
    private final String POLYGLOT_FILE;
    private final String SYZYGY_PATH;


    public WithoutTransposition() {
        POLYGLOT_FILE = System.getenv("POLYGLOT_FILE");
        SYZYGY_PATH = System.getenv("SYZYGY_PATH");
    }

    @Override
    public Controller get() {
        return ControllerFactory.createTangoControllerWithSearch(() ->
                new AlphaBetaBuilder()
                        .withGameEvaluatorCache()
                        .withGameEvaluator(Evaluator.createInstance())

                        .withQuiescence()

                        .withKillerMoveSorter()
                        .withRecaptureSorter()
                        .withMvvLvaSorter()

                        .withAspirationWindows()

                        .withIterativeDeepening()

                        .withStopProcessingCatch()

                        .build()
        );
    }
}
