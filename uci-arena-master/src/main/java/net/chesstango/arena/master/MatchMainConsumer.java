package net.chesstango.arena.master;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.worker.MatchResponse;
import net.chesstango.board.Game;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.pgn.PGNStringDecoder;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


/**
 * @author Mauricio Coria
 */
@Slf4j
public class MatchMainConsumer implements Runnable {

    /**
     * Example: -r localhost -s C:\java\projects\chess\chess-utils\testing\matches
     */
    public static void main(String[] args) {
        CommandLine parsedArgs = parseArguments(args);

        String rabbitHost = parsedArgs.getOptionValue("r", "localhost");
        log.info("Rabbit: {}", rabbitHost);

        String matchStoreDirectory = parsedArgs.getOptionValue("s");
        log.info("Store: {}", matchStoreDirectory);

        Path matchStore = Path.of(matchStoreDirectory);
        if (!Files.exists(matchStore) || !Files.isDirectory(matchStore)) {
            throw new RuntimeException("Directory not found: " + matchStoreDirectory);
        }

        new MatchMainConsumer(rabbitHost, new MatchSaver(matchStore)).run();
    }

    private final String rabbitHost;
    private final Consumer<MatchResponse> matchResponseConsumer;

    public MatchMainConsumer(String rabbitHost, Consumer<MatchResponse> matchResponseConsumer) {
        if (rabbitHost == null) {
            throw new IllegalArgumentException("rabbitHost and matchStore must be provided");
        }
        this.rabbitHost = rabbitHost;
        this.matchResponseConsumer = matchResponseConsumer;
    }

    @Override
    public void run() {
        log.info("To exit press CTRL+C");

        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitHost);
            factory.setUsername("guest");
            factory.setPassword("guest");
            factory.setSharedExecutor(executorService);

            log.info("Connecting to RabbitMQ");
            try (ResponseConsumer responseConsumer = new ResponseConsumer(factory)) {

                log.info("Connected to RabbitMQ");

                responseConsumer.setupQueueConsumer(matchResponseConsumer);

                log.info("Waiting for MatchResponse");

                Thread.sleep(Long.MAX_VALUE);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Done");
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

        Option storeOpt = Option
                .builder("s")
                .longOpt("store")
                .hasArg()
                .required()
                .argName("DIRECTORY")
                .desc("store directory")
                .build();
        options.addOption(storeOpt);


        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments

            return parser.parse(options, args);
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

        String cmdName = "MatchMainConsumer";
        String header = "\nCommand line utility for queueing matches.\n\nOptions:";
        String footer = "\nPlease report issues on GitHub.";

        formatter.printHelp(cmdName, header, options, footer, true);
    }


}
