package net.chesstango.tools.arena;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.board.Game;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.fen.FENParser;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.gardel.pgn.PGNStringDecoder;
import net.chesstango.tools.arena.common.ControllerPoolFactory;
import net.chesstango.tools.arena.common.MatchMultiple;
import net.chesstango.tools.worker.match.ControllerFactory;
import net.chesstango.uci.arena.MatchResult;
import net.chesstango.uci.arena.listeners.MatchBroadcaster;
import net.chesstango.uci.arena.listeners.SavePGNGame;
import net.chesstango.uci.arena.matchtypes.MatchByDepth;
import net.chesstango.uci.arena.matchtypes.MatchType;
import net.chesstango.uci.gui.Controller;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class MatchMain {

    private static final MatchType MATCH_TYPE = new MatchByDepth(4);
    //private static final MatchType MATCH_TYPE = new MatchByTime(2000);
    //private static final MatchType MATCH_TYPE = new MatchByClock(1000 * 60 * 3, 1000);

    private static final boolean MATCH_DEBUG = false;
    private static final boolean MATCH_SWITCH_CHAIRS = true;

    private static final String POLYGLOT_FILE = "C:/java/projects/chess/chess-utils/books/openings/polyglot-collection/komodo.bin";
    private static final String SYZYGY_DIRECTORY = "C:/java/projects/chess/chess-utils/books/syzygy/3-4-5";

    private static final Path spike = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Spike.json");
    private static final Path tango = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Tango-v1.2.0-CHT-328.json");

    //private static final int parallelJobs = Runtime.getRuntime().availableProcessors();
    private static final int parallelJobs = 1;

    /**
     * Add the following JVM parameters:
     * -Dcom.sun.management.jmxremote
     * -Dcom.sun.management.jmxremote.port=19999
     * -Dcom.sun.management.jmxremote.local.only=false
     * -Dcom.sun.management.jmxremote.authenticate=false
     * -Dcom.sun.management.jmxremote.ssl=false
     */
    public static void main(String[] args) {
        //Supplier<Controller> engine1Supplier = ControllerFactory::createTangoController;

        /*
        Supplier<Controller> engine1Supplier = () -> ControllerFactory.createTangoControllerWithSearch(() ->
                AlphaBetaBuilder
                        .createDefaultBuilderInstance(Evaluator.getInstance())
                        .withStatistics()
                        .build()
        );
         */
        //Supplier<Controller> engine1Supplier = () -> ControllerFactory.createTangoControllerWithEvaluator(EvaluatorImp05::new);


        Supplier<Controller> engine1Supplier = () -> ControllerFactory.createTangoControllerCustomConfig(config -> {
            config.setPolyglotFile(POLYGLOT_FILE);
            config.setSyzygyDirectory(SYZYGY_DIRECTORY);
        });


        //Supplier<Controller> engine1Supplier = () -> ControllerFactory.createProxyController(tango);


        Supplier<Controller> engine2Supplier = () -> ControllerFactory.createProxyController(spike);
        //Supplier<Controller> engine2Supplier = () -> ControllerFactory.createTangoControllerWithEvaluator(EvaluatorImp06::new);


        List<MatchResult> matchResult = new MatchMain(engine1Supplier, engine2Supplier)
                .play(getFEN());
    }

    private static Stream<FEN> getFromPGN() {
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMain.class.getClassLoader().getResourceAsStream("Balsa_Top10.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMain.class.getClassLoader().getResourceAsStream("Balsa_Top25.pgn"));
        Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMain.class.getClassLoader().getResourceAsStream("Balsa_Top50.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMain.class.getClassLoader().getResourceAsStream("Balsa_v500.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMain.class.getClassLoader().getResourceAsStream("Balsa_v2724.pgn"));

        return pgnStream
                .map(Game::from)
                .map(Game::getCurrentFEN);
    }


    private static Stream<FEN> getFEN() {
        //List<String> fenList = List.of(FENParser.INITIAL_FEN);
        List<String> fenList =  List.of("QN4n1/6r1/3k4/8/b2K4/8/8/8 b - - 0 1");
        //List<String> fenList =  List.of("1k1r3r/pp6/2P1bp2/2R1p3/Q3Pnp1/P2q4/1BR3B1/6K1 b - - 0 1");
        //List<String> fenList =  List.of(FENDecoder.INITIAL_FEN, "1k1r3r/pp6/2P1bp2/2R1p3/Q3Pnp1/P2q4/1BR3B1/6K1 b - - 0 1");

        return fenList
                .stream()
                .map(FEN::of);
    }

    private final Supplier<Controller> engine1Supplier;
    private final Supplier<Controller> engine2Supplier;

    public MatchMain(Supplier<Controller> engine1Supplier, Supplier<Controller> engine2Supplier) {
        this.engine1Supplier = engine1Supplier;
        this.engine2Supplier = engine2Supplier;
    }

    private List<MatchResult> play(Stream<FEN> fenStream) {
        try (ObjectPool<Controller> mainPool = new GenericObjectPool<>(new ControllerPoolFactory(engine1Supplier));
             ObjectPool<Controller> opponentPool = new GenericObjectPool<>(new ControllerPoolFactory(engine2Supplier))) {

            MatchMultiple match = new MatchMultiple(parallelJobs, mainPool, opponentPool, MATCH_TYPE)
                    .setPrintPGN(MATCH_DEBUG)
                    .setSwitchChairs(MATCH_SWITCH_CHAIRS)
                    .setMatchListener(new MatchBroadcaster()
                            //         .addListener(new MatchListenerToMBean())
                            .addListener(new SavePGNGame()));

            Instant start = Instant.now();

            List<MatchResult> matchResult = match.play(fenStream);

            log.info("Time taken: {} ms", Duration.between(start, Instant.now()).toMillis());

            return matchResult;
        }
    }
}
