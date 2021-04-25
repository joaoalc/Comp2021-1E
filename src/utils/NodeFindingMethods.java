package utils;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import table.Method;
import table.MySymbolTable;

import java.util.ArrayList;
import java.util.List;

import static utils.Utils.getChildrenOfKind;

public class NodeFindingMethods {

    public static Method FindParentMethod(JmmNode node){
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
            List<Symbol> arguments = new ArrayList<>();

            for (JmmNode argumentNode : argumentNodes) {
                JmmNode type_node = argumentNode.getChildren().get(0);
                Type type = parseTypeNode(type_node);
                String argument_name = argumentNode.get("name");
                arguments.add(new Symbol(type, argument_name));
            }
            return new Method(name, return_type, arguments);
        }
    }

    private static Type parseTypeNode(JmmNode node) {
        String type_name = node.get("name");
        boolean is_array = Boolean.parseBoolean(node.get("is_array"));

        return new Type(type_name, is_array);
    }

    public static boolean variableExists(Method method, MySymbolTable symbolTable, Symbol symbol){
        if(!method.localVariableExists(symbol)){
            if(!symbolTable.fieldExists(symbol)){
                return false;
            }
        }
        return true;
    }

}
