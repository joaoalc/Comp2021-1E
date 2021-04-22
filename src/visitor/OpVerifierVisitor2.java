/*package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import java.util.List;

public class OpVerifierVisitor extends PreorderJmmVisitor<List<Report>, Boolean> {
    public OpVerifierVisitor() {
        addVisit("Operation", this::visitOp);
    }

    public boolean visitOp(JmmNode node, List<Report> reports) {
        switch (node.get("op")) {
            case "PLUS":
                verifyInt(node, reports);
                break;

            default:
                System.out.println("Not implemented yet");
                break;
        }

        return true;
    }

    private void verifyInt(JmmNode binaryOperation, List<Report> reports) {
        List<JmmNode> children = binaryOperation.getChildren();
        JmmNode left = children.get(0);
        JmmNode right = children.get(1);

        System.out.println("CHILD LEFT:" + left);
        System.out.println("CHILD RIGHT:" + right);
    }
}
*/