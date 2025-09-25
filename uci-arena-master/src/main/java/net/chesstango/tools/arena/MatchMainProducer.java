package net.chesstango.tools.arena;

import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import net.chesstango.board.Game;
import net.chesstango.gardel.fen.FEN;
import net.chesstango.gardel.fen.FENParser;
import net.chesstango.gardel.pgn.PGN;
import net.chesstango.gardel.pgn.PGNStringDecoder;
import net.chesstango.tools.worker.match.MatchRequest;
import net.chesstango.uci.arena.matchtypes.MatchByDepth;
import net.chesstango.uci.arena.matchtypes.MatchType;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static net.chesstango.tools.arena.common.Common.SESSION_DATE;

/**
 * @author Mauricio Coria
 */
@Slf4j
public class MatchMainProducer implements Runnable {

    public static void main(String[] args) {
        String rabbitHost = "localhost";

        new MatchMainProducer(rabbitHost).run();
    }

    private final String rabbitHost;

    public MatchMainProducer(String rabbitHost) {
        this.rabbitHost = rabbitHost;
    }

    @Override
    public void run() {
        log.info("Starting");

        List<MatchRequest> matchRequests = createMatchRequests(new MatchByDepth(4), getFEN(), true);

        try (ExecutorService executorService = Executors.newSingleThreadExecutor()) {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(rabbitHost);
            factory.setSharedExecutor(executorService);

            try (RequestProducer requestProducer = RequestProducer.open(factory)) {

                matchRequests.forEach(requestProducer::publish);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        log.info("Finished");
    }


    private static List<MatchRequest> createMatchRequests(MatchType match, List<FEN> fenList, boolean switchChairs) {
        String player1 = "class:WithTables";
        //String player1 = "file:Tango-v1.2.0";
        String player2 = "file:Spike";
        String matchId = UUID.randomUUID().toString();

        String player1Name = player1.replace("file:", "").replace("class:", "");
        String player2Name = player2.replace("file:", "").replace("class:", "");

        Stream<MatchRequest> result = fenList.stream()
                .map(fen -> new MatchRequest()
                        .setWhiteEngine(player1)
                        .setBlackEngine(player2)
                        .setFen(fen)
                        .setMatchType(match)
                        .setMatchId(String.format("%s-%s-vs-%s", matchId, player1Name, player2Name))
                        .setSessionId(SESSION_DATE)
                );

        if (switchChairs) {
            Stream<MatchRequest> switchStream = fenList.stream()
                    .map(fen -> new MatchRequest()
                            .setWhiteEngine(player2)
                            .setBlackEngine(player1)
                            .setFen(fen)
                            .setMatchType(match)
                            .setMatchId(String.format("%s-%s-vs-%s", matchId, player2Name, player1Name))
                            .setSessionId(SESSION_DATE)
                    );

            result = Stream.concat(result, switchStream);
        }

        return result.toList();
    }


    private static List<FEN> getFEN_FromPGN() {
        Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMainProducer.class.getClassLoader().getResourceAsStream("Balsa_Top10.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMasterMain.class.getClassLoader().getResourceAsStream("Balsa_Top25.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMasterMain.class.getClassLoader().getResourceAsStream("Balsa_Top50.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMasterMain.class.getClassLoader().getResourceAsStream("Balsa_v500.pgn"));
        //Stream<PGN> pgnStream = new PGNStringDecoder().decodePGNs(MatchMasterMain.class.getClassLoader().getResourceAsStream("Balsa_v2724.pgn"));

        return pgnStream
                .map(Game::from)
                .map(Game::getCurrentFEN)
                .toList();
    }


    private static List<FEN> getFEN() {
        List<String> fenList = List.of(FENParser.INITIAL_FEN);
        //List<String> fenList =  List.of("K7/N7/k7/8/3p4/8/N7/8 w - - 0 1", "8/8/8/6B1/8/8/4k3/1K5N b - - 0 1");
        //List<String> fenList =  List.of("1k1r3r/pp6/2P1bp2/2R1p3/Q3Pnp1/P2q4/1BR3B1/6K1 b - - 0 1");
        //List<String> fenList =  List.of(FENDecoder.INITIAL_FEN, "1k1r3r/pp6/2P1bp2/2R1p3/Q3Pnp1/P2q4/1BR3B1/6K1 b - - 0 1");

        return fenList.stream().map(FEN::of).toList();
    }
}
