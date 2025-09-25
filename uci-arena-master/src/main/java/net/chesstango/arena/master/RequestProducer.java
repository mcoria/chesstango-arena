package net.chesstango.arena.master;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.worker.MatchRequest;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static net.chesstango.arena.worker.MatchRequest.MATCH_REQUESTS_QUEUE_NAME;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class RequestProducer implements AutoCloseable {

    private final Connection connection;
    private final Channel channel;

    public static RequestProducer open(ConnectionFactory factory) throws IOException, TimeoutException {
        return new RequestProducer(factory);
    }

    RequestProducer(ConnectionFactory factory) throws IOException, TimeoutException {
        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        this.channel.queueDeclare(MATCH_REQUESTS_QUEUE_NAME, false, false, false, null);
    }

    @Override
    public void close() throws Exception {
        channel.close();
        connection.close();
    }

    public void publish(MatchRequest matchRequest) {
        try {
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .build();

            byte[] message = matchRequest.encodeRequest();

            channel.basicPublish("", MATCH_REQUESTS_QUEUE_NAME, props, message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
