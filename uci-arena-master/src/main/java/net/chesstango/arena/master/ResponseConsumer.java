package net.chesstango.arena.master;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.worker.MatchResponse;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static net.chesstango.arena.worker.MatchResponse.MATCH_RESPONSES_QUEUE_NAME;

/**
 * @author Mauricio Coria
 */

@Slf4j
public class ResponseConsumer implements AutoCloseable {

    private final Connection connection;

    private final Channel channel;

    private String cTag;


    public ResponseConsumer(ConnectionFactory factory) throws IOException, TimeoutException {
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.channel.basicQos(1);
    }

    @Override
    public void close() throws Exception {
        channel.basicCancel(cTag);
        channel.close();
        connection.close();
    }


    public void setupQueueConsumer(Consumer<MatchResponse> matchResponseConsumer) {
        try {
            cTag = channel.basicConsume(MATCH_RESPONSES_QUEUE_NAME, true, (consumerTag, delivery) -> {

                MatchResponse response = MatchResponse.decodeResponse(delivery.getBody());

                matchResponseConsumer.accept(response);

            }, consumerTag -> {
                log.info("Queue consumer cancelled {}", cTag);
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
