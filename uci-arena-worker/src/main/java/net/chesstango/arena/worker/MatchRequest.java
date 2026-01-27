package net.chesstango.arena.worker;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.chesstango.arena.core.matchtypes.MatchType;
import net.chesstango.gardel.pgn.PGN;

import java.io.*;

/**
 * @author Mauricio Coria
 */
@Setter
@Getter
@Accessors(chain = true)
public class MatchRequest implements Serializable {
    public final static String MATCH_REQUESTS_QUEUE_NAME = "match_requests";

    @Serial
    private static final long serialVersionUID = 1L;

    private String sessionId;
    private String matchId;
    private String whiteEngine;
    private String blackEngine;
    private MatchType matchType;
    private PGN pgn;


    public byte[] encodeRequest() {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);) {
            oos.writeObject(this);
            oos.flush();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static MatchRequest decodeRequest(byte[] request) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(request);
             ObjectInputStream ois = new ObjectInputStream(bis);) {
            return (MatchRequest) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "MatchRequest{" + "sessionId=" + sessionId + ", matchId=" + matchId + ", whiteEngine=" + whiteEngine + ", blackEngine=" + blackEngine + ", matchType=" + matchType + ", pgn=" + pgn + '}';
    }
}
