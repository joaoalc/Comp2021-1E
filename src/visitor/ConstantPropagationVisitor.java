package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmNodeImpl;
import pt.up.fe.comp.jmm.ast.JmmVisitor;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.Method;
import table.MySymbolTable;
import utils.NodeFindingMethods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utils.Utils.getChildrenOfKind;

public class ConstantPropagationVisitor  extends AJmmVisitor<Boolean, List<Report>> {
    public boolean isChanged = false;

    public Map<String, String> varMap = new HashMap<>();

    public Method currentMethod = null;

    public MySymbolTable symbolTable;

    private List<Runnable> toChange = new ArrayList<>();

    public ConstantPropagationVisitor(MySymbolTable symbolTable, List<Report> report_list) {
        this.symbolTable = symbolTable;

        addVisit("Identifier", this::verifyIdentifier);

        addVisit("Assignment", this::verifyAssignment);

        addVisit("MethodDeclaration", this::verifyMethodDeclaration);
        
        //Places to not propagate
        addVisit("IfStatement", this::verifyIfStatement);
        addVisit("WhileStatement", this::verifyWhileStatement);


        //Default
        setDefaultVisit(this::defaultVisit);
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

    private List<Report> verifyMethodDeclaration(JmmNode node, Boolean propagate) {
        final String methodName = node.get("name");
        List<JmmNode> parameterNodes = getChildrenOfKind(node, "Argument");
        List<String> parameter_types = new ArrayList<>();
        for (JmmNode parameterNode : parameterNodes) {
            JmmNode type_node = parameterNode.getChildren().get(0);
            String type_name = type_node.get("name");
            boolean is_array = Boolean.parseBoolean(type_node.get("is_array"));
            final Type type = new Type(type_name, is_array);
            parameter_types.add(type.getName() + (type.isArray() ? "[]" : ""));
        }
        String methodId = String.join("-", methodName, String.join("-", parameter_types));
        currentMethod = symbolTable.getMethod(methodId);

        varMap.clear();
        return defaultVisit(node, true);
    }

    private List<Report> verifyIdentifier(JmmNode node, Boolean propagate) {
        if(!propagate)
            return new ArrayList<>();
        String varName = node.get("name");
        if(varMap.containsKey(varName)){
            String value = varMap.get(varName);
            Type varType = currentMethod.getLocalVariable(varName).getType();
            if(varType.getName().equals("boolean") && !varType.isArray()){
                String result = value.substring(0, 1).toUpperCase() + value.substring(1);
                JmmNode new_node = new JmmNodeImpl(result);
                new_node.put("value", value);
                toChange.add(() -> replaceNode(node, new_node));

                isChanged = true;
            } else if (varType.getName().equals("int") && !varType.isArray()) {
                JmmNode new_node = new JmmNodeImpl("Integer");
                new_node.put("value", value);
                toChange.add(() -> replaceNode(node, new_node));
            }
        }
        return new ArrayList<>();
    }

    private List<Report> verifyAssignment(JmmNode node, Boolean propagate) {
        JmmNode firstChild = node.getChildren().get(0);
        JmmNode secondChild = node.getChildren().get(1);
        if(firstChild.getKind().equals("Identifier")){
            String varName = firstChild.get("name");
            if(currentMethod != null && !NodeFindingMethods.isClassField(currentMethod, symbolTable, varName)){

                if((secondChild.getKind().equals("Integer") || secondChild.getKind().equals("True") || secondChild.getKind().equals("False")) && propagate){
                    varMap.put(varName, secondChild.get("value"));
                    return new ArrayList<>();
                }
                else{
                    varMap.remove(varName);
                }
            }
        }
        visit(firstChild, propagate);
        visit(secondChild, propagate);

        return new ArrayList<>();
    }

    private List<Report> verifyWhileStatement(JmmNode node, Boolean aBoolean) {
        return defaultVisit(node, false);

    }

    private List<Report> verifyIfStatement(JmmNode node, Boolean propagate) {
        return defaultVisit(node, false);
    }

    private List<Report> defaultVisit(JmmNode node, Boolean propagate) {
        for(JmmNode child : node.getChildren()){
            visit(child, propagate);
        }
        return new ArrayList<>();
    }

}
