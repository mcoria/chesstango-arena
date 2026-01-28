package net.chesstango.arena.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.core.MatchResult;
import net.chesstango.gardel.pgn.PGN;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * Por cada juego de Tango muestra estadísticas de cada arbol de búsqueda.
 *
 * @author Mauricio Coria
 */
@Slf4j
public class MatchToPGN {
    private final Path pgnFile;

    public MatchToPGN(Path matchStore) {
        pgnFile = matchStore.resolve("games.pgn");
    }


    public void save(List<MatchResult> matchResult) {
        try (BufferedWriter writer = Files.newBufferedWriter(pgnFile, StandardOpenOption.CREATE)) {
            List<PGN> pgnList = matchResult
                    .stream()
                    .map(MatchResult::pgn)
                    //.sorted(Comparator.comparing(o -> o.getFen().toString().concat(o.getWhite())))
                    .toList();
            for (PGN pgn : pgnList) {
                writer.write(pgn.toString());
                writer.newLine();
                writer.newLine();
            }
            log.info("Response serialized to file: {}", pgnFile.getFileName());
        } catch (IOException e) {
            System.err.println("Error appending to file: " + e.getMessage());
        }
    }
}
