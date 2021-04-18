package pt.up.fe.comp.jmm.ast.examples;

import java.util.List;
import java.util.stream.Collectors;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

public class OpVerifierVisitor extends PreorderJmmVisitor<List<Report>, Boolean> {

    public OpVerifierVisitor() {
        System.out.println("OpVerifierVisitor got called");
        //addVisit("Call", (node, reports) -> this.visitOp(node, reports)); // Method reference
        addVisit("Less_Than", (node, reports) -> this.visitOp(node, reports)); // Method reference
        setDefaultVisit(this::defaultVisit); // Method reference
    }

    private Boolean visitOp(JmmNode node, List<Report> reports){
        //System.out.println("OP: " + node + " -> " + node.get("op"));
        System.out.println("Found node: " + node.getKind());
        var children = node.getChildren();
        System.out.println("Has n children: " + children.size());

        System.out.println("LEFT CHILD: " + children.get(0));
        System.out.println("RIGHT CHILD: " + children.get(1));
        return true;
    }


    private Boolean defaultVisit(JmmNode node, List<Report> reports) {
        System.out.println("Node: " + node.getKind());
        //String content = space + node.getKind();
        /*String attrs = node.getAttributes()
                .stream()
                .filter(a -> !a.equals("line"))
                .map(a -> a + "=" + node.get(a))
                .collect(Collectors.joining(", ", "[", "]"));

        //content += ((attrs.length() > 2) ? attrs : "") + "\n";
        for (JmmNode child : node.getChildren()) {
            visit(child, reports);
        }*/
        return true;
    }

}
