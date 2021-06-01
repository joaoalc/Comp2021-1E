package utils;

import com.sun.jdi.Value;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import table.Method;
import table.MySymbolTable;
import table.ValueSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static utils.Utils.getChildrenOfKind;
import static utils.Utils.newSemanticReport;

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

    public static Method FindParentMethod(JmmNode node, MySymbolTable symbolTable){
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
            return symbolTable.getMethod(new Method(name, return_type, arguments));
        }
    }

    public static List<JmmNode> getCalls(JmmNode node) {
        List<JmmNode> nodes = new ArrayList<>();
        String kind = node.getKind();

        if (kind.equals("FCall") || kind.equals("NewExpression")) {
            nodes.add(node);
            return nodes;
        }

        for (var child : node.getChildren()) {
            if(child != null) {
                nodes.addAll(getCalls(child));
            }
        }
        return nodes;
    }

    private static Type parseTypeNode(JmmNode node) {
        String type_name = node.get("name");
        boolean is_array = Boolean.parseBoolean(node.get("is_array"));

        return new Type(type_name, is_array);
    }

    public static ValueSymbol getVariable(MySymbolTable symbolTable, String varName){
        return symbolTable.getField(varName);
    }

    public static ValueSymbol getVariable(Method method, MySymbolTable symbolTable, String varName){
        ValueSymbol result;

        result = method.getLocalVariable(varName);
        if(result == null){
            result = symbolTable.getField(varName);
        }

        return result;
    }

    public static boolean isClassField(Method method, MySymbolTable symbolTable, String varName){
        ValueSymbol result;

        result = method.getLocalVariable(varName);
        if(result == null){
            result = symbolTable.getField(varName);
            if(result == null){
                return false;
            }
            return true;
        }

        return false;
    }

    public static boolean variableExists(Method method, MySymbolTable symbolTable, ValueSymbol symbol){
        if(!method.localVariableExists(symbol)){
            if(!symbolTable.fieldExists(symbol)){
                return false;
            }
        }
        return true;
    }

    public static boolean sameType(String type, String intended_type){
        if(type.equals(""))
            return true;
        if(intended_type.equals(""))
            return true;
        if(type.equals(intended_type))
            return true;
        return false;
    }

    public static boolean sameType(Optional<String> type, String intended_type){
        if(type.isEmpty())
            return false;
        if((type.get()).equals(""))
            return true;
        if(intended_type.equals(""))
            return true;
        if((type.get()).equals(intended_type))
            return true;
        return false;
    }

    public static boolean sameType(Optional<String> type, Optional<String> intended_type){
        if(type.isEmpty() || intended_type.isEmpty())
            return false;
        if((type.get()).equals(""))
            return true;
        if((intended_type.get()).equals(""))
            return true;
        if((type.get()).equals((intended_type.get())))
            return true;
        return false;
    }

    public static boolean sameType(String type, Optional<String> intended_type){
        if(intended_type.isEmpty())
            return false;
        if(type.equals(""))
            return true;
        if((intended_type.get()).equals(""))
            return true;
        if(type.equals((intended_type.get())))
            return true;
        return false;
    }

    public static String getTypeStringReport(JmmNode node){
        String report_string = "";
        if(node.getOptional("type").isEmpty() || node.getOptional("is_array").isEmpty()){
            report_string += "<Empty type>";
        }
        else{
            report_string += node.get("type");
            if(node.get("is_array").equals("true"))
                report_string += "[]";
        }
        return report_string;
    }

}
