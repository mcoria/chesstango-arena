package net.chesstango.uci.arena;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.uci.gui.Controller;
import net.chesstango.uci.arena.listeners.MatchListener;
import net.chesstango.uci.arena.matchtypes.MatchType;
import org.apache.commons.pool2.ObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
public class MatchMultiple {
    private static final Logger logger = LoggerFactory.getLogger(MatchMultiple.class);

    private final ObjectPool<Controller> controllerPool1;

    private final ObjectPool<Controller> controllerPool2;

    private final MatchType matchType;

    private final List<MatchResult> result = Collections.synchronizedList(new LinkedList<>());

    @Setter
    @Accessors(chain = true)
    private boolean debugEnabled;

    @Setter
    @Accessors(chain = true)
    private boolean switchChairs;

    @Setter
    @Accessors(chain = true)
    private MatchListener matchListener;


    public MatchMultiple(ObjectPool<Controller> controllerPool1, ObjectPool<Controller> controllerPool2, MatchType matchType) {
        this.controllerPool1 = controllerPool1;
        this.controllerPool2 = controllerPool2;
        this.matchType = matchType;
        this.switchChairs = true;
    }

    public List<MatchResult> play(Stream<FEN> fenStream) {
        int availableCores = Runtime.getRuntime().availableProcessors();

        try (ExecutorService executor = Executors.newFixedThreadPool(availableCores - 1)) {
            fenStream.forEach(fen -> {
                executor.submit(() -> play(fen, controllerPool1, controllerPool2));
                if (switchChairs) {
                    executor.submit(() -> play(fen, controllerPool1, controllerPool2));
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

            Match match = new Match(controller1, controller2, matchType)
                    .setDebugEnabled(debugEnabled)
                    .setMatchListener(matchListener);

            match.setMatchListener(matchListener);

            result.add(match.play(fen));

            thePool1.returnObject(controller1);
            thePool2.returnObject(controller2);

        } catch (Exception e) {
            logger.error("Error playing", e);

            invalidateObject(controller1, thePool1);
            invalidateObject(controller2, thePool2);
        }
    }

    private static Controller getControllerFromPool(ObjectPool<Controller> pool) {
        try {
            return pool.borrowObject();
        } catch (Exception e) {
            logger.error("getControllerFromPool error", e);
            throw new RuntimeException(e);
        }
    }

    private static void invalidateObject(Controller controller, ObjectPool<Controller> pool) {
        if (controller != null && pool != null) {
            try {
                pool.invalidateObject(controller);
            } catch (Exception e) {
                logger.error("invalidateObject error", e);
            }
        }
    }
}
