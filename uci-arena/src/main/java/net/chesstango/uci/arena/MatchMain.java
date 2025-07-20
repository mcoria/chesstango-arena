package net.chesstango.uci.arena;

import net.chesstango.board.Game;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.gardel.pgn.PGNStringDecoder;
import net.chesstango.uci.arena.gui.ControllerFactory;
import net.chesstango.uci.arena.gui.ControllerPoolFactory;
import net.chesstango.uci.arena.listeners.MatchBroadcaster;
import net.chesstango.uci.arena.listeners.MatchListenerToMBean;
import net.chesstango.uci.arena.listeners.SavePGNGame;
import net.chesstango.uci.arena.matchtypes.MatchByTime;
import net.chesstango.uci.arena.matchtypes.MatchType;
import net.chesstango.uci.gui.Controller;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
public class MatchMain {
    private static final Logger logger = LoggerFactory.getLogger(MatchMain.class);

    //private static final MatchType MATCH_TYPE = new MatchByDepth(7);
    private static final MatchType MATCH_TYPE = new MatchByTime(2000);
    //private static final MatchType MATCH_TYPE = new MatchByClock(1000 * 60 * 3, 1000);

    private static final boolean MATCH_DEBUG = true;
    private static final boolean MATCH_SWITCH_CHAIRS = false;

    /**
     * Add the following JVM parameters:
     * -Dcom.sun.management.jmxremote
     * -Dcom.sun.management.jmxremote.port=19999
     * -Dcom.sun.management.jmxremote.local.only=false
     * -Dcom.sun.management.jmxremote.authenticate=false
     * -Dcom.sun.management.jmxremote.ssl=false
     */
    public static void main(String[] args) {
        Supplier<Controller> engine1Supplier = ControllerFactory::createTangoController;

        Supplier<Controller> engine2Supplier = () -> ControllerFactory.createProxyController("Spike", null);

        new MatchMain(engine1Supplier, engine2Supplier)
                .play();

    }

    private static Stream<FEN> getFenList() {
        //List<String> fenList = List.of(FENDecoder.INITIAL_FEN);
        //List<String> fenList =  List.of("1k1r3r/pp6/2P1bp2/2R1p3/Q3Pnp1/P2q4/1BR3B1/6K1 b - - 0 1");
        //List<String> fenList =  List.of(FENDecoder.INITIAL_FEN, "1k1r3r/pp6/2P1bp2/2R1p3/Q3Pnp1/P2q4/1BR3B1/6K1 b - - 0 1");
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMain.class.getClassLoader().getResourceAsStream("Balsa_Top10.pgn"));
        Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMain.class.getClassLoader().getResourceAsStream("Balsa_Top25.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMain.class.getClassLoader().getResourceAsStream("Balsa_Top50.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMain.class.getClassLoader().getResourceAsStream("Balsa_v500.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMain.class.getClassLoader().getResourceAsStream("Balsa_v2724.pgn"));
        //return Stream.of(FEN.of(INITIAL_FEN));
        return pgnStream
                .map(Game::from)
                .map(Game::getCurrentFEN);
    }

    private final Supplier<Controller> engine1Supplier;
    private final Supplier<Controller> engine2Supplier;

    public MatchMain(Supplier<Controller> engine1Supplier, Supplier<Controller> engine2Supplier) {
        this.engine1Supplier = engine1Supplier;
        this.engine2Supplier = engine2Supplier;
    }

    private List<MatchResult> play() {
        try (ObjectPool<Controller> mainPool = new GenericObjectPool<>(new ControllerPoolFactory(engine1Supplier));
             ObjectPool<Controller> opponentPool = new GenericObjectPool<>(new ControllerPoolFactory(engine2Supplier))) {

            MatchMultiple match = new MatchMultiple(mainPool, opponentPool, MATCH_TYPE)
                    .setDebugEnabled(MATCH_DEBUG)
                    .setSwitchChairs(MATCH_SWITCH_CHAIRS)
                    .setMatchListener(new MatchBroadcaster()
                            .addListener(new MatchListenerToMBean())
                            .addListener(new SavePGNGame()));

            Instant start = Instant.now();

            List<MatchResult> matchResult = match.play(getFenList());

            logger.info("Time taken: {} ms", Duration.between(start, Instant.now()).toMillis());

            return matchResult;
        }
    }
}
