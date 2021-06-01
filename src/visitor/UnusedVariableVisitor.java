package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import table.Method;
import table.MySymbolTable;
import utils.NodeFindingMethods;
import utils.Utils;

import java.lang.reflect.Array;
import java.util.*;

import static utils.Utils.getChildrenOfKind;

public class UnusedVariableVisitor extends AJmmVisitor<Boolean, List<Report>> {

    public HashMap<String, List<JmmNode>> unusedVariables = new HashMap<>();

    public List<Runnable> toRemove = new ArrayList<>();

    public List<Report> report_list;

    public MySymbolTable symbolTable;

    public boolean isChanged = false;

    public UnusedVariableVisitor(MySymbolTable symbolTable, List<Report> report_list) {
        this.symbolTable = symbolTable;
        this.report_list = report_list;

        addVisit("Add", this::verifyExpression);
        addVisit("Sub", this::verifyExpression);
        addVisit("Mult", this::verifyExpression);
        addVisit("Div", this::verifyExpression);
        addVisit("LessThan", this::verifyExpression);
        addVisit("And", this::verifyExpression);
        addVisit("Parentheses", this::verifyExpression);
        addVisit("Index", this::verifyExpression);
        addVisit("FCall", this::verifyExpression);
        addVisit("Assignment", this::verifyExpression);
        addVisit("NewExpression", this::verifyExpression);
        addVisit("VarDeclaration", this::verifyExpression);
        addVisit("IntArray", this::verifyExpression);
        addVisit("Identifier", this::verifyIdentifier);
        //addVisit("IfStatement", this::verifyIfStatement);
        //addVisit("WhileStatement", this::verifyIfStatement);
        addVisit("Negate", this::verifyExpression);
        addVisit("MethodReturn", this::verifyExpression);

        //addVisit("Statement1", this::verifyStatement1);
        //addVisit("Statement2", this::verifyStatement2);

        addVisit("Assignment", this::verifyAssignment);
        addVisit("MethodDeclaration", this::verifyMethodDeclaration);
        addVisit("VarDeclaration", this::verifyVarDeclaration);

        //Default
        setDefaultVisit(this::defaultVisit);
    }

    public void makeChanges() {
        for (Runnable change : toRemove) {
            change.run();
        }

        toRemove.clear();
    }

    private List<Report> verifyExpression(JmmNode node, boolean mark_as_used) {
        return defaultVisit(node, true);
    }

    private List<Report> verifyIdentifier(JmmNode node, boolean mark_as_used) {
        if (mark_as_used) {
            String varName = node.get("name");
            unusedVariables.remove(varName);
        }
        return new ArrayList<>();
    }

    private List<Report> verifyAssignment(JmmNode node, boolean mark_as_used) {
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);
        visit(firstChild, false);
        visit(secondChild, true);

        String varName = firstChild.get("name");

        if (unusedVariables.containsKey(varName)) {
            unusedVariables.get(varName).add(node);
        }

        return new ArrayList<>();
    }

    private List<Report> verifyVarDeclaration(JmmNode node, boolean mark_as_used) {
        final String varName = node.get("name");
        List<JmmNode> nodes = new ArrayList<>();
        nodes.add(node);

        unusedVariables.put(varName, nodes);

        return new ArrayList<>();
    }

    private List<Report> verifyMethodDeclaration(JmmNode node, boolean mark_as_used) {
        unusedVariables.clear();
        List<JmmNode> parameterNodes = getChildrenOfKind(node, "Argument");
        List<String> parameter_names = new ArrayList<>();
        for (JmmNode parameterNode : parameterNodes) {
            parameter_names.add(parameterNode.get("name"));
        }

        for (String parameter : parameter_names) {
            unusedVariables.put(parameter, new ArrayList<>());
        }

        for(JmmNode child : node.getChildren()){
            visit(child, false);
        }

        for (Map.Entry<String, List<JmmNode>> entry : unusedVariables.entrySet()) {
            for (JmmNode nodeToRemove : entry.getValue()) {
                toRemove.add(() -> removeUnused(nodeToRemove));
            }
        }

        return new ArrayList<>();
    }

    private String deriveFunctionType(JmmNode node) {
        String type = "";
        if (node.getKind().equals("FCall")) {
            JmmNode functionParent = node.getParent();
            String parentKind = functionParent.getKind();
            if (parentKind.equals("Assignment")) {
                type = functionParent.getChildren().get(0).get("type");
            } else if (parentKind.equals("LessThan")) {
                type = "int";
            } else {
                type = functionParent.get("type");
            }
        }
        return type;
    }

    private void removeUnused(JmmNode nodeToRemove) {
        if (nodeToRemove.getKind().equals("Assignment")) {
            JmmNode expression = nodeToRemove.getChildren().get(1);
            List<JmmNode> calls = NodeFindingMethods.getCalls(expression);
            int index = nodeToRemove.getParent().getChildren().indexOf(nodeToRemove);
            for (int i = 0; i < calls.size(); i++) {
                JmmNode call = calls.get(i);
                String type = deriveFunctionType(call);
                call.put("type", type);
                nodeToRemove.getParent().add(call, index + i);
            }

            nodeToRemove.getParent().removeChild(nodeToRemove);

        } else {
            nodeToRemove.getParent().removeChild(nodeToRemove);

        }
        report_list.add(Utils.newSemanticReport(nodeToRemove, ReportType.WARNING, "Unused variable"));
        isChanged = true;
    }

    private List<Report> defaultVisit(JmmNode node, boolean mark_as_used) {
        for(JmmNode child : node.getChildren()){
            visit(child, mark_as_used);
        }
        return new ArrayList<>();
    }
}
