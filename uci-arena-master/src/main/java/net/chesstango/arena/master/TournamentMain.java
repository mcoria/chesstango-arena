package net.chesstango.arena.master;

import net.chesstango.arena.core.MatchResult;
import net.chesstango.arena.core.listeners.CaptureMatchResult;
import net.chesstango.arena.core.listeners.MatchBroadcaster;
import net.chesstango.arena.core.listeners.SavePGNGame;
import net.chesstango.arena.core.matchtypes.MatchByDepth;
import net.chesstango.arena.master.common.MatchListenerToMBeans;
import net.chesstango.arena.master.common.Tournament;
import net.chesstango.arena.worker.ControllerFactory;
import net.chesstango.evaluation.evaluators.EvaluatorByMaterialAndPST;
import net.chesstango.evaluation.evaluators.EvaluatorImp02;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.gardel.pgn.PGNStringDecoder;
import net.chesstango.uci.gui.Controller;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
public class TournamentMain {
    private static final Path spike = Path.of("C:\\java\\projects\\chess\\chess-utils\\engines\\catalog\\Spike.json");
    //private static final int parallelJobs = Runtime.getRuntime().availableProcessors();
    private static final int parallelJobs = 2;

    private static final MatchByDepth matchType = new MatchByDepth(2);

    public static void main(String[] args) {
        Supplier<Controller> main = () -> ControllerFactory.createTangoControllerWithEvaluator(EvaluatorByMaterialAndPST::new);
        Supplier<Controller> evaluatorImp02 = () -> ControllerFactory.createTangoControllerWithEvaluator(EvaluatorImp02::new);
        /*
        EngineControllerPoolFactory factory1 = new EngineControllerPoolFactory(() -> EngineControllerFactory.createTangoControllerWithDefaultSearch(EvaluatorByMaterial.class));
        EngineControllerPoolFactory factory2 = new EngineControllerPoolFactory(() -> EngineControllerFactory.createTangoControllerWithDefaultSearch(EvaluatorByMaterialAndMoves.class));
        EngineControllerPoolFactory factory3 = new EngineControllerPoolFactory(() -> EngineControllerFactory.createTangoControllerWithDefaultSearch(EvaluatorImp01.class));

        EngineControllerPoolFactory factory5 = new EngineControllerPoolFactory(() -> EngineControllerFactory.createTangoControllerWithDefaultSearch(EvaluatorSimplifiedEvaluator.class));
         */
        Supplier<Controller> spikeSupplier = () -> ControllerFactory.createProxyController(spike);


        List<Supplier<Controller>> engineSupplierList = Arrays.asList(main, evaluatorImp02, spikeSupplier);

        List<MatchResult> matchResult = new TournamentMain(engineSupplierList)
                .play(getPGNs());
    }

    private static Stream<PGN> getPGNs() {
        try {
            //List<String> fenList = new Transcoding().pgnFileToFenPositions(TournamentMain.class.getClassLoader().getResourceAsStream("Balsa_v2724.pgn"));
            Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMain.class.getClassLoader().getResourceAsStream("Balsa_Top10.pgn"));
            //List<String> fenList = List.of(FENDecoder.INITIAL_FEN);
            return pgnStream;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final List<Supplier<Controller>> engineSupplierList;

    public TournamentMain(List<Supplier<Controller>> engineSupplierList) {
        this.engineSupplierList = engineSupplierList;
    }

    public List<MatchResult> play(Stream<PGN> pgnStream) {
        CaptureMatchResult captureMatchResult = new CaptureMatchResult();

        Tournament tournament = new Tournament(parallelJobs, engineSupplierList, matchType)
                .setMatchListener(new MatchBroadcaster()
                        .addListener(new MatchListenerToMBeans())
                        .addListener(new SavePGNGame())
                        .addListener(captureMatchResult));

        Instant start = Instant.now();
        tournament.play(pgnStream);
        System.out.println("Time elapsed: " + Duration.between(start, Instant.now()).toMillis() + " ms");

        return captureMatchResult.getMatchResults();
    }
}
