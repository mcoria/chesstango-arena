package net.chesstango.arena.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.core.MatchResult;
import net.chesstango.arena.core.listeners.MatchBroadcaster;
import net.chesstango.arena.core.listeners.SavePGNGame;
import net.chesstango.arena.core.matchtypes.MatchByClock;
import net.chesstango.arena.core.matchtypes.MatchByDepth;
import net.chesstango.arena.core.matchtypes.MatchByTime;
import net.chesstango.arena.core.matchtypes.MatchType;
import net.chesstango.arena.core.reports.MatchesByClock;
import net.chesstango.arena.core.reports.MatchesBySearchManager;
import net.chesstango.arena.core.reports.MatchesByTreeSummaryReport;
import net.chesstango.arena.core.reports.MatchesReport;
import net.chesstango.arena.master.common.ControllerPoolFactory;
import net.chesstango.arena.master.common.MatchMultiple;
import net.chesstango.arena.master.common.MatchSide;
import net.chesstango.arena.worker.ControllerFactory;
import net.chesstango.engine.Tango;
import net.chesstango.evaluation.Evaluator;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.gardel.pgn.PGNDecoder;
import net.chesstango.search.builders.AlphaBetaBuilder;
import net.chesstango.uci.gui.Controller;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final MatchType MATCH_TYPE = new MatchByDepth(5);
    //private static final MatchType MATCH_TYPE = new MatchByTime(500);
    //private static final MatchType MATCH_TYPE = new MatchByClock(1000 * 60 * 2, 1000);
    //private static final MatchType MATCH_TYPE = new MatchByClock(1000 * 60, 0);
    //private static final MatchType MATCH_TYPE = new MatchByClock(1000, 0); // Will time out

    private static final boolean DEBUG = true;
    private static final MatchSide MATCH_SIDE = MatchSide.BLACK_ONLY;

    // private static final String POLYGLOT_FILE = "C:/java/projects/chess/chess-utils/books/openings/polyglot-collection/komodo.bin";
    private static final String POLYGLOT_FILE = "C:\\java\\projects\\chess\\chess-utils\\books\\openings\\polyglot-collection\\komodo.bin";
    // C:\java\projects\chess\chess-utils\books\openings\chesstango
    // private static final String SYZYGY_PATH = "D:\\k8s_shared\\syzygy\\3-4-5";
    private static final String SYZYGY_PATH = "D:\\k8s_shared\\syzygy\\3-4-5;D:\\k8s_shared\\syzygy\\6-DTZ;D:\\k8s_shared\\syzygy\\6-WDL";

    private static final Path spike = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Spike.json");
    // private static final Path stockfish = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Stockfish.json");
    private static final Path tango_1_1 = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Tango-v1.1.0-no-books.json");
    private static final Path tango_1_2 = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Tango-v1.2.0.json");
    private static final Path tango_1_3 = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Tango-v1.3.0.json");
    private static final Path tango_1_4 = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Tango-v1.4.1.json");
    private static final Path tango_1_5 = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Tango-v1.5.0.json");
    private static final Path tango_1_6 = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Tango-v1.6.0-nobook.json");
    private static final Path tango_1_7 = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Tango-v1.7.0-nobook.json");
    private static final Path obsedian = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Obsidian.json");
    private static final Path arasan = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog_win\\Arasan.json");

    // private static final int parallelJobs = Runtime.getRuntime().availableProcessors();
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
        //Supplier<Controller> engine1Supplier = () -> ControllerFactory.createProxyController(tango_1_6);
        Supplier<Controller> engine1Supplier = ControllerFactory::createTangoController;

        /*
        Supplier<Controller> engine1Supplier = () -> ControllerFactory.createTangoControllerWithSearch(() ->
                AlphaBetaBuilder
                        .createDefaultBuilderInstance()
                        .withGameEvaluator(Evaluator.createInstance())
                        .withStatistics()
                        .build()
        ).overrideEngineName(Tango.ENGINE_NAME);
         */

        //Supplier<Controller> engine1Supplier = () -> ControllerFactory.createTangoControllerWithEvaluator(Evaluator::getInstance);

        //Supplier<Controller> engine2Supplier = () -> ControllerFactory.createProxyController(tango);
        Supplier<Controller> engine2Supplier = () -> ControllerFactory.createProxyController(tango_1_6);


        List<MatchResult> matchResult = new MatchMain(engine1Supplier, engine2Supplier)
                .play(fromPGN());

        new MatchesReport()
                .withMatchResults(matchResult)
                .printReport(System.out);


        new MatchesBySearchManager()
                .breakByGame()
                //.breakByColor()
                .withMathResults(matchResult)
                .printReport(System.out);

        new MatchesByClock()
                .withMathResults(matchResult)
                .printReport(System.out);


        /*
        // ES NECESARIO HABILITAR ESTADISTICAS PARA ESTE REPORTE
        new MatchesByTreeSummaryReport()
                .withNodesVisitedStatistics()
                .withCutoffStatistics()
                .withEvaluationStatistics()
                .withTranspositionStatistics()
                .breakByGame()
                //.breakByColor()
                .withMathResults(matchResult)
                .printReport(System.out);


        // no tiene sentido imprimir para todos los matches, deberia almacenar y luego reportar o filtrar


        new MatchesByTreeDetailsReport()
                .withCutoffStatistics()
                .withNodesVisitedStatistics()
                .withPrincipalVariationReport()
                .withEvaluationReport()
                .withFilter(pgn -> pgn.getFen().toString().equals(FENParser.INITIAL_FEN))
                .withMoveResults(MatchesByTreeDetailsReport.filterByEngineName("Tango", matchResult))
                .printReport(System.out);
         */

    }

    private static Stream<PGN> readPGNFile() {
        try (FileInputStream fis = new FileInputStream("C:\\java\\projects\\chess\\chess-utils\\testing\\PGN\\openings\\Balsa_v2724\\Balsa_Top10.pgn")) {
            return new PGNDecoder().decodePGNs(fis).limit(1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Stream<PGN> readFENFile() {
        Stream.Builder<PGN> fenBuilder = Stream.builder();

        Path filePath = Paths.get("C:\\java\\projects\\chess\\chess-utils\\testing\\PGN\\full\\LumbrasGigaBase\\OverTheBoard\\LumbrasGigaBase_OTB_2025_6_pieces-draws.fen");

        try (Stream<String> lines = Files.lines(filePath)) {
            lines.filter(s -> s != null && !s.trim().isEmpty())
                    .map(FEN::from)
                    .limit(200)
                    .map(PGN::from)
                    .forEach(fenBuilder::add);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        return fenBuilder.build();
    }


    private static Stream<PGN> fromFEN() {
        List<String> fenList = List.of(FEN.START_POSITION_STRING);
        return fenList
                .stream()
                .map(FEN::from)
                .map(PGN::from);
    }

    private static Stream<PGN> fromPGN() {
        String pgn =
                """
                [Event "SCCT - Unique v150"]
                [Site "?"]
                [Date "2023.02.07"]
                [Round "?"]
                [White "X"]
                [Black "X"]
                [Result "*"]
                [ECO "C16"]
                [PlyCount "18"]
                [EventDate "2022.??.??"]
                
                1. e4 e6 2. d4 d5 3. Nc3 Bb4 4. e5 Qd7 5. a3 Bxc3+ 6. bxc3 b6 7. Qg4 f5 8. Qg3 Bb7 9. a4 Nc6 *
                """;

        return Stream.of(PGN.from(pgn));
    }


    private final Supplier<Controller> engine1Supplier;
    private final Supplier<Controller> engine2Supplier;

    public MatchMain(Supplier<Controller> engine1Supplier, Supplier<Controller> engine2Supplier) {
        this.engine1Supplier = engine1Supplier;
        this.engine2Supplier = engine2Supplier;
    }

    private List<MatchResult> play(Stream<PGN> pgnStream) {
        try (ObjectPool<Controller> mainPool = new GenericObjectPool<>(new ControllerPoolFactory(engine1Supplier));
             ObjectPool<Controller> opponentPool = new GenericObjectPool<>(new ControllerPoolFactory(engine2Supplier))) {

            MatchMultiple matchMultiple = new MatchMultiple(parallelJobs, mainPool, opponentPool, MATCH_TYPE)
                    .setDebug(DEBUG)
                    .setSide(MATCH_SIDE)
                    .setMatchListener(new MatchBroadcaster()
                            //.addListener(new MatchListenerToMBean())
                            .addListener(new SavePGNGame())
                    );

            Instant start = Instant.now();

            List<MatchResult> matchResult = matchMultiple.play(pgnStream);

            log.info("Time taken: {} ms", Duration.between(start, Instant.now()).toMillis());

            return matchResult;
        }
    }
}
