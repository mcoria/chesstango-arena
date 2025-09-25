package net.chesstango.tools.arena;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.tools.arena.common.Common;
import net.chesstango.arena.worker.MatchResponse;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author Mauricio Coria
 */
@Slf4j
public class MatchMainConsumer implements Runnable {

    public static void main(String[] args) throws Exception {
        String rabbitHost = args[0];

        String directory = args[1];

        System.out.printf("matchStore={%s}\n", directory);

        Path matchStore = Path.of(directory);
        if (!Files.exists(matchStore) || !Files.isDirectory(matchStore)) {
            throw new RuntimeException("Directory not found: " + directory);
        }

        new MatchMainConsumer(rabbitHost, matchStore).run();
    }

    private final String rabbitHost;
    private final Path matchStore;

    public MatchMainConsumer(String rabbitHost, Path matchStore) {
        if (rabbitHost == null) {
            throw new IllegalArgumentException("rabbitHost and matchStore must be provided");
        }
        this.rabbitHost = rabbitHost;
        this.matchStore = matchStore;
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

                responseConsumer.setupQueueConsumer(this::accept);

                log.info("Waiting for MatchResponse");

                Thread.sleep(Long.MAX_VALUE);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        log.info("Done");
    }


    public void accept(MatchResponse matchResponse) {
        Path sessionDirectory = Common.createSessionDirectory(matchStore, matchResponse.getSessionId());

        log.info("Saving MatchResponse for {}", matchResponse.getSessionId());

        String filename = String.format("match_%s.ser", matchResponse.getMatchId());

        Path filePath = sessionDirectory.resolve(filename);

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath.toFile()))) {
            oos.writeObject(matchResponse);
            log.info("Response serialized to file: {}", filePath);
        } catch (IOException e) {
            log.error("Failed to serialize response", e);
            throw new RuntimeException(e);
        }
    }
}
