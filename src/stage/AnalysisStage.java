package stage;

import java.util.*;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import table.MySymbolTable;
import visitor.*;

public class AnalysisStage implements JmmAnalysis {
    @Override
    public JmmSemanticsResult semanticAnalysis(JmmParserResult parserResult) {
        if (TestUtils.getNumReports(parserResult.getReports(), ReportType.ERROR) > 0) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started semantic analysis but there are errors from previous stage");
            return new JmmSemanticsResult(parserResult, null, Collections.singletonList(errorReport));
        }

        if (parserResult.getRootNode() == null) {
            var errorReport = new Report(ReportType.ERROR, Stage.SEMANTIC, -1,
                    "Started semantic analysis but AST root node is null");
            return new JmmSemanticsResult(parserResult, null, Collections.singletonList(errorReport));
        }

        JmmNode node = parserResult.getRootNode();

        List<Report> reports = new ArrayList<>();

        DeclarationVisitor declarationVerifierVisitor = new DeclarationVisitor();
        declarationVerifierVisitor.visit(node, reports);

        MySymbolTable symbolTable = declarationVerifierVisitor.getSymbolTable();

        OperationVisitor operationVerifierVisitor = new OperationVisitor();
        operationVerifierVisitor.visit(node);

        VarDeclarationVisitor mvec = new VarDeclarationVisitor(declarationVerifierVisitor.getSymbolTable());
        mvec.visit(node);

        MultDivVisitor m_d_vis = new MultDivVisitor(symbolTable);
        m_d_vis.visit(node);

        SumSubVisitor a_s_vis = new SumSubVisitor(symbolTable);
        a_s_vis.visit(node);

        MethodVerifier mvef = new MethodVerifier(symbolTable);
        mvef.visit(node, reports);

        // No Symbol Table being calculated yet
        return new JmmSemanticsResult(parserResult, null, reports);
    }
}