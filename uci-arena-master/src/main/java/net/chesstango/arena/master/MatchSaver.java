package net.chesstango.arena.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.master.common.Common;
import net.chesstango.arena.worker.MatchResponse;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.function.Consumer;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class MatchSaver implements Consumer<MatchResponse> {
    private final Path matchStore;

    public MatchSaver(Path matchStore) {
        this.matchStore = matchStore;
    }

    @Override
    public void accept(MatchResponse matchResponse) {
        Path sessionDirectory = Common.createSessionDirectory(matchStore, matchResponse.getSessionId());
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
