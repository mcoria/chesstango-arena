package net.chesstango.arena.master;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.core.matchtypes.MatchByDepth;
import net.chesstango.arena.core.matchtypes.MatchType;
import net.chesstango.arena.worker.MatchRequest;
import net.chesstango.board.Game;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.fen.FENParser;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.gardel.pgn.PGNStringDecoder;
import org.apache.commons.cli.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static net.chesstango.arena.master.common.Common.SESSION_DATE;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class MatchMainProducer implements Runnable {

    public static void main(String[] args) {
        CommandLine parsedArgs = parseArguments(args);

        String rabbitHost = parsedArgs.getOptionValue("r", "localhost");
        log.info("Rabbit host: {}", rabbitHost);

        String engine = parsedArgs.getOptionValue("e");
        log.info("Engine: {}", engine);

        MatchType matchType = null;
        if (parsedArgs.hasOption('d')) {
            matchType = new MatchByDepth(Integer.parseInt(parsedArgs.getOptionValue("d")));
            log.info("Match: {}", matchType);
        }

        new MatchMainProducer(rabbitHost, engine, matchType)
                .run();
    }

    private final String rabbitHost;
    private final String engine;
    private final MatchType matchType;

    public MatchMainProducer(String rabbitHost, String engine, MatchType matchType) {
        this.rabbitHost = rabbitHost;
        this.engine = engine;
        this.matchType = matchType;
    }

    @Override
    public void run() {
        log.info("Starting");

        List<MatchRequest> matchRequests = createMatchRequests(fromPGN());

        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitHost);
            factory.setSharedExecutor(executorService);

            try (RequestProducer requestProducer = RequestProducer.open(factory)) {

                matchRequests.forEach(requestProducer::publish);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        log.info("Finished");
    }


    private List<MatchRequest> createMatchRequests(List<FEN> fenList) {
        String player2 = "file:Spike";

        Stream<MatchRequest> result = fenList.stream()
                .map(fen -> new MatchRequest()
                        .setWhiteEngine(engine)
                        .setBlackEngine(player2)
                        .setFen(fen)
                        .setMatchType(matchType)
                        .setMatchId(UUID.randomUUID().toString())
                        .setSessionId(SESSION_DATE)
                );


        Stream<MatchRequest> switchStream = fenList.stream()
                .map(fen -> new MatchRequest()
                        .setWhiteEngine(player2)
                        .setBlackEngine(engine)
                        .setFen(fen)
                        .setMatchType(matchType)
                        .setMatchId(UUID.randomUUID().toString())
                        .setSessionId(SESSION_DATE)
                );

        result = Stream.concat(result, switchStream);

        return result
                .peek(request -> log.info("{}", request.toString()))
                .toList();
    }


    private static List<FEN> fromPGN() {
        Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMainProducer.class.getClassLoader().getResourceAsStream("Balsa_Top10.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMasterMain.class.getClassLoader().getResourceAsStream("Balsa_Top25.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMasterMain.class.getClassLoader().getResourceAsStream("Balsa_Top50.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMasterMain.class.getClassLoader().getResourceAsStream("Balsa_v500.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMasterMain.class.getClassLoader().getResourceAsStream("Balsa_v2724.pgn"));

        return pgnStream
                .map(Game::from)
                .map(Game::getCurrentFEN)
                .toList();
    }


    private static List<FEN> fromFEN() {
        List<String> fenList = List.of(FENParser.INITIAL_FEN);
        //List<String> fenList =  List.of("K7/N7/k7/8/3p4/8/N7/8 w - - 0 1", "8/8/8/6B1/8/8/4k3/1K5N b - - 0 1");
        //List<String> fenList =  List.of("1k1r3r/pp6/2P1bp2/2R1p3/Q3Pnp1/P2q4/1BR3B1/6K1 b - - 0 1");
        //List<String> fenList =  List.of(FENDecoder.INITIAL_FEN, "1k1r3r/pp6/2P1bp2/2R1p3/Q3Pnp1/P2q4/1BR3B1/6K1 b - - 0 1");

        return fenList.stream().map(FEN::of).toList();
    }

    private static CommandLine parseArguments(String[] args) {
        final Options options = new Options();
        Option inputOpt = Option
                .builder("r")
                .longOpt("rabbitHost")
                .hasArg()
                .argName("HOSTNAME/IP")
                .desc("rabbit host where messages are sent")
                .build();
        options.addOption(inputOpt);

        Option mainEngineOpt = Option
                .builder("e")
                .longOpt("engine")
                .required()
                .hasArg()
                .argName("ENGINE")
                .desc("main engine under test")
                .build();
        options.addOption(mainEngineOpt);

        Option matchTypeByDepth = Option
                .builder("d")
                .longOpt("MatchByDepth")
                .hasArg()
                .argName("DEPTH")
                .desc("match by depth")
                .build();
        options.addOption(matchTypeByDepth);


        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine cmdLine = parser.parse(options, args);

            if (!cmdLine.hasOption('d')) {
                throw new ParseException("No match type argument");
            }

            return cmdLine;

        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("ERROR: " + exp.getMessage());
            printHelp(options);
            System.exit(-1);
        }
        return null;
    }

    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();

        // Custom settings (optional)
        formatter.setWidth(100); // Set the display width

        // 3. Call printHelp()
        String cmdName = "my-cli-tool";
        String header = "\nCommand line utility for queueing matches.\n\nOptions:";
        String footer = "\nPlease report issues on GitHub.";

        formatter.printHelp(cmdName, header, options, footer, true);
    }
}
