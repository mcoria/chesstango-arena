package net.chesstango.arena.master.common;

import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.arena.core.MatchResult;
import net.chesstango.arena.core.listeners.MatchListener;
import net.chesstango.arena.core.matchtypes.MatchType;
import net.chesstango.uci.gui.Controller;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
public class Tournament {
    private final int parallelJobs;

    private final MatchType matchType;

    private final List<Supplier<Controller>> engineSupplierList;

    @Setter
    @Accessors(chain = true)
    private MatchListener matchListener;

    public Tournament(int parallelJobs, List<Supplier<Controller>> engineSupplierList, MatchType matchType) {
        this.parallelJobs = parallelJobs;
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
                        MatchMultiple matchMultiple = new MatchMultiple(parallelJobs, mainPool, opponentPool, matchType)
                                .setSide(MatchSide.BOTH)
                                .setMatchListener(matchListener);
                        matchResults.addAll(matchMultiple.play(fenList));
                    }
                }
            }
        }

        return matchResults;
    }
}
