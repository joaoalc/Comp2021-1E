package visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;

import table.MySymbolTable;
import table.ValueSymbol;

import static utils.Utils.getChildrenOfKind;

public class DeclarationVisitor extends PreorderJmmVisitor<List<Report>, Boolean> {
    private final MySymbolTable symbolTable = new MySymbolTable();

    public DeclarationVisitor() {
        addVisit("ImportDeclaration", this::importDeclaration);
        addVisit("ClassDeclaration", this::classDeclaration);
        //addVisit("VarDeclaration", this::varDeclaration);
        addVisit("MethodDeclaration", this::methodDeclaration);

        //addVisit("Less_Than", (node, reports) -> this.visitOp(node, reports)); // Method reference
        setDefaultVisit(this::defaultVisit); // Method reference
    }

    private boolean importDeclaration(JmmNode node, List<Report> reports) {
        String importString = node.get("name");

        for (JmmNode child : node.getChildren()) {
            importString += child.get("name");
        }
        symbolTable.addImport(importString);

        return true;
    }

    private boolean classDeclaration(JmmNode node, List<Report> reports) {
        String class_name = node.get("name");
        Optional<String> super_name = node.getOptional("super_name");

        symbolTable.setClassName(class_name);
        super_name.ifPresent(symbolTable::setSuperClassName);

        return true;
    }

    private boolean methodDeclaration(JmmNode node, List<Report> reports) {
        JmmNode return_type_node = node.getChildren().get(0);
        Type return_type = parseTypeNode(return_type_node);

        String name = node.get("name");

        List<JmmNode> argumentNodes = getChildrenOfKind(node, "Argument");
        List<ValueSymbol> arguments = new ArrayList<>();

        for (JmmNode argumentNode : argumentNodes) {
            JmmNode type_node = argumentNode.getChildren().get(0);
            Type type = parseTypeNode(type_node);
            String argument_name = argumentNode.get("name");
            arguments.add(new ValueSymbol(type, argument_name));
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

        return true;
    }

    private Type parseTypeNode(JmmNode node) {
        String type_name = node.get("name");
        boolean is_array = Boolean.parseBoolean(node.get("is_array"));

        return new Type(type_name, is_array);
    }

    public MySymbolTable getSymbolTable() {
        return symbolTable;
    }
}
