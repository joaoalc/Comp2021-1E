package stage;

import java.util.*;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import table.MySymbolTable;
import visitor.*;

public class AnalysisStage implements JmmAnalysis {
    public List<Report> report_list = new ArrayList<>();
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


        DeclarationVisitor declarationVerifierVisitor = new DeclarationVisitor();
        List<Report> reports = new ArrayList<>();
        declarationVerifierVisitor.visit(node, reports);



        MySymbolTable symbolTable = declarationVerifierVisitor.getSymbolTable();

        List<Report> expressionReports;
        // Verify binary operations
        ExpressionVisitor a_s_vis = new ExpressionVisitor(symbolTable, report_list);
        expressionReports = a_s_vis.visit(node, true);

        boolean o_optimization = true;

        ConstantFoldingVisitor c_f_vis = new ConstantFoldingVisitor(symbolTable, report_list);
        ConstantPropagationVisitor c_prop_vis = new ConstantPropagationVisitor(symbolTable, report_list);

        UnusedVariableVisitor unusedVar_vis = new UnusedVariableVisitor(symbolTable, report_list);

        if(o_optimization){
            do {
                c_f_vis.isChanged = false;
                c_prop_vis.isChanged = false;
                unusedVar_vis.isChanged = false;
                c_f_vis.visit(node, true);
                c_f_vis.makeChanges();
                c_prop_vis.visit(node, true);
                c_prop_vis.makeChanges();

            } while(c_prop_vis.isChanged || c_f_vis.isChanged || unusedVar_vis.isChanged);
            //unusedVar_vis.visit(node, false);
            //unusedVar_vis.makeChanges();
        }

        System.out.println("Reports: ");
        for(int i = 0; i < report_list.size(); i++){
            System.out.println(report_list.get(i));
        }
        return new JmmSemanticsResult(parserResult, symbolTable, report_list);
    }
}