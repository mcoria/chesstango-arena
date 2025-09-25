package net.chesstango.tools.worker.match;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class MatchMainWorker implements Runnable {

    public static void main(String[] args) {
        String rabbitHost = System.getenv("RABBIT_HOST");
        String enginesCatalog = System.getenv("ENGINE_CATALOG");

        new MatchMainWorker(rabbitHost, enginesCatalog).run();
    }

    private final String rabbitHost;
    private final String enginesCatalog;

    public MatchMainWorker(String rabbitHost, String enginesCatalog) {
        if (rabbitHost == null || enginesCatalog == null) {
            throw new IllegalArgumentException("rabbitHost and enginesCatalog must be provided");
        }
        this.rabbitHost = rabbitHost;
        this.enginesCatalog = enginesCatalog;
    }

    @Override
    public void run() {
        log.info("To exit press CTRL+C");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitHost);
        factory.setUsername("guest");
        factory.setPassword("guest");

        log.info("Connecting to RabbitMQ");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            channel.basicQos(1);

            log.info("Connected to RabbitMQ");

            try (ControllerProvider controllerProvider = ControllerProvider.create(enginesCatalog)) {
                MatchWorker matchWorker = new MatchWorker(controllerProvider);

                RequestConsumer requestConsumer = new RequestConsumer(channel);

                ResponseProducer responseProducer = new ResponseProducer(channel);

                log.info("Waiting for MatchRequest");

                do {
                    MatchRequest request = requestConsumer.readMessage();
                    log.info("[{}] Received MatchRequest: {}", request.getMatchId(), request);
                    MatchResponse response = matchWorker.apply(request);
                    responseProducer.publish(response);
                } while (true);

                //log.info("Waiting for MatchRequest completed");
            }
        } catch (IOException | TimeoutException e) {
            log.error("Error", e);
        }


        log.info("Done");
    }
}
