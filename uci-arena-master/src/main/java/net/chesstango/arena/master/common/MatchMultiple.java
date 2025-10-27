package net.chesstango.arena.master.common;


import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.core.Match;
import net.chesstango.arena.core.MatchResult;
import net.chesstango.arena.core.listeners.MatchListener;
import net.chesstango.arena.core.matchtypes.MatchType;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.uci.gui.Controller;
import org.apache.commons.pool2.ObjectPool;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class MatchMultiple {

    public enum Side {
        BOTH,
        WHITE_ONLY,
        BLACK_ONLY
    }

    private final int parallelJobs;

    private final ObjectPool<Controller> controllerPool1;

    private final ObjectPool<Controller> controllerPool2;

    private final MatchType matchType;

    private final List<MatchResult> result = Collections.synchronizedList(new LinkedList<>());

    @Setter
    @Accessors(chain = true)
    private boolean printPGN;

    @Setter
    @Accessors(chain = true)
    private Side side = Side.BOTH;

    @Setter
    @Accessors(chain = true)
    private MatchListener matchListener;


    public MatchMultiple(int parallelJobs, ObjectPool<Controller> controllerPool1, ObjectPool<Controller> controllerPool2, MatchType matchType) {
        this.parallelJobs = parallelJobs;
        this.controllerPool1 = controllerPool1;
        this.controllerPool2 = controllerPool2;
        this.matchType = matchType;
    }

    public List<MatchResult> play(Stream<FEN> fenStream) {
        try (ExecutorService executor = Executors.newFixedThreadPool(parallelJobs)) {
            fenStream.forEach(fen -> {
                switch (side) {
                    case BOTH:
                        executor.execute(() -> play(fen, controllerPool1, controllerPool2));
                        executor.execute(() -> play(fen, controllerPool2, controllerPool1));
                        break;
                    case WHITE_ONLY:
                        executor.execute(() -> play(fen, controllerPool1, controllerPool2));
                        break;
                    case BLACK_ONLY:
                        executor.execute(() -> play(fen, controllerPool2, controllerPool1));
                        break;
                }
            });
        }
        return result;
    }

    private void play(FEN fen,
                      ObjectPool<Controller> thePool1,
                      ObjectPool<Controller> thePool2) {

        Controller controller1 = null;
        Controller controller2 = null;

        try {
            controller1 = getControllerFromPool(thePool1);
            controller2 = getControllerFromPool(thePool2);

            Match match = new Match(controller1, controller2, fen, matchType)
                    .setPrintPGN(printPGN)
                    .setMatchListener(matchListener);

            match.setMatchListener(matchListener);

            result.add(match.play());

            thePool1.returnObject(controller1);
            thePool2.returnObject(controller2);

        } catch (Exception e) {
            log.error("Error playing", e);
            invalidateObject(controller1, thePool1);
            invalidateObject(controller2, thePool2);
        }
    }

    private static Controller getControllerFromPool(ObjectPool<Controller> pool) {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            log.error("getControllerFromPool error", e);
            throw new RuntimeException(e);
        }
    }

    private static void invalidateObject(Controller controller, ObjectPool<Controller> pool) {
        if (controller != null && pool != null) {
            try {
                pool.invalidateObject(controller);
            } catch (Exception e) {
                log.error("invalidateObject error", e);
            }
        }
    }

}
