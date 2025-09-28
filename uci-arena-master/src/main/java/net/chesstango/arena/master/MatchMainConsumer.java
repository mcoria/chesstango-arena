package net.chesstango.arena.master;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.worker.MatchResponse;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;


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

}
