package net.chesstango.arena.worker;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

import static net.chesstango.arena.worker.MatchResponse.MATCH_RESPONSES_QUEUE_NAME;


/**
 * @author Mauricio Coria
 */
@Slf4j
public class ResponseProducer {
    private final Channel channel;

    public ResponseProducer(Channel channel) throws IOException {
        this.channel = channel;
        channel.queueDeclare(MATCH_RESPONSES_QUEUE_NAME, false, false, false, null);
    }

    public void publish(MatchResponse matchResponse) {
        try {
            AMQP.BasicProperties props = new AMQP.BasicProperties
                    .Builder()
                    .build();
            byte[] message = matchResponse.encodeResponse();
            channel.basicPublish("", MATCH_RESPONSES_QUEUE_NAME, props, message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
