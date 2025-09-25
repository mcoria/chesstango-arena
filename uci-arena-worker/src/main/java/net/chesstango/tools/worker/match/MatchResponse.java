package net.chesstango.tools.worker.match;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.uci.arena.MatchResult;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author Mauricio Coria
 */
@Setter
@Getter
@Accessors(chain = true)
public class MatchResponse implements Serializable {
    public final static String MATCH_RESPONSES_QUEUE_NAME = "match_responses";

    @Serial
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private String matchId;
    private String whiteEngineName;
    private String blackEngineName;
    private MatchResult matchResult;


    public byte[] encodeResponse() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);) {
            oos.writeObject(this);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MatchResponse decodeResponse(byte[] bytes) {
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (MatchResponse) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException("Error deserializing object", e);
        }
    }
}
