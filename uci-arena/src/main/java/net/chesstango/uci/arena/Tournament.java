package net.chesstango.uci.arena;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.uci.arena.gui.ControllerPoolFactory;
import net.chesstango.uci.gui.Controller;
import net.chesstango.uci.arena.listeners.MatchListener;
import net.chesstango.uci.arena.matchtypes.MatchType;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
public class Tournament {
    private static final Logger logger = LoggerFactory.getLogger(Tournament.class);

    private final MatchType matchType;

    private final List<Supplier<Controller>> engineSupplierList;

    @Setter
    @Accessors(chain = true)
    private MatchListener matchListener;

    public Tournament(List<Supplier<Controller>> engineSupplierList, MatchType matchType) {
        this.engineSupplierList = engineSupplierList;
        this.matchType = matchType;
    }

    public List<MatchResult> play(Stream<FEN> fenList) {

        List<MatchResult> matchResults = Collections.synchronizedList(new LinkedList<>());

        Supplier<Controller> mainEngineSupplier = engineSupplierList.getFirst();

        try (ObjectPool<Controller> mainPool = new GenericObjectPool<>(new ControllerPoolFactory(mainEngineSupplier))) {
            for (Supplier<Controller> opponentEngineSupplier : engineSupplierList) {
                try (ObjectPool<Controller> opponentPool = new GenericObjectPool<>(new ControllerPoolFactory(opponentEngineSupplier))) {
                    if (mainEngineSupplier != opponentEngineSupplier) {
                        MatchMultiple matchMultiple = new MatchMultiple(mainPool, opponentPool, matchType)
                                .setSwitchChairs(true)
                                .setMatchListener(matchListener);
                        matchResults.addAll(matchMultiple.play(fenList));
                    }
                }
            }
        }

        return matchResults;
    }


}
