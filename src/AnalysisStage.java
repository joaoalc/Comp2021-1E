
import java.util.*;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmAnalysis;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import visitor.OpVerifierVisitor;
//import visitor.OpVerifierVisitor;


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
        //System.out.println("To json: " + node.toJson());

        /*System.out.println("Dump tree with Visitor where you control tree traversal");
        ExampleVisitor visitor = new ExampleVisitor("Identifier", "id");
        System.out.println(visitor.visit(node, ""));

        System.out.println("Dump tree with Visitor that automatically performs preorder tree traversal");
        var preOrderVisitor = new ExamplePreorderVisitor("Identifier", "id");
        System.out.println(preOrderVisitor.visit(node, ""));
        */
/*        var opVisitor = new OpVerifierVisitor();
        List<Report> repor = new ArrayList<>();
        opVisitor.visitOp(node, repor);

        System.out.println(
                "Create histogram of node kinds with Visitor that automatically performs postorder tree traversal");
        var postOrderVisitor = new ExamplePostorderVisitor();
        var kindCount = new HashMap<String, Integer>();
        postOrderVisitor.visit(node, kindCount);
        System.out.println("Kinds count: " + kindCount + "\n");
*/      List<Report> reports = new ArrayList<>();

        OpVerifierVisitor opVerifierVisitor = new OpVerifierVisitor();
        opVerifierVisitor.visit(node, reports);

        // No Symbol Table being calculated yet
        return new JmmSemanticsResult(parserResult, null, reports);
    }
}