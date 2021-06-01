package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import table.MySymbolTable;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConstantFoldingVisitor  extends PostorderJmmVisitor<Boolean, List<Report>> {
    public boolean isChanged = false;
    public List<Report> report_list;

    private List<Runnable> toChange = new ArrayList<>();

    public ConstantFoldingVisitor(MySymbolTable symbolTable, List<Report> report_list) {
        this.report_list = report_list;

        addVisit("Add", this::verifySum);
        addVisit("Sub", this::verifySub);
        addVisit("Mult", this::verifyMult);
        addVisit("Div", this::verifyDiv);
        addVisit("LessThan", this::verifyLessThan);
        addVisit("And", this::verifyAnd);
        addVisit("Parentheses", this::verifyParentheses);
    }

    public void makeChanges() {
        for (Runnable change : toChange) {
            change.run();
        }

        toChange.clear();
    }

    private void replaceNode(JmmNode from, JmmNode to) {
        JmmNode parent = from.getParent();
        int index = parent.getChildren().indexOf(from);
        parent.removeChild(index);
        parent.add(to, index);
    }

    private List<Report> verifyParentheses(JmmNode node, Boolean aBoolean) {
        if(node.getChildren().size() != 1){
            return new ArrayList<>();
        }
        if(node.getChildren().get(0).getKind().equals("Integer")){
            JmmNode new_node = new JmmNodeImpl("Integer");
            new_node.put("value", node.getChildren().get(0).get("value"));

            toChange.add(() -> replaceNode(node, new_node));
        }

        else if(node.getChildren().get(0).getKind().equals("True") || node.getChildren().get(0).getKind().equals("False")){
            JmmNode new_node = new JmmNodeImpl(node.getChildren().get(0).getKind());
            new_node.put("value", node.getChildren().get(0).get("value"));
            toChange.add(() -> replaceNode(node, new_node));
        }
        else{
            return new ArrayList<>();
        }
        isChanged = true;
        return new ArrayList<>();
    }

    private List<Report> verifyLessThan(JmmNode node, Boolean aBoolean) {
        if(node.getChildren().get(0).getKind().equals("Integer") && node.getChildren().get(1).getKind().equals("Integer")){
            String result = String.valueOf(Integer.parseInt(node.getChildren().get(0).get("value")) < Integer.parseInt(node.getChildren().get(1).get("value")));
            result = result.toLowerCase();
            String kind = result.substring(0, 1).toUpperCase() + result.substring(1);
            JmmNode new_node = new JmmNodeImpl(kind);
            new_node.put("value", result);
            toChange.add(() -> replaceNode(node, new_node));

            isChanged = true;
        }
        return new ArrayList<>();

    }

    private boolean nodeIsBooleanConstant(JmmNode node){
        return (node.getKind().equals("True") || node.getKind().equals("False"));
    }

    private List<Report> verifyAnd(JmmNode node, Boolean aBoolean) {
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);
        if(nodeIsBooleanConstant(firstChild) && nodeIsBooleanConstant(secondChild)){

            String result;
            if(firstChild.getKind().equals("True") && secondChild.getKind().equals("True")){
                result = "True";
            }
            else{
                result = "False";
            }

            JmmNode new_node = new JmmNodeImpl(result);
            new_node.put("value", result);
            toChange.add(() -> replaceNode(node, new_node));
            isChanged = true;
        }
        return new ArrayList<>();
    }

    public List<Report> verifySum(JmmNode node, Boolean aBoolean) {
        if(node.getChildren().get(0).getKind().equals("Integer") && node.getChildren().get(1).getKind().equals("Integer")){
            String result = String.valueOf(Integer.parseInt(node.getChildren().get(0).get("value")) + Integer.parseInt(node.getChildren().get(1).get("value")));

            JmmNode new_node = new JmmNodeImpl("Integer");
            new_node.put("value", result);
            toChange.add(() -> replaceNode(node, new_node));

            isChanged = true;
        }
        return new ArrayList<>();
    }

    private List<Report> verifySub(JmmNode node, Boolean aBoolean) {
        if(node.getChildren().get(0).getKind().equals("Integer") && node.getChildren().get(1).getKind().equals("Integer")){
            String result = String.valueOf(Integer.parseInt(node.getChildren().get(0).get("value")) - Integer.parseInt(node.getChildren().get(1).get("value")));
            JmmNode new_node = new JmmNodeImpl("Integer");
            new_node.put("value", result);
            toChange.add(() -> replaceNode(node, new_node));
            isChanged = true;
        }
        return new ArrayList<>();
    }

    private List<Report> verifyMult(JmmNode node, Boolean aBoolean) {
        if(node.getChildren().get(0).getKind().equals("Integer") && node.getChildren().get(1).getKind().equals("Integer")){
            String result = String.valueOf(Integer.parseInt(node.getChildren().get(0).get("value")) * Integer.parseInt(node.getChildren().get(1).get("value")));
            JmmNode new_node = new JmmNodeImpl("Integer");
            new_node.put("value", result);
            toChange.add(() -> replaceNode(node, new_node));
            isChanged = true;
        }
        return new ArrayList<>();
    }

    private List<Report> verifyDiv(JmmNode node, Boolean aBoolean) {
        if (node.getChildren().get(0).getKind().equals("Integer") && node.getChildren().get(1).getKind().equals("Integer")) {
            try {
                String result = String.valueOf(Integer.parseInt(node.getChildren().get(0).get("value")) / Integer.parseInt(node.getChildren().get(1).get("value")));
                JmmNode new_node = new JmmNodeImpl("Integer");
                new_node.put("value", result);
                toChange.add(() -> replaceNode(node, new_node));
                isChanged = true;
            }
            catch(ArithmeticException e){
                report_list.add(Utils.newSemanticReport(node, ReportType.WARNING, "division by zero"));
            }

        }
        return new ArrayList<>();
    }
}
