package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.Method;
import table.MySymbolTable;
import table.ValueSymbol;
import utils.NodeFindingMethods;

import java.util.List;

import static utils.Utils.newSemanticReport;

public class BinaryOperatorVisitor extends PostorderJmmVisitor<List<Report>, Boolean> {
    MySymbolTable symbolTable;

    public BinaryOperatorVisitor(MySymbolTable symbolTable) {
        addVisit("Add", this::verifySumSub);
        addVisit("Sub", this::verifySumSub);
        addVisit("Mult", this::verifySumSub);
        addVisit("Div", this::verifySumSub);
        addVisit("LessThan", this::verifySumSub);
        addVisit("And", this::verifyAnd);
        addVisit("Parentheses", this::verifyParentheses);
        //addVisit("Length", this::verifyLength);

        this.symbolTable = symbolTable;
    }

    public boolean verifyAnd(JmmNode node, List<Report> reports) {

        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        if (!variablesDeclared(firstChild, secondChild, reports))
            return false;

        if (!firstChild.get("type").equals("boolean") && firstChild.get("is_array").compareTo("false") != 0) {
            reports.add(newSemanticReport(node, "Second value isn't an boolean"));

            return false;
        }

        if (!secondChild.get("type").equals("boolean") && secondChild.get("is_array").compareTo("false") != 0) {
            reports.add(newSemanticReport(node, "First value isn't an boolean"));

            return false;
        }

        node.put("type", "boolean");
        node.put("is_array", "false");

        return true;
    }

    public boolean verifySumSub(JmmNode node, List<Report> reports) {
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        if (!variablesDeclared(firstChild, secondChild, reports))
            return false;

        else {
            if (firstChild.get("type").compareTo("int") != 0 && firstChild.get("is_array").compareTo("false") != 0) {
                reports.add(newSemanticReport(node, "Second value isn't an integer"));

                return false;
            }

            if (secondChild.get("type").compareTo("int") != 0 && secondChild.get("is_array").compareTo("false") != 0) {
                reports.add(newSemanticReport(node, "First value isn't an integer"));

                return false;
            }

            node.put("type", "int");
            node.put("is_array", "false");
        }
        return true;
    }


    public boolean variablesDeclared(JmmNode firstChild, JmmNode secondChild, List<Report> reports) {
        if (firstChild.getOptional("type").isEmpty()) {
            if (firstChild.getKind().compareTo("Identifier") == 0) {
                //Check if identifier is declared
                Method method = NodeFindingMethods.FindParentMethod(firstChild);
                method = symbolTable.getMethod(method);
                Symbol symbol = NodeFindingMethods.getVariable(method, symbolTable, firstChild.get("name"));

                if (symbol == null) {
                    reports.add(newSemanticReport(firstChild, "First variable hasnt been declared"));

                    return false;
                }

                if (!((ValueSymbol) symbol).hasValue()) {
                    reports.add(newSemanticReport(firstChild, "First variable hasnt been given a value"));

                    return false;
                }

            }

            //TODO: Distinguish between undeclared and uninitialized variable
            //System.out.println("1st node Doesnt have a value.");
        }
        else if (secondChild.getOptional("type").isEmpty()) {
            if (secondChild.getKind().compareTo("Identifier") == 0) {
                //Check if identifier is declared
                Method method = NodeFindingMethods.FindParentMethod(secondChild);
                method = symbolTable.getMethod(method);
                Symbol symbol = NodeFindingMethods.getVariable(method, symbolTable, secondChild.get("name"));

                if (symbol == null) {
                    reports.add(newSemanticReport(secondChild, "Second variable hasnt been declared"));

                    return false;
                }

                if (!((ValueSymbol) symbol).hasValue()) {
                    reports.add(newSemanticReport(secondChild, "Second variable hasnt been given a value"));

                    return false;
                }

            }
            //TODO: Distinguish between undeclared and uninitialized variable
            //System.out.println("2nd node Doesnt have a value.");
        }

        return true;
    }

    public boolean verifyParentheses(JmmNode node, List<Report> reports) {
        JmmNode child = node.getChildren().get(0);

        node.put("type", child.get("type"));
        node.put("is_array", child.get("is_array"));

        return true;
    }

    public boolean verifyLength(JmmNode node, List<Report> reports) {
        JmmNode child = node.getChildren().get(0);

        node.put("type", child.get("type"));
        node.put("is_array", child.get("is_array"));

        return true;
    }
}