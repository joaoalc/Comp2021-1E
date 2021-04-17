package pt.up.fe.comp.jmm.ast.examples;

import java.util.List;
import java.util.stream.Collectors;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

public class OpVerifierVisitor extends PreorderJmmVisitor<List<Report>, Boolean> {

    public OpVerifierVisitor() {
        addVisit("Binary", (node, reports) -> this.visitOp(node, reports)); // Method reference
        //setDefaultVisit(this::defaultVisit); // Method reference
    }

    private Boolean visitOp(JmmNode node, List<Report> reports){
        System.out.println("OP: " + node + " -> " + node.get("op"));
        return true;
    }

    /*
    private String defaultVisit(JmmNode node, String space) {
        String content = space + node.getKind();
        String attrs = node.getAttributes()
                .stream()
                .filter(a -> !a.equals("line"))
                .map(a -> a + "=" + node.get(a))
                .collect(Collectors.joining(", ", "[", "]"));

        content += ((attrs.length() > 2) ? attrs : "") + "\n";
        for (JmmNode child : node.getChildren()) {
            content += visit(child, space + " ");
        }
        return content;
    }*/

}
