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

        String rabbitHost = parsedArgs.getOptionValue("r");

        /*
        if (parsedArgs.hasOption('p')) {
            epdFilter.setFilter(new PlayerFilter(parsedArgs.getOptionValue('p')));
        } else if (parsedArgs.hasOption('b')) {
            epdFilter.setFilter(Predicate.not(new BookFilter(createBook(parsedArgs.getOptionValue('b')))));
        } else {
            throw new RuntimeException("Filter not found");
        }
         */

        new MatchMainProducer(rabbitHost).run();
    }

    private final String rabbitHost;

    public MatchMainProducer(String rabbitHost) {
        this.rabbitHost = rabbitHost;
    }

    @Override
    public void run() {
        log.info("Starting");

        List<MatchRequest> matchRequests = createMatchRequests(new MatchByDepth(4), fromPGN(), true);

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


    private static List<MatchRequest> createMatchRequests(MatchType match, List<FEN> fenList, boolean switchChairs) {
        String player1 = "class:WithTables";
        //String player1 = "file:Tango-v1.2.0";
        String player2 = "file:Spike";

        Stream<MatchRequest> result = fenList.stream()
                .map(fen -> new MatchRequest()
                        .setWhiteEngine(player1)
                        .setBlackEngine(player2)
                        .setFen(fen)
                        .setMatchType(match)
                        .setMatchId(UUID.randomUUID().toString())
                        .setSessionId(SESSION_DATE)
                );

        if (switchChairs) {
            Stream<MatchRequest> switchStream = fenList.stream()
                    .map(fen -> new MatchRequest()
                            .setWhiteEngine(player2)
                            .setBlackEngine(player1)
                            .setFen(fen)
                            .setMatchType(match)
                            .setMatchId(UUID.randomUUID().toString())
                            .setSessionId(SESSION_DATE)
                    );

            result = Stream.concat(result, switchStream);
        }

        return result.toList();
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
                .required()
                .hasArg()
                .argName("HOSTNAME/IP")
                .desc("Rabbit host where messages are sent")
                .build();
        options.addOption(inputOpt);

        /*
        Option player = Option.builder("p")
                .argName("player")
                .hasArg()
                .desc("Player name filter")
                .build();
        options.addOption(player);

        Option book = Option.builder("b")
                .argName("book")
                .hasArg()
                .desc("Book file")
                .build();
        options.addOption(book);
         */


        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            return parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            printHelp(options);
            System.exit(-1);
        }
        return null;
    }

    private static void printHelp(Options options){
        HelpFormatter formatter = new HelpFormatter();

        // Custom settings (optional)
        formatter.setWidth(100); // Set the display width

        // 3. Call printHelp()
        String cmdName = "my-cli-tool";
        String header = "\nA simple command line utility for file processing.\n\nOptions:";
        String footer = "\nPlease report issues on GitHub.";

        formatter.printHelp(cmdName, header, options, footer, true);
    }
}
