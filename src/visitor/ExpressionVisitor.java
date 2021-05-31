package visitor;

import com.sun.jdi.Value;
import org.w3c.dom.Node;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import table.Method;
import table.MySymbolTable;
import table.ValueSymbol;
import utils.NodeFindingMethods;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static utils.Utils.newSemanticReport;

public class ExpressionVisitor extends PostorderJmmVisitor<Boolean, List<Report>> {
    MySymbolTable symbolTable;

    public List<Report> report_list;

    private List<Symbol> undeclaredVariables = new ArrayList<>();

    public ExpressionVisitor(MySymbolTable symbolTable, List<Report> report_list) {
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

        addVisit("Statement1", this::verifyStatement1);
        addVisit("Statement2", this::verifyStatement2);
        setDefaultVisit(this::defaultVisit);

        this.symbolTable = symbolTable;
        this.report_list = report_list;
    }

    private List<Report> verifyStatement1(JmmNode node, Boolean aBoolean) {
        if(node.getChildren().size() > 0) {
            JmmNode childNode = node.getChildren().get(0);
            if (childNode.getOptional("type").isPresent()) {
                node.put("type", childNode.get("type"));
            }
            if (childNode.getOptional("is_array").isPresent()) {
                node.put("is_array", childNode.get("is_array"));
            }
            if (childNode.getOptional("value").isPresent()) {
                node.put("value", childNode.get("value"));
            }
            if (childNode.getOptional("name").isPresent()) {
                node.put("name", childNode.get("name"));
            }
        }
        return new ArrayList<Report>();
    }

    private List<Report> verifyStatement2(JmmNode node, Boolean aBoolean) {
        if(node.getChildren().size() > 0) {
            JmmNode childNode = node.getChildren().get(0);
            if (childNode.getOptional("type").isPresent()) {
                node.put("type", childNode.get("type"));
            }
            if (childNode.getOptional("is_array").isPresent()) {
                node.put("is_array", childNode.get("is_array"));
            }
            if (childNode.getOptional("value").isPresent()) {
                node.put("value", childNode.get("value"));
            }
            if (childNode.getOptional("name").isPresent()) {
                node.put("name", childNode.get("name"));
            }
        }
        return new ArrayList<Report>();
    }

    public List<Report> verifyAnd(JmmNode node, Boolean aBoolean) {
        node.put("type", "boolean");
        node.put("is_array", "false");

        List<Report> reports = new ArrayList<>();

        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        if (variablesNotDeclared(firstChild, secondChild, reports)) {
            return reports;
        }

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
                reports.add(newSemanticReport(node, ReportType.ERROR, "TypeMismatch: && first operand must be boolean, is " + firstChild.get("type") + (firstChild.get("is_array").equals("true") ? "[]" : "")));
                report_list.add(newSemanticReport(node, ReportType.ERROR, "TypeMismatch: && first operand must be boolean, is " + firstChild.get("type") + (firstChild.get("is_array").equals("true") ? "[]" : "")));
                return reports;
            }

            if ((!NodeFindingMethods.sameType(secondChild.get("type"), "boolean")) || (!NodeFindingMethods.sameType(secondChild.get("is_array"), "false"))) {
                reports.add(newSemanticReport(node, ReportType.ERROR, "TypeMismatch: && second operand must be boolean, is " + secondChild.get("type") + (secondChild.get("is_array").equals("true") ? "[]" : "")));
                report_list.add(newSemanticReport(node, ReportType.ERROR, "TypeMismatch: && second operand must be boolean, is " + secondChild.get("type") + (secondChild.get("is_array").equals("true") ? "[]" : "")));
                return reports;
            }
        }
        return reports;
    }

    public List<Report> verifyLessThan(JmmNode node, Boolean aBoolean) {
        node.put("type", "boolean");
        node.put("is_array", "false");
        List<Report> reports = new ArrayList<>();

        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);

        if (variablesNotDeclared(firstChild, secondChild, reports))
            return reports;

        else {
            if ((!NodeFindingMethods.sameType(firstChild.get("type"), "int")) || (!NodeFindingMethods.sameType(firstChild.get("is_array"), "false"))) {
                reports.add(newSemanticReport(node, ReportType.ERROR, "TypeMismatch: < first operand must be int, is " + firstChild.get("type") + (firstChild.get("is_array").equals("true") ? "[]" : "")));
                report_list.add(newSemanticReport(node, ReportType.ERROR, "TypeMismatch: < first operand must be int, is " + firstChild.get("type") + (firstChild.get("is_array").equals("true") ? "[]" : "")));
                return reports;
            }

            if ((!NodeFindingMethods.sameType(secondChild.get("type"), "int")) || (!NodeFindingMethods.sameType(secondChild.get("is_array"), "false"))) {
                reports.add(newSemanticReport(node, ReportType.ERROR, "TypeMismatch: < second operand must be int, is " + secondChild.get("type") + (secondChild.get("is_array").equals("true") ? "[]" : "")));
                report_list.add(newSemanticReport(node, ReportType.ERROR, "TypeMismatch: < second operand must be int, is " + secondChild.get("type") + (secondChild.get("is_array").equals("true") ? "[]" : "")));
                return reports;
            }

            node.put("type", "boolean");
            node.put("is_array", "false");
        }
        return reports;
    }

    public List<Report> verifySumSub(JmmNode node, Boolean aBoolean) {
        //We assumed that sums are EXCLUSIVELY between integers and result in another integer
        node.put("type", "int");
        node.put("is_array", "false");
        List<Report> reports = new ArrayList<>();


        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);
        if (variablesNotDeclared(firstChild, secondChild, reports))
            return reports;

        else {
            Method method;
            if(firstChild.getOptional("name").isPresent()){
                method = NodeFindingMethods.FindParentMethod(firstChild, symbolTable);
                Symbol var = NodeFindingMethods.getVariable(method, symbolTable, firstChild.get("name"));
                if(var == null){
                    reports.add(newSemanticReport(node, ReportType.WARNING,"Undeclared variable"));
                    report_list.add(newSemanticReport(node, ReportType.WARNING,"Undeclared variable"));
                    return reports;
                }
                if(!firstChild.getOptional("type").isPresent()) {
                    firstChild.put("type", var.getType().getName());
                }
                if(!firstChild.getOptional("is_array").isPresent()) {
                    firstChild.put("is_array", String.valueOf(var.getType().isArray()));
                }
            }
            if(secondChild.getOptional("name").isPresent()){
                method = NodeFindingMethods.FindParentMethod(secondChild, symbolTable);
                Symbol var = NodeFindingMethods.getVariable(method, symbolTable, secondChild.get("name"));
                if(var == null){
                    reports.add(newSemanticReport(node, ReportType.WARNING,"Undeclared variable"));
                    report_list.add(newSemanticReport(node, ReportType.WARNING,"Undeclared variable"));
                    return reports;
                }
                if(!secondChild.getOptional("type").isPresent())
                    secondChild.put("type", var.getType().getName());
                if(!secondChild.getOptional("is_array").isPresent())
                    secondChild.put("is_array", String.valueOf(var.getType().isArray()));
            }

            if ((!NodeFindingMethods.sameType(firstChild.get("type"), "int")) || (!NodeFindingMethods.sameType(firstChild.get("is_array"), "false"))) {
                reports.add(newSemanticReport(node, ReportType.ERROR, "TypeMismatch: + first operand must be int, is " + firstChild.get("type") + (firstChild.get("is_array").equals("true") ? "[]" : "")));
                report_list.add(newSemanticReport(node, ReportType.ERROR, "TypeMismatch: + first operand must be int, is " + firstChild.get("type") + (firstChild.get("is_array").equals("true") ? "[]" : "")));

                return reports;
            }

            if ( (!NodeFindingMethods.sameType(secondChild.get("type"), "int")) || (!NodeFindingMethods.sameType(secondChild.get("is_array"), "false"))) {
                reports.add(newSemanticReport(node, ReportType.ERROR, "TypeMismatch: + second operand must be int, is " + secondChild.get("type") + (secondChild.get("is_array").equals("true") ? "[]" : "")));
                report_list.add(newSemanticReport(node, ReportType.ERROR, "TypeMismatch: + second operand must be int, is " + secondChild.get("type") + (secondChild.get("is_array").equals("true") ? "[]" : "")));

                return reports;
            }

            node.put("type", "int");
            node.put("is_array", "false");
        }
        return reports;
    }

    public boolean variablesNotDeclared(JmmNode firstChild, JmmNode secondChild, List<Report> reports) {
        if (firstChild.getOptional("type").isEmpty()) {
            if (NodeFindingMethods.sameType(firstChild.getKind(), "Identifier")) {
                //Check if identifier is declared
                Method method = NodeFindingMethods.FindParentMethod(firstChild);
                method = symbolTable.getMethod(method);
                Symbol symbol = NodeFindingMethods.getVariable(method, symbolTable, firstChild.get("name"));

                if (symbol == null) {
                    reports.add(newSemanticReport(firstChild, ReportType.ERROR,"UndeclaredVariable: First variable hasn't been declared"));
                    report_list.add(newSemanticReport(firstChild, ReportType.ERROR,"UndeclaredVariable: First variable hasn't been declared"));

                    return true;
                }

                if (!((ValueSymbol) symbol).hasValue()) {
                    reports.add(newSemanticReport(firstChild, ReportType.ERROR,"UndeclaredVariable: First variable hasn't been given a value"));
                    report_list.add(newSemanticReport(firstChild, ReportType.ERROR,"UndeclaredVariable: First variable hasn't been given a value"));

                    return true;
                }
            }
        }

        else if (secondChild.getOptional("type").isEmpty()) {
            if (NodeFindingMethods.sameType(secondChild.getKind(), "Identifier")) {
                //Check if identifier is declared
                Method method = NodeFindingMethods.FindParentMethod(secondChild);
                method = symbolTable.getMethod(method);
                Symbol symbol = NodeFindingMethods.getVariable(method, symbolTable, secondChild.get("name"));

                if (symbol == null) {
                    reports.add(newSemanticReport(secondChild, ReportType.ERROR,"UndeclaredVariable: Second variable hasnt been declared"));
                    report_list.add(newSemanticReport(secondChild, ReportType.ERROR,"UndeclaredVariable: Second variable hasnt been declared"));

                    return false;
                }

                if (!((ValueSymbol) symbol).hasValue()) {
                    reports.add(newSemanticReport(secondChild, ReportType.ERROR,"UndeclaredVariable: Second variable hasnt been given a value"));
                    report_list.add(newSemanticReport(secondChild, ReportType.ERROR,"UndeclaredVariable: Second variable hasnt been given a value"));

                    return false;
                }

            }
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
                    reports.add(newSemanticReport(child, ReportType.ERROR,"UndeclaredVariable: First variable hasn't been declared"));
                    report_list.add(newSemanticReport(child, ReportType.ERROR,"UndeclaredVariable: First variable hasn't been declared"));

                    return true;
                }

                if (!((ValueSymbol) symbol).hasValue()) {
                    reports.add(newSemanticReport(child, ReportType.ERROR,"UndeclaredVariable: First variable hasn't been given a value"));
                    report_list.add(newSemanticReport(child, ReportType.ERROR,"UndeclaredVariable: First variable hasn't been given a value"));

                    return true;
                }
            }
        }
        return false;
    }

    public List<Report> verifyParentheses(JmmNode node, Boolean aBoolean) {
        node.put("type", "");
        node.put("is_array", "");


        List<Report> reports = new ArrayList<>();
        JmmNode child = node.getChildren().get(0);

        //TODO: Add report
        if(child.getOptional("type").isEmpty()){

            return reports;
        }
        if(child.getOptional("is_array").isEmpty()){
            return reports;
        }

        node.put("type", child.get("type"));
        node.put("is_array", child.get("is_array"));

        return reports;
    }

    public List<Report> verifyIndex(JmmNode node, Boolean aBoolean) {
        //TODO: Check if there are multidimensional arrays
        List<Report> reports = new ArrayList<>();
        node.put("type" , "");
        node.put("is_array", "false");


        JmmNode array = node.getChildren().get(0);
        if(array.getOptional("name").isEmpty()){
            reports.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Array type expected; found: " + NodeFindingMethods.getTypeStringReport(node.getChildren().get(0))));
            report_list.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Array type expected; found: " + NodeFindingMethods.getTypeStringReport(node.getChildren().get(0))));

            System.out.println("Non variable used as array for index");
            return reports;
        }
        if(!(array.get("is_array").equals("true"))){
            reports.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Array type expected; found: " + NodeFindingMethods.getTypeStringReport(node.getChildren().get(0))));
            report_list.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Array type expected; found: " + NodeFindingMethods.getTypeStringReport(node.getChildren().get(0))));
            return reports;
        }
        if(variablesNotDeclared(array, reports)){
            reports.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Cannot resolve symbol " + node.getOptional("name")));
            report_list.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Cannot resolve symbol " + node.getOptional("name")));
            return reports;
        }


        JmmNode index = node.getChildren().get(1);

        if((!NodeFindingMethods.sameType(index.get("type"), "int")) || (!NodeFindingMethods.sameType(index.get("is_array"), "false"))){
            //TODO: Semantic error, index isn't int
            reports.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Cannot resolve symbol " + node.getOptional("name")));
            report_list.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Cannot resolve symbol " + node.getOptional("name")));
            return reports;
        }

        node.put("type", array.get("type"));
        node.put("is_array", "false");
        node.put("name", array.get("name"));
        return reports;
    }

    public List<Report> verifyCall(JmmNode node, Boolean aBoolean){
        node.put("type", "");
        node.put("is_array", "");
        List<Report> reports = new ArrayList<>();


        if (node.getChildren().size() == 2){
            //Check if it's a length call, if it is, the thing calling it has to be an array
            if (node.getChildren().get(1).getKind().equals("Length")){
                node.put("type", "int");
                node.put("is_array", "false");
                if (node.getChildren().get(0).getOptional("name").isPresent()) {
                    Method method = NodeFindingMethods.FindParentMethod(node, symbolTable);

                    //If it has a method, find in method and then symbol table
                    if(method != null){
                        if(method.getLocalVariable(node.getChildren().get(0).get("name")) == null) {
                            if(symbolTable.getField(node.getChildren().get(0).get("name")) == null) {
                                reports.add(newSemanticReport(node, ReportType.WARNING,"UndeclaredVariable: .length operand must be int"));
                                report_list.add(newSemanticReport(node, ReportType.WARNING,"UndeclaredVariable:  .length operand must be int"));

                                return reports;
                            }
                        }
                    }
                    //If it doesn't have a method, find in symbol table only
                    else if (symbolTable.getField(node.getChildren().get(0).get("name")) == null){
                        reports.add(newSemanticReport(node, ReportType.WARNING,"UndeclaredVariable: .length operand must be int\""));
                        report_list.add(newSemanticReport(node, ReportType.WARNING,"UndeclaredVariable: .length operand must be int\""));

                        return reports;
                    }

                    node.put("type", "int");
                    node.put("is_array", "false");

                    return reports;
                }
                else if((NodeFindingMethods.sameType(node.getChildren().get(0).get("is_array"), "true"))) {
                    node.put("type", "int");
                    node.put("is_array", "false");

                    return reports;
                }

                else{
                    reports.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Array type expected;"));
                    report_list.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Array type expected;"));

                    return reports;
                }
            }

            boolean ownFunction = node.getChildren().get(0).getKind().equals("This");

            //this.etc()
            if(!ownFunction){

                Optional<String> opName = node.getChildren().get(0).getOptional("name");
                if(opName.isEmpty()){
                    if(node.getChildren().get(0).get("type") == symbolTable.getClassName() && symbolTable.getSuper() == null){


                        List<ValueSymbol> arguments = new ArrayList<>();

                        //Get function arguments;
                        for (int i = 0; i < node.getChildren().get(1).getChildren().size(); i++){
                            arguments.add(new ValueSymbol(new Type(node.getChildren().get(1).getChildren().get(i).get("type"), Boolean.parseBoolean(node.getChildren().get(1).getChildren().get(i).get("is_array"))), "-", false));

                        }


                        if(node.getChildren().get(1).getOptional("name").isEmpty()){
                            reports.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredMethod: Cannot resolve method"));
                            report_list.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredMethod: Cannot resolve method"));
                        }
                        if (!symbolTable.methodExists(node.getChildren().get(1).get("name"), arguments)) {
                            //TODO: Exception
                            reports.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredMethod: Cannot resolve method " + node.getChildren().get(1).get("name")));
                            report_list.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredMethod: Cannot resolve method " + node.getChildren().get(1).get("name")));

                            System.out.println("Method does not exist. Line " + node.getChildren().get(1).get("line"));
                        }
                        else{
                            Method m = symbolTable.getMethod(node.getChildren().get(1).get("name"), arguments);
                            node.put("type", m.getReturnType().getName());
                            node.put("is_array", String.valueOf(m.getReturnType().isArray()));
                        }
                    }

                    node.put("type", "");
                    node.put("is_array", "");
                    return reports;
                }
                String name = opName.get();

                //If it's an import
                if(symbolTable.importExists(name)){
                    node.put("type", "");
                    node.put("is_array", "");
                    return reports;
                }

                Method method = NodeFindingMethods.FindParentMethod(node, symbolTable);
                ValueSymbol symbol;
                if(method != null) {
                    symbol = NodeFindingMethods.getVariable(method, symbolTable, name);
                }
                else{
                    symbol = NodeFindingMethods.getVariable(symbolTable, name);
                }
                if(symbol != null){
                    if(!symbol.hasValue()){
                        reports.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Cannot resolve symbol " + node.getChildren().get(0).get("name")));
                        report_list.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Cannot resolve symbol " + node.getChildren().get(0).get("name")));

                        System.out.println("error: uninitialized variable calling method");
                        return reports;
                    }
                    if(symbol.getType().getName().equals(symbolTable.getClassName()) && symbolTable.getSuper() == null){
                        //See if method exists
                        ownFunction = true;
                    }
                    else if(symbol.getType().getName().equals(symbolTable.getClassName()) && symbolTable.getSuper() != null){
                        node.put("type", "");
                        node.put("is_array", "");
                        return reports;
                    }
                }
                else{
                    reports.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredMethod: Cannot resolve method " + node.getChildren().get(1).get("name")));
                    report_list.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredMethod: Cannot resolve method " + node.getChildren().get(1).get("name")));

                    System.out.println("error: uninitialized variable calling method");
                    return reports;
                }
                if(!ownFunction) {
                    node.put("type", symbol.getType().getName());
                    node.put("is_array", String.valueOf(symbol.getType().isArray()));
                    return reports;
                }


            }
            //DO NOT DO ELSE IF, THIS VALUE IS CHANGED IN THE FIRST IF
            if(ownFunction) {
                //Get arguments
                //Is the method in the table?

                List<ValueSymbol> arguments = new ArrayList<>();

                //Get function arguments;
                for (int i = 0; i < node.getChildren().get(1).getChildren().size(); i++){
                    arguments.add(new ValueSymbol(new Type(node.getChildren().get(1).getChildren().get(i).get("type"), Boolean.parseBoolean(node.getChildren().get(1).getChildren().get(i).get("is_array"))), "-", false));

                }
                if (!symbolTable.methodExists(node.getChildren().get(1).get("name"), arguments)) {
                    if(symbolTable.getSuper() == null){
                        //TODO: Add report
                        reports.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredMethod: Cannot resolve method " + node.getChildren().get(1).get("name")));
                        report_list.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredMethod: Cannot resolve method " + node.getChildren().get(1).get("name")));
                        System.out.println("Undeclared method, add report here and stop execution");

                        return reports;
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
                    return reports;
                }
            }
            // <import_name>.etc()
            //  or
            // varName.etc()


            return reports;
        }
        else{
        }
        return reports;
    }

    public List<Report> varAssignment(JmmNode node, Boolean aBoolean){
        node.put("type", "");
        node.put("is_array", "");
        List<Report> reports = new ArrayList<>();

        //Without index
        if(node.getChildren().size() == 2){
            if(node.getChildren().get(0).getOptional("name").isEmpty()){
                //TODO: Index on non variable error
                reports.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Array type expected; found: " + node.getChildren().get(0).getOptional("type")));
                report_list.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Array type expected; found: " + node.getChildren().get(0).getOptional("type")));
                return reports;
            }
            //TODO: add report
            if(node.getChildren().get(0).getOptional("type").isEmpty() || node.getChildren().get(0).getOptional("is_array").isEmpty()) {
                //reports.add(newSemanticReport(node, "Cannot resolve symbol " + node.getChildren().get(0).get("name")));
                return reports;
            }

            Method method = NodeFindingMethods.FindParentMethod(node, symbolTable);
            ValueSymbol var_symbol = NodeFindingMethods.getVariable(method, symbolTable, node.getChildren().get(0).get("name"));

            if(var_symbol == null){
                reports.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Cannot resolve symbol " + node.getChildren().get(0).get("name")));
                report_list.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Cannot resolve symbol " + node.getChildren().get(0).get("name")));

                return reports;
            }
            else if((var_symbol.getType().isArray() == false) && node.getChildren().get(1).get("is_array").equals("true")){
                reports.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Variable " + node.getChildren().get(0).get("name") + " shouldn't be an array but is."));
                report_list.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Variable " + node.getChildren().get(0).get("name") + " shouldn't be an array but is."));

                return reports;
            }
            else if(!NodeFindingMethods.sameType(node.getChildren().get(1).get("type"), var_symbol.getType().getName())){
                reports.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Expected type: " + var_symbol.getType().getName() + ", got type " + node.getChildren().get(1).get("type")));
                report_list.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Expected type: " + var_symbol.getType().getName() + ", got type " + node.getChildren().get(1).get("type")));

                return reports;
            }
            var_symbol.setHas_value(true);
            return reports;
        }
        return reports;
    }

    public List<Report> varDeclaration(JmmNode node, Boolean aBoolean){
        List<Report> reports = new ArrayList<>();

        if(node.getOptional("name").isEmpty()){
            reports.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Assignment to non variable"));
            report_list.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Assignment to non variable"));
            return reports;
        }

        String varName = node.get("name");

        Type varType = new Type(node.getChildren().get(0).get("name"), Boolean.parseBoolean(node.getChildren().get(0).get("is_array")));
        ValueSymbol symbol = new ValueSymbol(varType, varName);

        Method method = NodeFindingMethods.FindParentMethod(node, symbolTable);

        //Add to class' (global) symbol table
        if(method == null){
            if(!symbolTable.fieldExists(symbol)) {
                symbolTable.addField(varType, varName, false);
                return reports;
            }
            else{
                reports.add(newSemanticReport(node, ReportType.ERROR,"VariableRedefinition: Variable " + symbol + " is already defined in the scope"));
                report_list.add(newSemanticReport(node, ReportType.ERROR,"VariableRedefinition: Variable " + symbol + " is already defined in the scope"));

                return reports;
            }
        }
        else{
            Method table_method = symbolTable.getMethod(method);
            if(table_method == null){
                System.out.println("Error: Unreported class");
                return reports;
            }
            if(table_method.localVariableExists(symbol)){
                reports.add(newSemanticReport(node, ReportType.ERROR,"VariableRedefinition: Variable " + symbol + " is already defined in the scope"));
                report_list.add(newSemanticReport(node, ReportType.ERROR,"VariableRedefinition: Variable " + symbol + " is already defined in the scope"));

                return reports;
            }
            table_method.addLocalVariable(varType, varName, false);
        }

        return reports;
    }

    public List<Report> verifyArray(JmmNode node, Boolean aBoolean){
        List<Report> reports = new ArrayList<>();

        JmmNode child = node.getChildren().get(0);

        if(child.getOptional("type").isEmpty() || child.getOptional("type").isEmpty()){
            reports.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Expected type: int, got no type."));
            report_list.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Expected type: int, got no type."));

            return reports;
        }
        if(!child.get("type").equals("int")){
            reports.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Expected type: int, got type " + node.getChildren().get(0).get("type")));
            report_list.add(newSemanticReport(node, ReportType.ERROR,"TypeMismatch: Expected type: int, got type " + node.getChildren().get(0).get("type")));

            return reports;
        }

        node.put("type", child.get("type"));
        node.put("is_array", "true");

        return reports;
    }

    public List<Report> addValueToNodeOptional(JmmNode node, Boolean aBoolean){
        node.put("type", "");
        node.put("is_array", "");
        List<Report> reports = new ArrayList<>();

        Method method = NodeFindingMethods.FindParentMethod(node, symbolTable);
        ValueSymbol symbol = (ValueSymbol) NodeFindingMethods.getVariable(method, symbolTable, node.get("name"));

        if(symbol == null){
            if(node.getParent().getKind().equals("FCall")){
                for(String import_name: symbolTable.getImports()){
                    if(node.get("name").equals(import_name)){
                        return reports;
                    }
                }
                //Assume it's a static method call otherwise
                return reports;
            }
            //In the case of variable declarations where the value isn't in the symbol table yet
            reports.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Cannot resolve symbol " + node.getOptional("name")));
            report_list.add(newSemanticReport(node, ReportType.ERROR,"UndeclaredVariable: Cannot resolve symbol " + node.getOptional("name")));
            return reports;
        }
        node.put("type", symbol.getType().getName());
        node.put("is_array", String.valueOf(symbol.getType().isArray()));
        return reports;
    }

    public List<Report> verifyIfStatement(JmmNode node, Boolean aBoolean){
        List<Report> reports = new ArrayList<>();

        if((! NodeFindingMethods.sameType(node.getChildren().get(0).get("type"), "boolean")) || NodeFindingMethods.sameType(node.getChildren().get(0).get("is_array"), "true")){
            //TODO: Wrong variable type in if statement error
            System.out.println("Wrong type inside of if.");
            return reports;
        }
        return reports;
    }

    public List<Report> verifyNegate(JmmNode node, Boolean aBoolean) {
        node.put("type", "");
        node.put("is_array", "");
        List<Report> reports = new ArrayList<>();

        Boolean invalid_type = false;
        Boolean invalid_array = false;

        JmmNode child_node = node.getChildren().get(0);


        if(NodeFindingMethods.sameType(child_node.getOptional("type"), "boolean")) {
            if (NodeFindingMethods.sameType(child_node.getOptional("is_array"), "false")) {
                if (child_node.getKind().equals("Identifier")) {
                    if (!variablesNotDeclared(child_node, reports)) {
                        node.put("type", "boolean");
                        node.put("is_array", "false");
                        return reports;
                    }
                    //TODO: Add non declared variable report here
                    System.out.println("(Negate) variable not declared.");
                    return reports;
                }
                //Any type gets converted to boolean aswell
                node.put("type", "boolean");
                node.put("is_array", "false");
                return reports;
            }
            else{
                invalid_array = true;
            }
        }
        else{
            invalid_type = true;
        }

        if(invalid_type || invalid_array) {
            String report_string = "TypeMismatch: Operator '!' cannot be applied to ";
            report_string += NodeFindingMethods.getTypeStringReport(child_node);
            reports.add(newSemanticReport(node, ReportType.ERROR, report_string));
            report_list.add(newSemanticReport(node, ReportType.ERROR, report_string));

            //TODO: Add wrong type variable report here
            System.out.println(report_string);
            node.put("type", "");
            node.put("is_array", "");
        }
        return reports;
    }

    public List<Report> defaultVisit(JmmNode node, Boolean aBoolean){
        return new ArrayList<>();
    }
}
