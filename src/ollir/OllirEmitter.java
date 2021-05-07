package ollir;

import com.sun.jdi.Value;
import org.specs.comp.ollir.Ollir;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.AJmmVisitor;
import pt.up.fe.comp.jmm.ast.JmmVisitor;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import table.Method;
import table.MySymbolTable;
import table.ValueSymbol;
import utils.NodeFindingMethods;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static utils.Utils.getChildrenOfKind;

public class OllirEmitter extends AJmmVisitor<String, OllirData> {

    private MySymbolTable symbolTable;

    private int localVariableCounter = 0;

    public OllirEmitter(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;


        addVisit("ClassDeclaration", this::generateClass);
        addVisit("ClassBody", this::generateClassBody);
        addVisit("MethodDeclaration", this::generateMethod);
        addVisit("MethodReturn", this::generateReturn);
        addVisit("Add", this::generateAdd);
        addVisit("Sub", this::generateSub);
        addVisit("Mult", this::generateMult);
        addVisit("Div", this::generateDiv);
        addVisit("LessThan", this::generateLessThan);
        addVisit("And", this::generateAnd);
        //addVisit("Parentheses", this::verifyParentheses);
        //addVisit("Index", this::verifyIndex);
        //addVisit("FCall", this::verifyCall);
        addVisit("Assignment", this::generateAssignment);
        //addVisit("NewExpression", this::verifyParentheses);
        //addVisit("VarDeclaration", this::varDeclaration);
        //addVisit("IntArray", this::verifyArray);*/
        //addVisit("IfStatement", this::verifyIfStatement);
        //addVisit("WhileStatement", this::verifyIfStatement);

        //addVisit("Negate", this::verifyNegate);

        addVisit("Identifier", this::generateIdentifier);
        addVisit("Integer", this::generateInteger);
        addVisit("MethodBody", this::generateMethodBody);
        addVisit("NewExpression", this::generateNewExpression);
        addVisit("VarCreation", this::generateVarCreation);
        addVisit("FCall", this::generateFCall);
        setDefaultVisit(this::defaultVisit);
    }


    private OllirData generateAssignment(JmmNode node, String methodId) {
        JmmNode identifierNode = node.getChildren().get(0);
        JmmNode valueNode = node.getChildren().get(1);

        String return_type;
        String ollir_code = "";
        OllirData identifierData = visit(identifierNode, methodId);
        return_type = identifierData.getReturnVar();


        OllirData data = visit(valueNode, methodId);
        if (data.getOllirCode().equals("")) {
            //For things like integers or identifiers (a = 2; a = b)
            if (valueNode.getOptional("type").isPresent()) {
                ollir_code += identifierData.getReturnVar() + " :=." + getVarOllirType(valueNode) + " " + data.getReturnVar() + ";\n";
            }
            else {
                //???
            }
        }
        else {
            ollir_code += data.getOllirCode();
            ollir_code += identifierData.getReturnVar() + " :=." + getVarOllirType(valueNode) + " " + data.getReturnVar() + ";\n";
        }

        return new OllirData(return_type, ollir_code);
    }

    private OllirData generateLessThan(JmmNode node, String methodId) {
        JmmNode firstOpNode = node.getChildren().get(0);
        JmmNode secondOpNode = node.getChildren().get(1);

        OllirData firstOp = visit(firstOpNode, methodId);
        OllirData secondOp = visit(secondOpNode, methodId);

        String ollirCode = "";

        ollirCode += firstOp.getOllirCode() + secondOp.getOllirCode();

        String name = getVarAssignmentName(node);
        ollirCode += name + ".bool :=.bool " + firstOp.getReturnVar() + " <.i32 " + secondOp.getReturnVar() + ";\n";

        return new OllirData(name + ".bool", ollirCode);
    }

    private OllirData generateDiv(JmmNode node, String methodId) {
        JmmNode firstOpNode = node.getChildren().get(0);
        JmmNode secondOpNode = node.getChildren().get(1);

        OllirData firstOp = visit(firstOpNode, methodId);
        OllirData secondOp = visit(secondOpNode, methodId);

        String ollirCode = "";

        ollirCode += firstOp.getOllirCode() + secondOp.getOllirCode();

        String name = getVarAssignmentName(node);
        ollirCode += name + ".i32 :=.i32 " + firstOp.getReturnVar() + " /.i32 " + secondOp.getReturnVar() + ";\n";

        return new OllirData(name + ".i32", ollirCode);
    }

    private OllirData generateMult(JmmNode node, String methodId) {
        JmmNode firstOpNode = node.getChildren().get(0);
        JmmNode secondOpNode = node.getChildren().get(1);

        OllirData firstOp = visit(firstOpNode, methodId);
        OllirData secondOp = visit(secondOpNode, methodId);

        String ollirCode = "";

        ollirCode += firstOp.getOllirCode() + secondOp.getOllirCode();

        String name = getVarAssignmentName(node);
        ollirCode += name + ".i32 :=.i32 " + firstOp.getReturnVar() + " *.i32 " + secondOp.getReturnVar() + ";\n";

        return new OllirData(name + ".i32", ollirCode);
    }

    private OllirData generateSub(JmmNode node, String methodId) {
        JmmNode firstOpNode = node.getChildren().get(0);
        JmmNode secondOpNode = node.getChildren().get(1);

        OllirData firstOp = visit(firstOpNode, methodId);
        OllirData secondOp = visit(secondOpNode, methodId);

        String ollirCode = "";

        ollirCode += firstOp.getOllirCode() + secondOp.getOllirCode();

        String name = getVarAssignmentName(node);
        ollirCode += name + ".i32 :=.i32 " + firstOp.getReturnVar() + " -.i32 " + secondOp.getReturnVar() + ";\n";

        return new OllirData(name + ".i32", ollirCode);
    }

    private OllirData generateAdd(JmmNode node, String methodId) {
        JmmNode firstOpNode = node.getChildren().get(0);
        JmmNode secondOpNode = node.getChildren().get(1);

        OllirData firstOp = visit(firstOpNode, methodId);
        OllirData secondOp = visit(secondOpNode, methodId);

        String ollirCode = "";

        ollirCode += firstOp.getOllirCode() + secondOp.getOllirCode();

        String name = getVarAssignmentName(node);
        ollirCode += name + ".i32" + " :=.i32 " + firstOp.getReturnVar() + " +.i32 " + secondOp.getReturnVar() + ";\n";

        return new OllirData(name + ".i32", ollirCode);
    }

    private OllirData generateAnd(JmmNode node, String methodId) {
        JmmNode firstOpNode = node.getChildren().get(0);
        JmmNode secondOpNode = node.getChildren().get(1);

        OllirData firstOp = visit(firstOpNode, methodId);
        OllirData secondOp = visit(secondOpNode, methodId);

        String ollirCode = "";

        ollirCode += firstOp.getOllirCode() + secondOp.getOllirCode();

        String name = getVarAssignmentName(node);
        ollirCode += name + ".bool :=.bool " + firstOp.getReturnVar() + " &&.bool " + secondOp.getReturnVar() + ";\n";

        return new OllirData(name + ".bool", ollirCode);
    }

    private OllirData generateInteger(JmmNode jmmNode, String s) {
        //TODO: (for self) Check if ollirCode is right later
        return new OllirData(jmmNode.get("value") + ".i32", "");
    }

    private OllirData generateIdentifier(JmmNode jmmNode, String s) {
        String return_type = getVarOllirType(jmmNode);
        return new OllirData(jmmNode.get("name") + "." + return_type, "");
    }

    public OllirData defaultVisit(JmmNode node, String data) {
        String ollirCode = "";

        for (JmmNode child : node.getChildren()) {
            OllirData childOllir = visit(child, null);
            ollirCode += childOllir.getOllirCode();
        }

        return new OllirData(ollirCode);
    }

    private OllirData generateClass(JmmNode node, String data) {
        String ollirCode = "";

        List<String> imports = symbolTable.getImports();

        for (String importString : imports) {
            ollirCode += "import " + importString + ";\n";
        }

        String className = symbolTable.getClassName();
        String superClass = symbolTable.getSuper();

        ollirCode += className + " " +
            (superClass != null ? ("extends " + superClass) : "") + " {\n";

        List<Symbol> classFields = symbolTable.getFields();

        for (Symbol field : classFields) {
            Type fieldType = field.getType();
            String fieldName = field.getName();

            ollirCode += ".field " + fieldName + "." + getOllirType(fieldType) + ";\n";
        }

        ollirCode += ".construct " + className + "().V {\n" +
            "invokespecial(this, \"<init>\").V;\n" +
            "}\n";


        for (JmmNode child : node.getChildren()) {
            OllirData childData = visit(child, null);
            ollirCode += childData.getOllirCode();
        }

        ollirCode += "}";

        return new OllirData(ollirCode);
    }

    private OllirData generateClassBody(JmmNode node, String data) {
        String ollirCode = "";
        for (JmmNode child : node.getChildren()) {
            OllirData childData = visit(child, null);
            ollirCode += childData.getOllirCode() + "\n";
        }
        return new OllirData("", ollirCode);
    }

    private OllirData generateMethod(JmmNode node, String data) {
        String ollirCode = "";

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

        localVariableCounter = 0;

        final Type returnType = symbolTable.getReturnType(methodId);

        final List<ValueSymbol> parameters = symbolTable.getParams(methodId);

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(".method public " + (methodName.equals("main") ? "static " : "") + methodName + "(");
        List<String> parametersOllir = new ArrayList<>();
        for (Symbol parameter : parameters) {
            final Type parameterType = parameter.getType();

            parametersOllir.add(parameter.getName() + "." + getOllirType(parameterType));
        }
        stringBuilder.append(String.join(", ", parametersOllir) + ")." + getOllirType(returnType) + " {\n");

        for (JmmNode child : node.getChildren()) {
            OllirData childData = visit(child, methodId);
            ollirCode += childData.getOllirCode();
        }

        return new OllirData(stringBuilder.toString() + ollirCode + "}\n");
    }

    private OllirData generateMethodBody(JmmNode node, String methodId) {
        String ollirCode = "";

        for (JmmNode child : node.getChildren()) {
            OllirData childData = visit(child, methodId);
            ollirCode += childData.getOllirCode();
        }

        return new OllirData(ollirCode + "\n");
    }

    private OllirData generateReturn(JmmNode node, String methodId) {
        JmmNode returnExpression = node.getChildren().get(0);

        OllirData expression = visit(returnExpression, methodId);

        String ollirCode = expression.getOllirCode() +
            "ret." + getOllirType(symbolTable.getReturnType(methodId));

        ollirCode += " " + expression.getReturnVar() + ";\n";

        return new OllirData(ollirCode);
    }

    private OllirData generateNewExpression(JmmNode node, String methodId) {
        OllirData data = visit(node.getChildren().get(0), methodId);

        return data;
    }

    private OllirData generateVarCreation(JmmNode node, String methodId) {
        String return_type = getVarOllirType(node);
        String aux = "aux_" + localVariableCounter++;
        String ollir_code = aux + "." + return_type + " :=." + return_type + " new(" + return_type + ")." + return_type + ";\n";
        ollir_code += "invokespecial(" + aux + "." + return_type + ",\"<init>\").V;\n";

        return new OllirData(aux + "." + return_type, ollir_code);
    }

    private OllirData generateFCall(JmmNode node, String methodId) {
        String ollir_code = "";
        JmmNode identifier_node = node.getChildren().get(0);

        JmmNode function_node = node.getChildren().get(1);
        String function_name = function_node.get("name");

        List<String> args = new ArrayList<>();

        List<ValueSymbol> types = new ArrayList<>();

        // arguments
        for (JmmNode child : function_node.getChildren()) {
            OllirData childData = visit(child, methodId);
            //ollirCode += childData.getOllirCode();
            args.add(childData.getReturnVar());
            //TODO: Add value or operation possibility to this
            types.add(NodeFindingMethods.getVariable(symbolTable.getMethod(methodId), symbolTable, child.get("name")));
        }


        String return_var = "";
        if (identifier_node.getKind().equals("This")) {
            //this.function(arguments);
            Method method = symbolTable.getMethod(function_name, types);
            return_var = "aux_" + localVariableCounter++ + "." + getOllirType(method.getReturnType());
            ollir_code += return_var + ":=." + getOllirType(method.getReturnType()) + " invokevirtual(this, \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + getOllirType(method.getReturnType()) + ";";

        }
        else {
            String identifier_name = identifier_node.get("name");
            ValueSymbol symbol = NodeFindingMethods.getVariable(symbolTable.getMethod(methodId), symbolTable, identifier_name);
            if (symbol == null) {
                //Import
                //ollir_code += "invokestatic(" + identifier_name + "." + symbolTable.getClassName() + "," + function_name + ").V;";
                ollir_code += "invokestatic(" + identifier_name + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ").V;";


            }
            else {
                //Variable
                if (symbol.getType().getName().equals(symbolTable.getClassName()) && symbolTable.getSuper() == null) {
                    Method method = symbolTable.getMethod(function_name, types);
                    ValueSymbol identifier = NodeFindingMethods.getVariable(symbolTable.getMethod(methodId), symbolTable, identifier_name);
                    return_var = "aux_" + localVariableCounter++ + "." + getOllirType(method.getReturnType());
                    ollir_code += return_var + ":=." + getOllirType(method.getReturnType()) + " invokevirtual(" + identifier_name + "." + getOllirType(identifier.getType()) + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + getOllirType(method.getReturnType()) + ";";

                }

            }
        }

        return new OllirData(return_var, ollir_code + "\n");
    }

    public String getVarAssignmentName(JmmNode jmmNode) {
        JmmNode parent_node = jmmNode.getParent();
        String name = "";
        if (parent_node.getKind().equals("Assignment")) {
            JmmNode identifier_node = parent_node.getChildren().get(0); // TODO: This will probably not work with an indice, in that case we would have to check it's child for the name
            name = identifier_node.get("name");
        }
        else {
            name = "aux" + localVariableCounter++;
        }

        return name;
    }

    private String getVarOllirType(JmmNode jmmNode) {
        String return_type = "";
        if (jmmNode.get("type").equals("int")) {
            return_type += "i32";
            if (jmmNode.get("is_array").equals("true")) {
                return_type += ".array";
            }
        }
        else if (jmmNode.get("type").equals("boolean")) {
            return_type += "bool";
        }
        else {
            return_type += jmmNode.get("type");
        }
        return return_type;
    }

    private String getOllirType(Type type) {
        String name = type.getName();

        String ollirType;
        if (name.equals("int")) {
            ollirType = "i32";
        }
        else if (name.equals("void")) {
            ollirType = "V";
        }
        else {
            ollirType = name;
        }

        return (type.isArray() ? "array." : "") + ollirType;
    }


}