package net.chesstango.uci.arena;

import net.chesstango.engine.Tango;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.fen.FENParser;
import net.chesstango.search.dummy.Dummy;
import net.chesstango.uci.arena.gui.ControllerPoolFactory;
import net.chesstango.uci.arena.matchtypes.MatchByDepth;
import net.chesstango.uci.engine.UciTango;
import net.chesstango.uci.gui.Controller;
import net.chesstango.uci.gui.ControllerTango;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Mauricio Coria
 */

public class MatchMultipleTest {

    private ObjectPool<Controller> smartEnginePool;

    private ObjectPool<Controller> dummyEnginePool;


    @BeforeEach
    public void setup() {
        smartEnginePool = new GenericObjectPool<>(new ControllerPoolFactory(() ->
                new ControllerTango(new UciTango())
                        .overrideEngineName("Smart")
        ));

        //new Tango(new Dummy())

        dummyEnginePool = new GenericObjectPool<>(new ControllerPoolFactory(() ->
                new ControllerTango(new UciTango(config -> {
                    config.setSearch(new Dummy());
                    return Tango.open(config);
                }))
                        .overrideEngineName("Dummy")
        ));
    }

    @Test
    public void testPlay() {
        MatchMultiple matchMultiple = new MatchMultiple(smartEnginePool, dummyEnginePool, new MatchByDepth(3));
        //matchMultiple.setDebugEnabled(true);

        List<MatchResult> matchResult = matchMultiple.play(Stream.of(FEN.of(FENParser.INITIAL_FEN)));

        assertEquals(2, matchResult.size());

        // Deberia ganar el engine smartEngine
        assertEquals(2, matchResult.stream()
                .map(MatchResult::getWinner)
                .filter(Objects::nonNull)
                .map(Controller::getEngineName)
                .filter("Smart"::equals)
                .count()
        );

    }


}
