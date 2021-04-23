package visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import table.Method;
import table.MySymbolTable;

import static utils.Utils.getChildrenOfKind;

public class DeclarationVerifierVisitor extends PreorderJmmVisitor<List<Report>, Boolean> {
    public MySymbolTable symbolTable = new MySymbolTable();

    public DeclarationVerifierVisitor() {
        //TODO: consider other scopes I guess

        addVisit("ImportDeclaration", this::importDeclaration);
        addVisit("ClassDeclaration", this::classDeclaration);
        addVisit("VarDeclaration", this::varDeclaration);
        addVisit("MethodDeclaration", this::methodDeclaration);

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

        symbolTable.addImport(importString.toString());

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

    private boolean methodDeclaration(JmmNode node, List<Report> reports) {
        JmmNode return_type_node = node.getChildren().get(0);
        Type return_type = parseTypeNode(return_type_node);

        String name = node.get("name");

        List<JmmNode> argumentNodes = getChildrenOfKind(node, "Argument");
        List<Symbol> arguments = new ArrayList<>();

        for (JmmNode argumentNode : argumentNodes) {
            JmmNode type_node = argumentNode.getChildren().get(0);
            Type type = parseTypeNode(type_node);
            String argument_name = argumentNode.get("name");
            arguments.add(new Symbol(type, argument_name));
        }

        symbolTable.addMethod(return_type, name, arguments);

        return true;
    }

    private boolean varDeclaration(JmmNode node, List<Report> reports) {
        JmmNode typeNode = node.getChildren().get(0);
        Type type = parseTypeNode(typeNode);

        String name = node.get("name");

        symbolTable.addField(type, name);

        return true;
    }

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

    private Type parseTypeNode(JmmNode node) {
        String type_name = node.get("name");
        boolean is_array = Boolean.parseBoolean(node.get("is_array"));

        return new Type(type_name, is_array);
    }


}
