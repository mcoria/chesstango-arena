package net.chesstango.arena.core.reports;


import net.chesstango.gardel.pgn.PGN;
import net.chesstango.reports.evaluation.EvaluationReport;
import net.chesstango.reports.evaluation.EvaluationReportModel;
import net.chesstango.reports.nodes.NodesReport;
import net.chesstango.reports.nodes.NodesReportModel;
import net.chesstango.reports.pv.PrincipalVariationReport;
import net.chesstango.reports.pv.PrincipalVariationReportModel;
import net.chesstango.arena.core.MatchResult;

import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Por cada juego de Tango muestra estadísticas de cada búsqueda.
 *
 * @author Mauricio Coria
 */
public class SearchesReport {
    private final NodesReport nodesReport = new NodesReport();
    private final EvaluationReport evaluationReport = new EvaluationReport();
    private final PrincipalVariationReport principalVariationReport = new PrincipalVariationReport();
    private final List<ReportModels> reportModels = new LinkedList<>();
    private boolean withPrincipalVariation;
    private boolean withEvaluationReport;
    private boolean withNodesReport;

    public SearchesReport printReport(PrintStream out) {
        reportModels.forEach(reportModel -> {
            if (withNodesReport) {
                nodesReport.setReportModel(reportModel.nodesReportModel())
                        .printReport(out);
            }

            if (withEvaluationReport) {
                evaluationReport.setReportModel(reportModel.evaluationReportModel())
                        .printReport(out);
            }

            if (withPrincipalVariation) {
                principalVariationReport.setReportModel(reportModel.principalVariationReportModel())
                        .printReport(out);
            }
        });
        return this;
    }

    public SearchesReport withMathResults(List<MatchResult> matchResult) {
        matchResult.stream()
                .filter(result -> result.whiteSearches() != null)
                .forEach(result -> {
                    PGN pgn = result.pgn();
                    String engineName = pgn.getWhite();
                    NodesReportModel nodesReportModel = NodesReportModel.collectStatistics(String.format("%s - %s", engineName, pgn.getEvent()), result.whiteSearches());
                    EvaluationReportModel evaluationReportModel = EvaluationReportModel.collectStatistics(String.format("%s - %s", engineName, pgn.getEvent()), result.whiteSearches());
                    PrincipalVariationReportModel principalVariationReportModel = PrincipalVariationReportModel.collectStatics(String.format("%s - %s", engineName, pgn.getEvent()), result.whiteSearches());
                    reportModels.add(new ReportModels(nodesReportModel, evaluationReportModel, principalVariationReportModel));
                });

        matchResult.stream()
                .filter(result -> result.blackSearches() != null)
                .forEach(result -> {
                    PGN pgn = result.pgn();
                    String engineName = pgn.getBlack();
                    NodesReportModel nodesReportModel = NodesReportModel.collectStatistics(String.format("%s - %s", engineName, pgn.getEvent()), result.blackSearches());
                    EvaluationReportModel evaluationReportModel = EvaluationReportModel.collectStatistics(String.format("%s - %s", engineName, pgn.getEvent()), result.blackSearches());
                    PrincipalVariationReportModel principalVariationReportModel = PrincipalVariationReportModel.collectStatics(String.format("%s - %s", engineName, pgn.getEvent()), result.blackSearches());
                    reportModels.add(new ReportModels(nodesReportModel, evaluationReportModel, principalVariationReportModel));
                });
        return this;
    }


    public SearchesReport withCutoffStatistics() {
        nodesReport.withCutoffStatistics();
        this.withNodesReport = true;
        return this;
    }

    public SearchesReport withNodesVisitedStatistics() {
        nodesReport.withNodesVisitedStatistics();
        this.withNodesReport = true;
        return this;
    }


    public SearchesReport withEvaluationReport() {
        this.withEvaluationReport = true;
        return this;
    }

    public SearchesReport withPrincipalVariation() {
        this.withPrincipalVariation = true;
        return this;
    }

    private record ReportModels(NodesReportModel nodesReportModel, EvaluationReportModel evaluationReportModel,
                                PrincipalVariationReportModel principalVariationReportModel) {
    }

    ;
}
