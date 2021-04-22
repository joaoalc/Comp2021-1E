package visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import table.MySymbolTable;

public class DeclarationVerifierVisitor extends PreorderJmmVisitor<List<Report>, Boolean> {
    public MySymbolTable symbolTable = new MySymbolTable();

    public DeclarationVerifierVisitor() {
        //addVisit("Call", (node, reports) -> this.visitOp(node, reports)); // Method reference

        //TODO: consider other scopes I guess
        //addVisit("WhileStatement", (node, reports) -> this.visitOp(node, reports));
        //addVisit("IfStatement", (node, reports) -> this.visitOp(node, reports));

        addVisit("ImportDeclaration", this::importDeclaration);
        addVisit("ClassDeclaration", this::classDeclaration);
        addVisit("VarDeclaration", this::varDeclaration);

        //addVisit("Less_Than", (node, reports) -> this.visitOp(node, reports)); // Method reference
        setDefaultVisit(this::defaultVisit); // Method reference
    }

    private boolean importDeclaration(JmmNode node, List<Report> reports) {
        // TODO: Probably we will not want a simple string in the future
        StringBuilder importString = new StringBuilder();

        // Get the first identifier's name
        importString.append(node.get("name"));

        // Get the other identifiers' names after each dot
        for (JmmNode child : node.getChildren()) {
            String importIdentifier = child.get("name");

            importString.append(".").append(importIdentifier);
        }

        return true;
    }

    private boolean classDeclaration(JmmNode node, List<Report> reports) {
        String class_name = node.get("name");
        Optional<String> super_name = node.getOptional("super_name");

        symbolTable.setClassName(class_name);
        super_name.ifPresent(s -> symbolTable.setSuperClassName(s));

        System.out.println("Class name: " + symbolTable.getClassName());
        System.out.println("Superclass name: " + symbolTable.getSuper());

        return true;
    }

    private boolean varDeclaration(JmmNode node, List<Report> reports) {
        // Type() <IDENTIFIER> {} <SEMICOLON>

//        symbolTable.add

        return true;
    }

    /*
    private boolean addToTable(JmmNode node, List<Report> reports) {
        var children = node.getChildren();

        if (children.size() == 2) {
            String type;
            if (node.getChildren().get(0).getKind().compareTo("Int") == 0) {
                type = "int";
            }
            else if (node.getChildren().get(0).getKind().compareTo("Boolean") == 0) {
                type = "boolean";
            }
            else if (node.getChildren().get(0).getKind().compareTo("IntArray") == 0) {
                type = "int[]";
            }
            else {
                type = node.getChildren().get(0).get("name");
            }

            String name = "abc";
            name = node.getChildren().get(1).get("name");

            ArrayList<String> arr = new ArrayList<>();
            arr.add(type);
            if (symbolTable.(name, arr) == 0) {
                return true;
            }
            else {
                Report rep = new Report(ReportType.ERROR, Stage.SEMANTIC, 0, "Variable " + name + " is already declared in scope.");
                reports.add(rep);
                return false;
            }
        }
        else {
            System.out.println("Has n children: " + children.size());
            return false;
        }
        //symbolTable.insert(node.get(), );
    }*/

    private Boolean visitOp(JmmNode node, List<Report> reports) {
        //System.out.println("OP: " + node + " -> " + node.get("op"));
        System.out.println("Found node: " + node.getKind());
        var children = node.getChildren();
        System.out.println("Has n children: " + children.size());

        System.out.println("LEFT CHILD: " + children.get(0));
        System.out.println("RIGHT CHILD: " + children.get(1));

        return true;
    }


    private Boolean defaultVisit(JmmNode node, List<Report> reports) {
        /*System.out.println("Node: " + node.getKind());
        System.out.println("Node has " + node.getAttributes().size() + " attributes.");
        for (String entry : node.getAttributes()) {
            System.out.println(entry);
        }*/
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
