package net.chesstango.arena.master;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.core.matchtypes.MatchByDepth;
import net.chesstango.arena.core.matchtypes.MatchType;
import net.chesstango.arena.worker.MatchRequest;
import net.chesstango.board.Game;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.pgn.PGNStringDecoder;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.chesstango.arena.master.common.Common.SESSION_DATE;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class MatchMainProducer implements Runnable {

    /**
     * Example: -d 2 -e "class:WithTables" -o "file:Spike" -p "C:\java\projects\chess\chess-utils\testing\matches\Balsa_Top10.pgn"
     */
    public static void main(String[] args) {
        CommandLine parsedArgs = parseArguments(args);

        String rabbitHost = parsedArgs.getOptionValue("r", "localhost");
        log.info("Rabbit: {}", rabbitHost);

        String engine = parsedArgs.getOptionValue("e");
        log.info("Engine: {}", engine);

        List<String> opponents = Arrays.stream(parsedArgs.getOptionValues("o")).toList();
        log.info("Opponents: {}", opponents);

        MatchType matchType = null;
        if (parsedArgs.hasOption('d')) {
            matchType = new MatchByDepth(Integer.parseInt(parsedArgs.getOptionValue("d")));
            log.info("Match: {}", matchType);
        }

        List<FEN> fenList = parsedArgs.hasOption('f')
                ? Arrays.stream(parsedArgs.getOptionValues("f")).map(FEN::of).toList()
                : fromPGN(parsedArgs.getOptionValue('p'));

        log.info("FEN size: {}", fenList.size());

        new MatchMainProducer(rabbitHost, engine, opponents, matchType, fenList)
                .run();
    }

    private final String rabbitHost;
    private final String engine;
    private final MatchType matchType;
    private final List<String> opponents;
    private final List<FEN> fenList;

    public MatchMainProducer(String rabbitHost, String engine, List<String> opponents, MatchType matchType, List<FEN> fenList) {
        this.rabbitHost = rabbitHost;
        this.engine = engine;
        this.matchType = matchType;
        this.opponents = opponents;
        this.fenList = fenList;
    }

    @Override
    public void run() {
        List<MatchRequest> matchRequests = createMatchRequests();
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
    }


    private List<MatchRequest> createMatchRequests() {
        List<MatchRequest> matchRequests = new LinkedList<>();

        for (String opponent : opponents) {
            fenList.stream()
                    .map(fen -> new MatchRequest()
                            .setWhiteEngine(engine)
                            .setBlackEngine(opponent)
                            .setFen(fen)
                            .setMatchType(matchType)
                            .setMatchId(UUID.randomUUID().toString())
                            .setSessionId(SESSION_DATE)
                    )
                    .peek(request -> log.info("{}", request.toString()))
                    .forEach(matchRequests::add);

            fenList.stream()
                    .map(fen -> new MatchRequest()
                            .setWhiteEngine(opponent)
                            .setBlackEngine(engine)
                            .setFen(fen)
                            .setMatchType(matchType)
                            .setMatchId(UUID.randomUUID().toString())
                            .setSessionId(SESSION_DATE)
                    )
                    .peek(request -> log.info("{}", request.toString()))
                    .forEach(matchRequests::add);
        }

        return matchRequests;
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

        Option opponents = Option
                .builder("o")
                .longOpt("opponents")
                .required()
                .hasArgs()
                .argName("OPPONENT...")
                .desc("opponents list")
                .build();
        options.addOption(opponents);

        Option matchTypeByDepth = Option
                .builder("d")
                .longOpt("MatchByDepth")
                .hasArg()
                .argName("DEPTH")
                .desc("match by depth")
                .build();
        options.addOption(matchTypeByDepth);

        Option fenList = Option
                .builder("f")
                .longOpt("fenList")
                .hasArgs()
                .argName("FEN...")
                .desc("fen list")
                .build();
        options.addOption(fenList);

        Option pgnFile = Option
                .builder("p")
                .longOpt("pgnFile")
                .hasArg()
                .argName("FILE")
                .desc("PNG file path")
                .build();
        options.addOption(pgnFile);

        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments
            CommandLine cmdLine = parser.parse(options, args);
            if (!cmdLine.hasOption('d')) {
                throw new ParseException("No match type argument");
            }
            if (!cmdLine.hasOption('f') && !cmdLine.hasOption('p')) {
                throw new ParseException("FEN nor PGN file option present");
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

        formatter.setWidth(100); // Set the display width

        String cmdName = "my-cli-tool";
        String header = "\nCommand line utility for queueing matches.\n\nOptions:";
        String footer = "\nPlease report issues on GitHub.";

        formatter.printHelp(cmdName, header, options, footer, true);
    }

    private static List<FEN> fromPGN(String pgnFile) {
        try {
            return new PGNStringDecoder()
                    .decodePGNs(Path.of(pgnFile))
                    .map(Game::from)
                    .map(Game::getCurrentFEN)
                    .toList();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
