package net.chesstango.arena.master;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.core.matchtypes.MatchByDepth;
import net.chesstango.arena.core.matchtypes.MatchType;
import net.chesstango.arena.master.common.MatchSide;
import net.chesstango.arena.worker.MatchRequest;
import net.chesstango.board.Game;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.pgn.PGNStringDecoder;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
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

    /**
     * Example:
     * -d 2 -e "file:Spike" -o "file:Spike" -p "C:\java\projects\chess\chess-utils\testing\matches\Balsa_Top10.pgn"
     * -d 2 -s white -e "class:WithTables" -o "file:Stockfish" -f "C:\\java\\projects\\chess\\chess-utils\\testing\\matches\\LumbrasGigaBase\\LumbrasGigaBase_OTB_2025_5_pieces_finalLessThan6_blackWins.fen"
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

        MatchSide matchSide = MatchSide.BOTH;
        matchSide = switch (parsedArgs.getOptionValue("s", "both")) {
            case "white" -> MatchSide.WHITE_ONLY;
            case "black" -> MatchSide.BLACK_ONLY;
            case "both" -> MatchSide.BOTH;
            default -> throw new IllegalArgumentException("Invalid match side: " + parsedArgs.getOptionValue("s"));
        };
        log.info("MatchSide: {}", matchSide);

        List<FEN> fenList = parsedArgs.hasOption('f')
                ? fromFEN(parsedArgs.getOptionValue("f"))
                : fromPGN(parsedArgs.getOptionValue('p'));

        log.info("FEN size: {}", fenList.size());

        new MatchMainProducer(rabbitHost, engine, opponents, matchType, matchSide, fenList)
                .run();
    }

    private final String rabbitHost;
    private final String engine;
    private final MatchType matchType;
    private final List<String> opponents;
    private final List<FEN> fenList;
    private final MatchSide side;

    public MatchMainProducer(String rabbitHost, String engine, List<String> opponents, MatchType matchType, MatchSide side, List<FEN> fenList) {
        this.rabbitHost = rabbitHost;
        this.engine = engine;
        this.matchType = matchType;
        this.side = side;
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

            if (side == MatchSide.BOTH || side == MatchSide.WHITE_ONLY) {
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
            }

            if (side == MatchSide.BOTH || side == MatchSide.BLACK_ONLY) {
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

        Option matchSide = Option
                .builder("s")
                .longOpt("MatchSide")
                .hasArg()
                .argName("SIDE")
                .desc("both | white | black")
                .build();
        options.addOption(matchSide);

        Option fenList = Option
                .builder("f")
                .longOpt("fenList")
                .hasArgs()
                .argName("FILE")
                .desc("FEN file path")
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

        String cmdName = "MatchMainProducer";
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

    private static List<FEN> fromFEN(String fenFile) {
        List<FEN> fens = new LinkedList<>();
        Path filePath = Paths.get(fenFile);
        try (Stream<String> lines = Files.lines(filePath)) {
            lines.filter(s -> s != null && !s.trim().isEmpty())
                    .map(FEN::of)
                    .forEach(fens::add);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(-1);
        }
        return fens;
    }
}
