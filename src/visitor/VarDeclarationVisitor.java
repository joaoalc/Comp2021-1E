package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.Method;
import table.MySymbolTable;
import table.ValueSymbol;
import utils.NodeFindingMethods;

import java.util.ArrayList;
import java.util.List;

import static utils.Utils.getChildrenOfKind;

public class VarDeclarationVisitor extends PreorderJmmVisitor<List<Report>, Boolean> {
    MySymbolTable symbolTable;
    public VarDeclarationVisitor(MySymbolTable symbolTable) {
        addVisit("VarDeclaration", this::varDeclaration);
        this.symbolTable = symbolTable;

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
            node.getChildren().get(0).put("type", varType.getName());
            node.getChildren().get(0).put("is_array", String.valueOf(varType.isArray()));
            System.out.println("added variable");
        }

        return true;
    }



    public Method FindParentMethod(JmmNode node){
        JmmNode currentNode = node;
        while(currentNode.getKind().compareTo("ClassBody") != 0 && currentNode.getKind().compareTo("MethodDeclaration") != 0){
            currentNode = currentNode.getParent();
        }
        if(currentNode.getKind().compareTo("ClassBody") == 0){
            return null;
        }
        else{

            JmmNode return_type_node = currentNode.getChildren().get(0);
            Type return_type = parseTypeNode(return_type_node);

            String name = currentNode.get("name");

            List<JmmNode> argumentNodes = getChildrenOfKind(currentNode, "Argument");
            List<ValueSymbol> arguments = new ArrayList<>();

            for (JmmNode argumentNode : argumentNodes) {
                JmmNode type_node = argumentNode.getChildren().get(0);
                Type type = parseTypeNode(type_node);
                String argument_name = argumentNode.get("name");
                arguments.add(new ValueSymbol(type, argument_name));
            }
            return new Method(name, return_type, arguments);
        }
    }

    private Type parseTypeNode(JmmNode node) {
        String type_name = node.get("name");
        boolean is_array = Boolean.parseBoolean(node.get("is_array"));

        return new Type(type_name, is_array);
    }

}
