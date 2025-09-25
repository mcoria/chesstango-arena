package net.chesstango.tools.worker.match;


import com.rabbitmq.client.Channel;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.ShutdownSignalException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.function.Consumer;
import java.util.function.Function;

import static net.chesstango.tools.worker.match.MatchRequest.MATCH_REQUESTS_QUEUE_NAME;

/**
 * @author Mauricio Coria
 */
@Slf4j
class RequestConsumer {

    private final Channel channel;

    public RequestConsumer(Channel channel) {
        this.channel = channel;
    }


    public MatchRequest readMessage() throws IOException {
        do {
            GetResponse response = channel.basicGet(MATCH_REQUESTS_QUEUE_NAME, true);
            if (response != null) {
                return MatchRequest.decodeRequest(response.getBody());
            } else {
                try {
                    log.info("Waiting for MatchRequest");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    log.error("Interrupted while waiting for message", e);
                    throw new RuntimeException(e);
                }
            }
        } while (true);
    }

}

