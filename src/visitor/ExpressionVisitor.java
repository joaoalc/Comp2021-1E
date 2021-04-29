package visitor;

import com.sun.jdi.Value;
import org.w3c.dom.Node;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.Method;
import table.MySymbolTable;
import table.ValueSymbol;
import utils.NodeFindingMethods;

import java.util.ArrayList;
import java.util.List;

import static utils.Utils.newSemanticReport;

public class ExpressionVisitor extends PostorderJmmVisitor<List<Report>, Boolean> {
    MySymbolTable symbolTable;

    public ExpressionVisitor(MySymbolTable symbolTable) {
        addVisit("Add", this::verifySumSub);
        addVisit("Sub", this::verifySumSub);
        addVisit("Mult", this::verifySumSub);
        addVisit("Div", this::verifySumSub);
        addVisit("LessThan", this::verifyLessThan);
        addVisit("And", this::verifyAnd);
        addVisit("Parentheses", this::verifyParentheses);
        addVisit("Index", this::verifyIndex);
        addVisit("FCall", this::verifyCall);
        addVisit("Assignment", this::varAssignment);
        addVisit("NewExpression", this::verifyParentheses);
        addVisit("VarDeclaration", this::varDeclaration);
        addVisit("IntArray", this::verifyArray);
        addVisit("Identifier", this::addValueToNodeOptional);
        addVisit("IfStatement", this::verifyIfStatement);
        addVisit("WhileStatement", this::verifyIfStatement);
        addVisit("Negate", this::verifyNegate);

        this.symbolTable = symbolTable;
    }

    public boolean verifyAnd(JmmNode node, List<Report> reports) {
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        if (variablesNotDeclared(firstChild, secondChild, reports))
            return false;

        else {
            Method method;
            if(firstChild.getOptional("name").isPresent()){
                method = NodeFindingMethods.FindParentMethod(firstChild, symbolTable);
                Symbol var = NodeFindingMethods.getVariable(method, symbolTable, firstChild.get("name"));
                firstChild.put("type", var.getType().getName());
                firstChild.put("is_array", String.valueOf(var.getType().isArray()));
            }
            if(secondChild.getOptional("name").isPresent()){
                method = NodeFindingMethods.FindParentMethod(secondChild, symbolTable);
                Symbol var = NodeFindingMethods.getVariable(method, symbolTable, secondChild.get("name"));
                secondChild.put("type", var.getType().getName());
                secondChild.put("is_array", String.valueOf(var.getType().isArray()));
            }


            if ( (!NodeFindingMethods.sameType(firstChild.get("type"), "boolean")) || (!NodeFindingMethods.sameType(firstChild.get("is_array"), "false"))) {
                reports.add(newSemanticReport(node, "Second value isn't an integer"));

                return false;
            }

            if ((!NodeFindingMethods.sameType(secondChild.get("type"), "boolean")) || (!NodeFindingMethods.sameType(secondChild.get("is_array"), "false"))) {
                reports.add(newSemanticReport(node, "First value isn't an integer"));

                return false;
            }

            node.put("type", "boolean");
            node.put("is_array", "false");
        }
        return true;
    }

    public boolean verifyLessThan(JmmNode node, List<Report> reports) {
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        if (variablesNotDeclared(firstChild, secondChild, reports))
            return false;

        else {
            Method method;
            if(firstChild.getOptional("name").isPresent()){
                method = NodeFindingMethods.FindParentMethod(firstChild, symbolTable);
                Symbol var = NodeFindingMethods.getVariable(method, symbolTable, firstChild.get("name"));
                firstChild.put("type", var.getType().getName());
                firstChild.put("is_array", String.valueOf(var.getType().isArray()));
            }
            if(secondChild.getOptional("name").isPresent()){
                method = NodeFindingMethods.FindParentMethod(secondChild, symbolTable);
                Symbol var = NodeFindingMethods.getVariable(method, symbolTable, secondChild.get("name"));
                secondChild.put("type", var.getType().getName());
                secondChild.put("is_array", String.valueOf(var.getType().isArray()));
            }


            if ((!NodeFindingMethods.sameType(firstChild.get("type"), "int")) || (!NodeFindingMethods.sameType(firstChild.get("is_array"), "false"))) {
                reports.add(newSemanticReport(node, "Second value isn't an integer"));

                return false;
            }

            if ( (!NodeFindingMethods.sameType(secondChild.get("type"), "int")) || (!NodeFindingMethods.sameType(secondChild.get("is_array"), "false"))) {
                reports.add(newSemanticReport(node, "First value isn't an integer"));

                return false;
            }

            node.put("type", "boolean");
            node.put("is_array", "false");
        }
        return true;
    }

    public boolean verifySumSub(JmmNode node, List<Report> reports) {
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        if (variablesNotDeclared(firstChild, secondChild, reports))
            return false;

        else {
            Method method;
            if(firstChild.getOptional("name").isPresent()){
                method = NodeFindingMethods.FindParentMethod(firstChild, symbolTable);
                Symbol var = NodeFindingMethods.getVariable(method, symbolTable, firstChild.get("name"));
                firstChild.put("type", var.getType().getName());
                firstChild.put("is_array", String.valueOf(var.getType().isArray()));
            }
            if(secondChild.getOptional("name").isPresent()){
                method = NodeFindingMethods.FindParentMethod(secondChild, symbolTable);
                Symbol var = NodeFindingMethods.getVariable(method, symbolTable, secondChild.get("name"));
                secondChild.put("type", var.getType().getName());
                secondChild.put("is_array", String.valueOf(var.getType().isArray()));
            }


            if ((!NodeFindingMethods.sameType(firstChild.get("type"), "int")) || (!NodeFindingMethods.sameType(firstChild.get("is_array"), "false"))) {
                reports.add(newSemanticReport(node, "Second value isn't an integer"));

                return false;
            }

            if ( (!NodeFindingMethods.sameType(secondChild.get("type"), "int")) || (!NodeFindingMethods.sameType(secondChild.get("is_array"), "false"))) {
                reports.add(newSemanticReport(node, "First value isn't an integer"));

                return false;
            }

            node.put("type", "int");
            node.put("is_array", "false");
        }
        return true;
    }

    public boolean variablesNotDeclared(JmmNode firstChild, JmmNode secondChild, List<Report> reports) {
        if (firstChild.getOptional("type").isEmpty()) {
            if (NodeFindingMethods.sameType(firstChild.getKind(), "Identifier")) {
                //Check if identifier is declared
                Method method = NodeFindingMethods.FindParentMethod(firstChild);
                method = symbolTable.getMethod(method);
                Symbol symbol = NodeFindingMethods.getVariable(method, symbolTable, firstChild.get("name"));

                if (symbol == null) {
                    reports.add(newSemanticReport(firstChild, "First variable hasn't been declared"));

                    return true;
                }

                if (!((ValueSymbol) symbol).hasValue()) {
                    reports.add(newSemanticReport(firstChild, "First variable hasn't been given a value"));

                    return true;
                }
            }

            //TODO: Distinguish between undeclared and uninitialized variable
            //System.out.println("1st node Doesnt have a value.");
        }

        else if (secondChild.getOptional("type").isEmpty()) {
            if (NodeFindingMethods.sameType(secondChild.getKind(), "Identifier")) {
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

        return false;
    }

    public boolean variablesNotDeclared(JmmNode child, List<Report> reports) {
        if (child.getOptional("type").isEmpty()) {
            if (NodeFindingMethods.sameType(child.getKind(), "Identifier")) {
                //Check if identifier is declared
                Method method = NodeFindingMethods.FindParentMethod(child);
                method = symbolTable.getMethod(method);
                Symbol symbol = NodeFindingMethods.getVariable(method, symbolTable, child.get("name"));

                if (symbol == null) {
                    reports.add(newSemanticReport(child, "First variable hasn't been declared"));

                    return true;
                }

                if (!((ValueSymbol) symbol).hasValue()) {
                    reports.add(newSemanticReport(child, "First variable hasn't been given a value"));

                    return true;
                }
            }
        }
        return false;
    }

    public boolean verifyParentheses(JmmNode node, List<Report> reports) {
        JmmNode child = node.getChildren().get(0);

        node.put("type", child.get("type"));
        node.put("is_array", child.get("is_array"));

        return true;
    }

    public boolean verifyIndex(JmmNode node, List<Report> reports) {
        JmmNode child = node.getChildren().get(0);

        if(!(NodeFindingMethods.sameType(child.get("type"), "int"))){
            //TODO: Semantic error, index isn't int
            System.out.println("Index isn't int");
            return false;
        }

        node.put("type", child.get("type"));
        node.put("is_array", child.get("is_array"));

        return true;
    }

    public boolean verifyCall(JmmNode node, List<Report> reports){
        if (node.getChildren().size() == 2){

            //Check if it's a length call, if it is, the thing calling it has to be an array
            if (node.getChildren().get(1).getKind().equals("Length")){
                if (node.getChildren().get(0).getOptional("name").isPresent()) {
                    Method method = NodeFindingMethods.FindParentMethod(node, symbolTable);

                    //If it has a method, find in method and then symbol table
                    if(method != null){
                        if(method.getLocalVariable(node.getChildren().get(0).get("name")) == null) {
                            if(symbolTable.getField(node.getChildren().get(0).get("name")) == null) {
                                reports.add(newSemanticReport(node, "Undeclared variable"));

                                return false;
                            }
                        }
                    }
                    //If it doesn't have a method, find in symbol table only
                    else if (symbolTable.getField(node.getChildren().get(0).get("name")) == null){
                        reports.add(newSemanticReport(node, "Undeclared variable"));

                        return false;
                    }

                    node.put("type", "int");
                    node.put("is_array", "false");
                    return true;
                }
                else if((NodeFindingMethods.sameType(node.getChildren().get(0).get("is_array"), "true"))) {
                    node.put("type", "int");
                    node.put("is_array", "false");
                    return true;
                }

                else{
                    reports.add(newSemanticReport(node, "Variable isn't an array"));

                    return false;
                }
            }

            boolean ownFunction = node.getChildren().get(0).getKind().equals("This");

            //this.etc()
            if(!ownFunction){

                String name = node.getChildren().get(0).get("name");
                //If it's an import
                if(symbolTable.importExists(name)){
                    node.put("type", "");
                    node.put("is_array", "");
                    return true;
                }

                Method method = NodeFindingMethods.FindParentMethod(node, symbolTable);
                ValueSymbol symbol;
                if(method != null) {
                    symbol = (ValueSymbol) NodeFindingMethods.getVariable(method, symbolTable, name);
                }
                else{
                    symbol = (ValueSymbol) NodeFindingMethods.getVariable(symbolTable, name);
                }
                if(symbol != null){
                    if(!symbol.hasValue()){

                        //TODO: Uninitialized variable error
                        System.out.println("error: uninitialized variable calling method");
                        return true;
                    }
                    if(symbol.getType().getName().equals(symbolTable.getClassName()) && symbolTable.getSuper() == null){
                        //See if method exists
                        ownFunction = true;
                    }
                    else if(symbol.getType().getName().equals(symbolTable.getClassName()) && symbolTable.getSuper() != null){
                        node.put("type", "");
                        node.put("is_array", "");
                        return true;
                    }
                }
                else{
                    //TODO: Undeclared variable error
                    System.out.println("error: uninitialized variable calling method");
                    return false;
                }
                if(!ownFunction) {
                    node.put("type", symbol.getType().getName());
                    node.put("is_array", String.valueOf(symbol.getType().isArray()));
                    return true;
                }


            }
            //DO NOT DO ELSE IF, THIS VALUE IS CHANGED IN THE FIRST IF
            if(ownFunction) {
                //Get arguments
                //Is the method in the table?

                List<ValueSymbol> arguments = new ArrayList<>();

                //Get function arguments;
                for (int i = 0; i < node.getChildren().get(1).getChildren().size(); i++){
                    //TODO: See which function we are in so we can get that method's local variables

                    arguments.add(new ValueSymbol(new Type(node.getChildren().get(1).getChildren().get(i).get("type"), Boolean.parseBoolean(node.getChildren().get(1).getChildren().get(i).get("is_array"))), "-", false));

                }
                if (!symbolTable.methodExists(node.getChildren().get(1).get("name"), arguments)) {
                    if(symbolTable.getSuper() == null){
                        //TODO: Add report
                        System.out.println("Undeclared method, add report here and stop execution");
                        return false;
                    }
                    //The method could be from the superclass if none is found with the same name and arguments
                    else{
                        node.put("type", "");
                        node.put("is_array", "");
                    }
                }
                //if method exists with same arguments
                else{
                    Method method = symbolTable.getMethod(node.getChildren().get(1).get("name"), arguments);
                    node.put("type", method.getReturnType().getName());
                    node.put("is_array", String.valueOf(method.getReturnType().isArray()));
                    return true;
                }
            }
            // <import_name>.etc()
            //  or
            // varName.etc()


            return true;
        }
        else{
        }
        return true;
    }

    public boolean varAssignment(JmmNode node, List<Report> reports){
        //Without index
        if(node.getChildren().size() == 2){
            if(node.getChildren().get(0).getOptional("name").isEmpty()){
                //TODO: Index on non variable error
                System.out.println("Index on non variable error. Line " + node.getChildren().get(0).get("line"));
                return false;
            }

            Method method = NodeFindingMethods.FindParentMethod(node, symbolTable);
            ValueSymbol var_symbol = (ValueSymbol) NodeFindingMethods.getVariable(method, symbolTable, node.getChildren().get(0).get("name"));

            if(var_symbol == null){
                //TODO: Undeclared variable error
                System.out.println("Undeclared variable.");
                return false;
            }
            else if(var_symbol.getType().isArray() != Boolean.parseBoolean(node.getChildren().get(1).get("is_array"))){
                //TODO: Index on non array variable error
                System.out.println("Index on non array variable error on line " + node.getChildren().get(1).get("line"));
                return false;
            }
            else if(!NodeFindingMethods.sameType(node.getChildren().get(1).get("type"), var_symbol.getType().getName())){
                //TODO: Type mismatch error
                System.out.println("Type mismatch error. Line " + node.getChildren().get(1).get("line"));
                return false;
            }
            var_symbol.setHas_value(true);
            return true;
        }
        //With index
        else if(node.getChildren().size() == 3){
            if(node.getChildren().get(0).getOptional("name").isEmpty()){
                //TODO: Index on non variable error
                System.out.println("Index on non variable error");
                return false;
            }

            Method method = NodeFindingMethods.FindParentMethod(node, symbolTable);
            ValueSymbol var_symbol = NodeFindingMethods.getVariable(method, symbolTable, node.getChildren().get(0).get("name"));

            if(var_symbol == null){
                //TODO: Undeclared variable error
                System.out.println("Undeclared variable.");
                return false;
            }
            //Note: This only works cause we do not have multidimensional arrays
            else if((!var_symbol.getType().isArray()) || node.getChildren().get(2).get("is_array").equals("true")){
                //TODO: Index on non array variable error
                System.out.println("Index on non array variable error line " + node.getChildren().get(2).get("line"));
                return false;
            }
            else if(!NodeFindingMethods.sameType(node.getChildren().get(2).get("type"), var_symbol.getType().getName())){
                //TODO: Type mismatch error
                System.out.println("Type mismatch error.");
                return false;
            }
            if(!var_symbol.hasValue()){
                //TODO: Uninitialized array error
                System.out.println("Uninitalized array. Line " + node.getChildren().get(0).get("line") + " col " + node.getChildren().get(0).get("col"));
                return false;
            }
            var_symbol.setHas_value(true);
            return true;
        }
        return true;
    }

    public boolean varDeclaration(JmmNode node, List<Report> reports){
        String varName = node.get("name");

        Type varType = new Type(node.getChildren().get(0).get("name"), Boolean.parseBoolean(node.getChildren().get(0).get("is_array")));
        ValueSymbol symbol = new ValueSymbol(varType, varName);

        Method method = NodeFindingMethods.FindParentMethod(node, symbolTable);

        //Add to class' (global) symbol table
        if(method == null){
            System.out.println(symbol.getName());
            if(!symbolTable.fieldExists(symbol)) {
                symbolTable.addField(varType, varName, false);
                return true;
            }
            else{
                //TODO: Create report: Repeat variable in class
                System.out.println("Redeclaration of variable " + symbol);
            }
        }
        else{
            Method table_method = symbolTable.getMethod(method);
            if(table_method == null){
                System.out.println("Error: Unreported class");
                return false;
            }
            if(table_method.localVariableExists(symbol)){
                //TODO: Create report: Repeat variable in method
                System.out.println("This variable " + symbol + " does not exist");
                return false;
            }
            table_method.addLocalVariable(varType, varName, false);
            System.out.println("added variable");
        }

        return true;
    }

    public boolean verifyArray(JmmNode node, List<Report> reports){
        JmmNode child = node.getChildren().get(0);

        node.put("type", child.get("type"));
        node.put("is_array", "true");

        return true;
    }

    public boolean addValueToNodeOptional(JmmNode node, List<Report> reports){
        Method method = NodeFindingMethods.FindParentMethod(node, symbolTable);
        ValueSymbol symbol = (ValueSymbol) NodeFindingMethods.getVariable(method, symbolTable, node.get("name"));

        if(symbol == null){
            //In the case of variable declarations where the value isn't in the symbol table yet
            return true;
        }
        node.put("type", symbol.getType().getName());
        node.put("is_array", String.valueOf(symbol.getType().isArray()));
        node.put("declared", String.valueOf(symbol.hasValue()));
        return true;
    }

    public boolean verifyIfStatement(JmmNode node, List<Report> reports){
        if((! NodeFindingMethods.sameType(node.getChildren().get(0).get("type"), "boolean")) || NodeFindingMethods.sameType(node.getChildren().get(0).get("is_array"), "true")){
            //TODO: Wrong variable type in if statement error
            System.out.println("Wrong type inside of if.");
            return false;
        }
        //node.put("type", "boolean");
        //node.put("is_array", "true");
        return true;
    }

    public boolean verifyNegate(JmmNode node, List<Report> reports) {

        if(NodeFindingMethods.sameType(node.getChildren().get(0).get("type"), "boolean"))
            if(NodeFindingMethods.sameType(node.getChildren().get(0).get("is_array"), "false")){
                if(node.getChildren().get(0).getKind().equals("Identifier")){
                    if(!variablesNotDeclared(node.getChildren().get(0), reports)){
                        node.put("type", "boolean");
                        node.put("is_array", "false");
                    }
                }
                //Any type gets converted to boolean aswell
                node.put("type", "boolean");
                node.put("is_array", "false");
            }
        return true;
    }
}
