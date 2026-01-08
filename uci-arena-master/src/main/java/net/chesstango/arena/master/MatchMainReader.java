package net.chesstango.arena.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.core.MatchResult;
import net.chesstango.arena.core.reports.MatchesReport;
import net.chesstango.arena.worker.MatchResponse;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class MatchMainReader {

    private static final Path responsesStore = Path.of("C:\\java\\projects\\chess\\chess-utils\\testing\\matches\\2026-01-08-01-14");

    public static void main(String[] args) {
        List<MatchResponse> matchResponses = loadMatchResponses(responsesStore);

        List<MatchResult> matchResult = matchResponses.stream().map(MatchResponse::getMatchResult).toList();

        new MatchesReport()
                .withMatchResults(matchResult)
                //.withMatchResult(List.of(engineController1, engineController2), matchResult)
                .sortBy(Comparator.comparing(MatchesReport.ReportRowModel::getWinPercentage).reversed())
                .printReport(System.out);

         /*
        new MatchesSearchesReport()
                //.breakByGame()
                //.breakByColor()
                .withMathResults(matchResult)
                .printReport(System.out);

        new MatchesSearchesByTreeSummaryReport()
                //.withCollisionStatistics()
                .withNodesVisitedStatistics()
                .withCutoffStatistics()
                .breakByColor()
                .withMathResults(matchResult)
                .printReport(System.out);
        */

        /*
        new MatchesSearchesByTreeDetailReport()
                //.withCutoffStatistics()
                .withNodesVisitedStatistics()
                .withPrincipalVariationReport()
                .withMathResults(matchResult)
                .printReport(System.out);
         */

        new MathesToPGN(responsesStore)
                .save(matchResult);
    }

    public static List<MatchResponse> loadMatchResponses(Path directory) {
        List<MatchResponse> matchResponses = new LinkedList<>();
        try (Stream<Path> files = Files.list(directory)) {
            files.forEach(file -> {
                try {
                    log.info("File: {}", file.getFileName());
                    MatchResponse matchResponse = deserializeFromFile(file);

                    matchResponses.add(matchResponse);
                } catch (IOException e) {
                    log.error("Error reading file: " + file.getFileName(), e);
                }
            });
        } catch (IOException e) {
            log.error("Error listing directory: " + directory, e);
        }
        return matchResponses;
    }

    private static MatchResponse deserializeFromFile(Path file) throws IOException {
        byte[] bytes = Files.readAllBytes(file);
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            return (MatchResponse) ois.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException("Error deserializing object", e);
        }
    }

}
