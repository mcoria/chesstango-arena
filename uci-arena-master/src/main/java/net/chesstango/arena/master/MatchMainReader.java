package net.chesstango.arena.master;

import lombok.extern.slf4j.Slf4j;
import net.chesstango.arena.core.MatchResult;
import net.chesstango.arena.core.reports.MatchesReport;
import net.chesstango.arena.core.reports.SearchesReport;
import net.chesstango.arena.core.reports.SessionReport;
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

    public static void main(String[] args) {
        List<MatchResponse> matchResponses = loadMatchResponses("C:\\java\\projects\\chess\\chess-utils\\testing\\matches\\2025-10-15-08-16");

        List<MatchResult> matchResult = matchResponses.stream().map(MatchResponse::getMatchResult).toList();

        new MatchesReport()
                .withMatchResults(matchResult)
                //.withMatchResult(List.of(engineController1, engineController2), matchResult)
                .sortBy(Comparator.comparing(MatchesReport.ReportRowModel::getWinPercentage).reversed())
                .printReport(System.out);


        /*
        new SessionReport()
                //.withCollisionStatistics()
                .withNodesVisitedStatistics()
                .withCutoffStatistics()
                .breakByColor()
                .withMathResults(matchResult)
                .printReport(System.out);
        */


        new SearchesReport()
                //.withCutoffStatistics()
                .withNodesVisitedStatistics()
                .withPrincipalVariation()
                .withMathResults(matchResult)
                .printReport(System.out);

    }

    public static List<MatchResponse> loadMatchResponses(String directoryStr) {
        List<MatchResponse> matchResponses = new LinkedList<>();

        Path directory = Path.of(directoryStr);

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
