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
import utils.OllirUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static utils.Utils.getChildrenOfKind;

public class OllirEmitter extends AJmmVisitor<String, OllirData> {

    private MySymbolTable symbolTable;

    private int localVariableCounter = 0;

    private int labelCounter = 0;

    public OllirEmitter(MySymbolTable symbolTable) {
        this.symbolTable = symbolTable;

        //Class related
        addVisit("ClassDeclaration", this::generateClass);
        addVisit("ClassBody", this::generateClassBody);


        //Method related
        addVisit("MethodDeclaration", this::generateMethod);
        addVisit("MethodReturn", this::generateReturn);
        addVisit("MethodBody", this::generateMethodBody);


        //Binary operators
        //int, int returns int
        addVisit("Add", this::generateAdd);
        addVisit("Sub", this::generateSub);
        addVisit("Mult", this::generateMult);
        addVisit("Div", this::generateDiv);
        //int, int returns boolean
        addVisit("LessThan", this::generateLessThan);
        //boolean, boolean returns boolean
        addVisit("And", this::generateAnd);
        addVisit("Negate", this::generateNegate); //! is a binary operator in ollir for some reason


        addVisit("Parentheses", this::verifyParentheses);
        addVisit("Index", this::generateIndex);
        //addVisit("FCall", this::verifyCall);
        addVisit("Assignment", this::generateAssignment);
        //addVisit("VarDeclaration", this::varDeclaration);
        addVisit("IntArray", this::generateArray);
        addVisit("IfStatement", this::generateIf);
        addVisit("WhileStatement", this::generateWhile);
        //addVisit("IntArray", this::verifyArray);*/
        //addVisit("IfStatement", this::verifyIfStatement);
        //addVisit("WhileStatement", this::verifyIfStatement);


        addVisit("Identifier", this::generateIdentifier);
        addVisit("Integer", this::generateInteger);
        addVisit("True", this::generateTrue);
        addVisit("False", this::generateFalse);
        addVisit("NewExpression", this::generateNewExpression);
        addVisit("VarCreation", this::generateVarCreation);
        addVisit("FCall", this::generateFCall);

        addVisit("Statement1", this::generateStatement1);
        addVisit("Statement2", this::generateStatement2);
        setDefaultVisit(this::defaultVisit);
    }

    private OllirData generateStatement1(JmmNode node, String s) {
        if (node.getChildren().size() > 0) {
            return visit(node.getChildren().get(0), s);
        }
        return new OllirData("", "");
    }

    private OllirData generateStatement2(JmmNode node, String s) {
        if (node.getChildren().size() > 0) {
            return visit(node.getChildren().get(0), s);
        }
        return new OllirData("", "");
    }

    private OllirData generateIndex(JmmNode node, String s) {
        String return_type = "";
        String ollir_code = "";
        JmmNode arrayNode = node.getChildren().get(0);
        OllirData arrayData = visit(arrayNode, s);
        JmmNode indexNode = node.getChildren().get(1);
        OllirData indexData = visit(indexNode, s);
        System.out.println("Array data: " + arrayData.getOllirCode());
        ollir_code += arrayData.getOllirCode();
        ollir_code += indexData.getOllirCode();

        JmmNode parent_node = node.getParent();

        //TODO: Refactoring: Put this inside the getVarAssignmentName function, somehow
        if (parent_node.getKind().equals("Assignment")) {
            if (indexNode.getKind().equals("Integer")) {
                ollir_code += "aux" + localVariableCounter + ".i32 :=.i32 " + indexData.getReturnVar() + ";\n";
                return_type = node.get("name") + "[" + "aux" + localVariableCounter++ + ".i32" + "]." + getOllirType(new Type(arrayNode.get("type"), false));
            }
            else {
                return_type = node.get("name") + "[" + indexData.getReturnVar() + "]." + getOllirType(new Type(arrayNode.get("type"), false));
            }
        }
        else {
            return_type = "aux" + localVariableCounter++ + ".i32";
        }

        return new OllirData(return_type, ollir_code);
    }

    private OllirData generateArray(JmmNode node, String s) {
        String return_type = "";
        String ollir_code = "";
        JmmNode childNode = node.getChildren().get(0);
        OllirData childData = visit(childNode, s);

        ollir_code += childData.getOllirCode();
        return_type += "new(array, " + childData.getReturnVar() + ")" + ".array.i32";

        return new OllirData(return_type, ollir_code);
    }

    private OllirData verifyParentheses(JmmNode node, String s) {
        return visit(node.getChildren().get(0), s);
    }

    private OllirData generateIf(JmmNode node, String s) {
        String return_type = "";
        String ollir_code = "";

        JmmNode conditionNode = node.getChildren().get(0);
        JmmNode trueNode = node.getChildren().get(1);
        String trueString = visit(trueNode, s).getOllirCode();
        JmmNode elseNode = node.getChildren().get(2);
        String elseString = visit(elseNode, s).getOllirCode();

        String conditionString = "";

        switch (conditionNode.getKind()) {
            case "LessThan":
                OllirData firstNodeLT = visit(conditionNode.getChildren().get(0), s);
                OllirData secondNodeLT = visit(conditionNode.getChildren().get(1), s);
                ollir_code += firstNodeLT.getOllirCode() + secondNodeLT.getOllirCode();
                conditionString = generateGreaterOrEqualAuxiliar(conditionNode, s, firstNodeLT, secondNodeLT).getOllirCode();
                break;
            case "And":
                //OllirData firstNodeAnd = visit(conditionNode.getChildren().get(0), s);
                //OllirData secondNodeAnd = visit(conditionNode.getChildren().get(1), s);

                OllirData firstNegated = generateNegateAuxVar(node.getChildren().get(0).getChildren().get(0), s);
                OllirData secondNegated = generateNegateAuxVar(node.getChildren().get(0).getChildren().get(1), s);
                ollir_code += firstNegated.getOllirCode() + secondNegated.getOllirCode();

                conditionString = generateOrAuxiliar(conditionNode, s, firstNegated.getReturnVar(), secondNegated.getReturnVar()).getOllirCode();
                break;
            case "True":
                conditionString += "0.bool" + " !.bool " + generateTrue(conditionNode, s).getReturnVar();
                break;
            case "False":
                conditionString += "0.bool" + " !.bool " + generateFalse(conditionNode, s).getReturnVar();
                break;
            case "Negate":
                OllirData nodeNegate = visit(conditionNode.getChildren().get(0), s);
                ollir_code += nodeNegate.getOllirCode();

                conditionString += "1.bool" + " &&.bool " + nodeNegate.getReturnVar();
                break;
            case "Identifier":
                OllirData nodeIdentifier = visit(conditionNode, s);
                ollir_code += nodeIdentifier.getOllirCode();
                conditionString += "0.bool" + " !.bool " + nodeIdentifier.getReturnVar();
                break;
            case "FCall":
                OllirData nodeFCall = visit(conditionNode, s);
                ollir_code += nodeFCall.getOllirCode();
                ollir_code += "aux" + localVariableCounter + ".bool" + " :=.bool " + nodeFCall.getReturnVar() + ";\n";
                conditionString += "0.bool" + " !.bool " + "aux" + localVariableCounter++ + ".bool";
                break;
            default:
                System.out.println("This condition of the if statement isn't done yet.");
                System.out.println(conditionNode.getKind());
                break;
        }

        ollir_code += "if(" + conditionString + ")" + "goto else" + labelCounter + ";\n" + trueString + "\ngoto endif" + labelCounter + ";\n" + "else" + labelCounter + ":\n" + elseString + "endif" + labelCounter + ":\n";

        labelCounter++;

        return new OllirData(return_type, ollir_code);
    }

    private OllirData generateWhile(JmmNode node, String s) {
        String return_type = "";
        String ollir_code = "";

        ollir_code += "Loop" + labelCounter + ":\n";

        JmmNode conditionNode = node.getChildren().get(0);
        JmmNode trueNode = node.getChildren().get(1);
        String trueString = visit(trueNode, s).getOllirCode();

        String conditionString = "";

        switch (conditionNode.getKind()) {
            case "LessThan":
                OllirData firstNodeLT = visit(conditionNode.getChildren().get(0), s);
                OllirData secondNodeLT = visit(conditionNode.getChildren().get(1), s);
                ollir_code += firstNodeLT.getOllirCode() + secondNodeLT.getOllirCode();
                conditionString = generateLessAuxiliar(conditionNode, s, firstNodeLT, secondNodeLT).getOllirCode();
                break;
            case "And":
                //OllirData firstNodeAnd = visit(conditionNode.getChildren().get(0), s);
                //OllirData secondNodeAnd = visit(conditionNode.getChildren().get(1), s);

                OllirData firstNegated = visit(node.getChildren().get(0).getChildren().get(0), s);
                OllirData secondNegated = visit(node.getChildren().get(0).getChildren().get(1), s);
                ollir_code += firstNegated.getOllirCode() + secondNegated.getOllirCode();

                conditionString = generateAndAuxiliar(conditionNode, s, firstNegated.getReturnVar(), secondNegated.getReturnVar()).getOllirCode();
                break;
            case "True":
                conditionString += "0.bool" + " !.bool " + generateFalse(conditionNode, s).getReturnVar();
                break;
            case "False":
                conditionString += "0.bool" + " !.bool " + generateTrue(conditionNode, s).getReturnVar();
                break;
            case "Negate":
                OllirData nodeNegate = visit(conditionNode.getChildren().get(0), s);
                ollir_code += nodeNegate.getOllirCode();

                conditionString += "0.bool" + " !.bool " + nodeNegate.getReturnVar();
                break;
            case "Identifier":
                OllirData nodeIdentifier = visit(conditionNode, s);
                ollir_code += nodeIdentifier.getOllirCode();
                conditionString += "1.bool" + " &&.bool " + nodeIdentifier.getReturnVar();
                break;
            case "FCall":
                OllirData nodeFCall = visit(conditionNode, s);
                ollir_code += nodeFCall.getOllirCode();
                ollir_code += "aux" + localVariableCounter + ".bool" + " :-.bool" + nodeFCall.getReturnVar() + ";\n";
                conditionString += "1.bool" + " &&.bool " + "aux" + localVariableCounter++ + ".bool";
                break;
            default:
                System.out.println("This condition of the if statement isn't done yet.");
                break;
        }

        ollir_code += "if(" + conditionString + ")" + "goto Body" + labelCounter + ";\n";
        ollir_code += "goto EndLoop" + labelCounter + ";\n";
        ollir_code += "Body" + labelCounter + ": " + trueString;
        ollir_code += "goto Loop" + labelCounter + ";\n";
        ollir_code += "EndLoop" + labelCounter + ":\n";

        //   trueString + "\nif(" + conditionString + ")" + "goto Loop" + labelCounter + ";\n" + "else" + labelCounter + ":\n"
        labelCounter++;
        return new OllirData(return_type, ollir_code);
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
                //TODO: Is this a class field?
                /*if(identifierNode.getOptional("putfield_required").isPresent()){
                    if(identifierNode.get("putfield_required").equals("true")){
                        ollir_code += "putfield(this, " + identifierNode.get("name") + "." + getVarOllirType(valueNode) + ", " + data.getReturnVar() + ")." + getVarOllirType(valueNode) + ";\n";
                    }
                }*/
                ollir_code += identifierData.getReturnVar() + " :=." + getVarOllirType(valueNode) + " " + data.getReturnVar() + ";\n";
            }
            else{
                // ????
            }
        }
        else {
            ollir_code += data.getOllirCode();
            if (getVarOllirType(valueNode).equals("")) {
                //TODO: This might not work properly for assignment to an index
                ollir_code += identifierData.getReturnVar() + " :=." + getVarOllirType(identifierNode) + " " + data.getReturnVar() + ";\n";
            }
            else {
                ollir_code += identifierData.getReturnVar() + " :=." + getVarOllirType(valueNode) + " " + data.getReturnVar() + ";\n";
            }
        }

        return new OllirData(return_type, ollir_code);
    }

    private OllirData generateGreaterOrEqualAuxiliar(JmmNode node, String methodId, OllirData firstOpReturnVar, OllirData secondOpReturnVar) {
        String ollirCode = "";

        ollirCode += firstOpReturnVar.getReturnVar() + " >=.i32 " + secondOpReturnVar.getReturnVar();

        return new OllirData(".bool", ollirCode);
    }

    private OllirData generateLessAuxiliar(JmmNode node, String methodId, OllirData firstOpReturnVar, OllirData secondOpReturnVar) {
        String ollirCode = "";

        ollirCode += firstOpReturnVar.getReturnVar() + " <.i32 " + secondOpReturnVar.getReturnVar();

        return new OllirData(".bool", ollirCode);
    }

    private OllirData generateOrAuxiliar(JmmNode node, String methodId, String firstOpReturnVar, String secondOpReturnVar) {
        String ollirCode = "";

        ollirCode += firstOpReturnVar + " ||.bool " + secondOpReturnVar;

        return new OllirData(".bool", ollirCode);
    }

    private OllirData generateAndAuxiliar(JmmNode node, String methodId, String firstOpReturnVar, String secondOpReturnVar) {
        String ollirCode = "";

        ollirCode += firstOpReturnVar + " &&.bool " + secondOpReturnVar;

        return new OllirData(".bool", ollirCode);
    }

    //Generates negate var and all vars under it
    private OllirData generateNegateAuxVar(JmmNode node, String methodId) {

        //
        JmmNode OpNode = node/*.getChildren().get(0)*/;

        OllirData Op = visit(OpNode, methodId);

        String ollirCode = "";

        ollirCode += Op.getOllirCode();

        String name = getVarAssignmentName(node);
        ollirCode += name + ".bool :=.bool " + "0.bool" + " !.bool " + Op.getReturnVar();

        if (OllirUtils.IsEndOfLine(node)) {
            ollirCode += ";\n";
        }

        return new OllirData(name + ".bool", ollirCode);

    }

    private OllirData generateLessThan(JmmNode node, String methodId) {
        JmmNode firstOpNode = node.getChildren().get(0);
        JmmNode secondOpNode = node.getChildren().get(1);

        OllirData firstOp = visit(firstOpNode, methodId);
        OllirData secondOp = visit(secondOpNode, methodId);

        String ollirCode = "";

        ollirCode += firstOp.getOllirCode() + secondOp.getOllirCode();

        String name = getVarAssignmentName(node);
        ollirCode += name + ".bool :=.bool " + firstOp.getReturnVar() + " <.i32 " + secondOp.getReturnVar();

        if (OllirUtils.IsEndOfLine(node)) {
            ollirCode += ";\n";
        }

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
        ollirCode += name + ".i32 :=.i32 " + firstOp.getReturnVar() + " /.i32 " + secondOp.getReturnVar();

        if (OllirUtils.IsEndOfLine(node)) {
            ollirCode += ";\n";
        }

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
        ollirCode += name + ".i32 :=.i32 " + firstOp.getReturnVar() + " *.i32 " + secondOp.getReturnVar();

        if (OllirUtils.IsEndOfLine(node)) {
            ollirCode += ";\n";
        }

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
        ollirCode += name + ".i32 :=.i32 " + firstOp.getReturnVar() + " -.i32 " + secondOp.getReturnVar();

        if (OllirUtils.IsEndOfLine(node)) {
            ollirCode += ";\n";
        }

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
        ollirCode += name + ".i32" + " :=.i32 " + firstOp.getReturnVar() + " +.i32 " + secondOp.getReturnVar();

        if (OllirUtils.IsEndOfLine(node)) {
            ollirCode += ";\n";
        }

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
        ollirCode += name + ".bool :=.bool " + firstOp.getReturnVar() + " &&.bool " + secondOp.getReturnVar();

        if (OllirUtils.IsEndOfLine(node)) {
            ollirCode += ";\n";
        }
        return new OllirData(name + ".bool", ollirCode);
    }

    private OllirData generateNegate(JmmNode node, String methodId) {
        JmmNode OpNode = node.getChildren().get(0);

        OllirData Op = visit(OpNode, methodId);

        String ollirCode = "";

        ollirCode += Op.getOllirCode();

        String name = getVarAssignmentName(node);
        ollirCode += name + ".bool :=.bool " + "0.bool" + " !.bool " + Op.getReturnVar();

        if (OllirUtils.IsEndOfLine(node)) {
            ollirCode += ";\n";
        }
        return new OllirData(name + ".bool", ollirCode);
    }


    private OllirData generateInteger(JmmNode jmmNode, String s) {
        //TODO: (for self) Check if ollirCode is right later
        return new OllirData(jmmNode.get("value") + ".i32", "");
    }

    private OllirData generateTrue(JmmNode jmmNode, String s) {
        //TODO: (for self) Check if ollirCode is right later
        return new OllirData(1 + ".bool", "");
    }

    private OllirData generateFalse(JmmNode jmmNode, String s) {
        //TODO: (for self) Check if ollirCode is right later
        return new OllirData(0 + ".bool", "");
    }

    private OllirData generateIdentifier(JmmNode jmmNode, String s) {
        String return_type = getVarOllirType(jmmNode);
        OllirData fieldData = getOrPutFieldCode(jmmNode, s, return_type);

        //Not a class field
        if(fieldData == null){
            return new OllirData(jmmNode.get("name") + "." + return_type, "");
        }
        else if(fieldData.getOllirCode().equals("")){
            //putfield
            jmmNode.put("putfield_required", "true");
            return new OllirData(jmmNode.get("name") + "." + return_type, "");
        }
        else{
            return fieldData;
        }
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

//        System.out.println("Full code:\n");
        System.out.println(ollirCode);
//        System.out.println("End of code.");

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

        String return_var = "";
        if (!function_node.getKind().equals("Length")) {
            //String ollirCode = "";
            List<String> args = new ArrayList<>();

            List<ValueSymbol> types = new ArrayList<>();

            // arguments
            for (JmmNode child : function_node.getChildren()) {
                OllirData childData = visit(child, methodId);
                //ollirCode += childData.getOllirCode();
                args.add(childData.getReturnVar());
                if (child.getKind().equals("Integer")) {
                    types.add(new ValueSymbol(new Type("int", false), "", true));
                }
                else if (child.getKind().equals("True") || child.getKind().equals("False")) {
                    types.add(new ValueSymbol(new Type("boolean", false), "", true));
                }
                else if (child.getOptional("name").isPresent()) {
                    //TODO: Add value or operation possibility to this
                    types.add(NodeFindingMethods.getVariable(symbolTable.getMethod(methodId), symbolTable, child.get("name")));
                }
                else {
                    ollir_code += childData.getOllirCode();
                    types.add(new ValueSymbol(new Type(child.get("type"), Boolean.parseBoolean(child.get("is_array"))), "", true));
                }
            }


            if (identifier_node.getKind().equals("This")) {
                Method method = symbolTable.getMethod(function_name, types);
                if (method != null) {
                    return_var = "aux_" + localVariableCounter++ + "." + getOllirType(method.getReturnType());
                    ollir_code += return_var + ":=." + getOllirType(method.getReturnType()) + " invokevirtual(this, \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + getOllirType(method.getReturnType()) + ";";
                }
                else {
                    //Calling a superclass' method (arguments' ollir code is already written)
                    String type = getFunctionTypeIfNonExistant(node);
                    if (!type.equals("V")) {
                        return_var = getVarAssignmentName(node) + "." + type;
                        ollir_code += return_var + " :=." + type + " invokevirtual(this, \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
                    }
                    else {
                        ollir_code += "invokevirtual(this, \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
                    }
                }


            }
            else {
                if (identifier_node.getOptional("name").isPresent()) {
                    String identifier_name = identifier_node.get("name");
                    ValueSymbol symbol = NodeFindingMethods.getVariable(symbolTable.getMethod(methodId), symbolTable, identifier_name);
                    if (symbol == null) {
                        //Import
                        String type = getFunctionTypeIfNonExistant(node);
                        if (!type.equals("V")) {
                            return_var = "aux_" + localVariableCounter++ + "." + type;
                            ollir_code += return_var + " :=." + type + " invokestatic(" + identifier_name + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ").V;";
                        }
                        else {
                            return_var = "";
                            ollir_code += "invokestatic(" + identifier_name + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ").V;";
                        }

                    }
                    else {
                        //Variable
                        if (symbol.getType().getName().equals(symbolTable.getClassName()) && symbolTable.getSuper() == null) {
                            Method method = symbolTable.getMethod(function_name, types);
                            ValueSymbol identifier = NodeFindingMethods.getVariable(symbolTable.getMethod(methodId), symbolTable, identifier_name);
                            return_var = "aux_" + localVariableCounter++ + "." + getOllirType(method.getReturnType());
                            ollir_code += return_var + ":=." + getOllirType(method.getReturnType()) + " invokevirtual(" + identifier_name + "." + getOllirType(identifier.getType()) + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + getOllirType(method.getReturnType()) + ";";

                        }
                        else if (symbolTable.getSuper() != null) {
                            //TODO
                            Method method = symbolTable.getMethod(function_name, types);
                            ValueSymbol identifier = NodeFindingMethods.getVariable(symbolTable.getMethod(methodId), symbolTable, identifier_name);
                            if (method != null) {
                                return_var = "aux_" + localVariableCounter++ + "." + getOllirType(method.getReturnType());
                                ollir_code += return_var + ":=." + getOllirType(method.getReturnType()) + " invokevirtual(" + identifier_name + "." + getOllirType(identifier.getType()) + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + getOllirType(method.getReturnType()) + ";";
                            }
                            else {
                                String type = getFunctionTypeIfNonExistant(node);
                                if (!type.equals("V")) {
                                    return_var = "aux_" + localVariableCounter++ + "." + type;
                                    ollir_code += return_var + ":=." + type + " invokevirtual(" + identifier_name + "." + getOllirType(identifier.getType()) + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
                                }
                                else {
                                    return_var = "";
                                    ollir_code += "invokevirtual(" + identifier_name + "." + getOllirType(identifier.getType()) + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
                                }
                            }
                        }

                    }
                }
                else {
                    //This happens when an object is created and one of it's functions are immediately called. Eg: pi_estimate_times_100 = new MonteCarloPi().estimatePi100(num_samples); in the MonteCarloPi test
                    //TODO: Check if it's its own class and act accordingly
                    String type = getFunctionTypeIfNonExistant(node);
                    if (!type.equals("V")) {
                        return_var = "aux_" + localVariableCounter++ + "." + type;
                        ollir_code += return_var + ":=." + type + " invokevirtual(this, \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
                    }
                    else {
                        return_var = "";
                        ollir_code += "invokevirtual(this, \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
                    }
                }
            }
            ollir_code += "\n";
        }
        else {
            OllirData idData = visit(identifier_node, methodId);
            ollir_code += idData.getOllirCode();
            JmmNode parent_node = node.getParent();
            //TODO: Refactoring: Put this and the index version inside the getVarAssignmentName function, somehow
            if (parent_node.getKind().equals("Assignment")) {
                return_var = "arraylength(" + idData.getReturnVar() + ").i32";
            }
            else {
                ollir_code += "aux" + localVariableCounter + ".i32 :=.i32 " + "arraylength(" + idData.getReturnVar() + ").i32" + ";\n";
                return_var = "aux" + localVariableCounter++ + ".i32";
            }
            //return_var = "arraylength(" + idData.getReturnVar() + ").i32";
        }

        return new OllirData(return_var, ollir_code);
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

    private String getFunctionTypeIfNonExistant(JmmNode node) {
        //TODO: Check if this works for array assignment
        JmmNode parent_node = node.getParent();
        String type = "";
        if (parent_node.getKind().equals("Assignment")) {
            //TODO: Implement for indices
            type = getOllirTypeNotVoid(new Type(parent_node.getChildren().get(0).get("type"), Boolean.parseBoolean(parent_node.getChildren().get(0).get("is_array"))));
        }
        else if (parent_node.getKind().equals("Add") || parent_node.getKind().equals("Sub") || parent_node.getKind().equals("Mult") || parent_node.getKind().equals("Div") || parent_node.getKind().equals("LessThan")) {
            type = "i32";
        }
        else if (parent_node.getKind().equals("And") || parent_node.getKind().equals("Negate") || parent_node.getKind().equals("IfStatement")) {
            type = "bool";
        }
        else {
            type = "V";
        }
        return type;
    }

    private String getVarOllirType(JmmNode jmmNode) {
        String return_type = "";
        if (jmmNode.get("type").equals("int")) {
            if (jmmNode.get("is_array").equals("true")) {
                return_type += "array.";
            }
            return_type += "i32";
        }
        else if (jmmNode.get("type").equals("boolean")) {
            return_type += "bool";
        }
        else {
            return_type += jmmNode.get("type");
        }
        return return_type;
    }

    //In case someone creates a void class instance (?)
    private String getOllirTypeNotVoid(Type type) {
        String name = type.getName();

        String ollirType;
        if (name.equals("int")) {
            ollirType = "i32";
        }
        else if (name.equals("boolean")) {
            ollirType = "bool";
        }
        else {
            ollirType = name;
        }

        return (type.isArray() ? "array." : "") + ollirType;
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
        else if (name.equals("boolean")) {
            ollirType = "bool";
        }
        else {
            ollirType = name;
        }

        return (type.isArray() ? "array." : "") + ollirType;
    }

    private OllirData getOrPutFieldCode(JmmNode identifierNode, String methodId, String varType){
        String varName = identifierNode.get("name");
        String getOrPutFieldString = "";
        String return_var = "";

        //Is it a class field?
        if(NodeFindingMethods.isClassField(symbolTable.getMethod(methodId), symbolTable, varName)){
            if(isSet(identifierNode)){
                //putfield can't be done here since it requires things to be placed after operations
                // eg: a = 2 + 2            aux1 = 2 + 2; putfield(a)
                return new OllirData("", "");
            }
            else{
                //getfield
                String auxVarName = "aux" + localVariableCounter++ + "." + varType;
                getOrPutFieldString = auxVarName + " :=." + varType + " getfield(this, " + varName + "." + varType + ")." + varType + ";\n";
                return_var = auxVarName;
                return new OllirData(return_var, getOrPutFieldString);
            }
        }

        return null;
    }

    private boolean isSet(JmmNode node){
        if(node.getParent().getKind().equals("Assignment")){
            if(node == node.getParent().getChildren().get(0)) {
                return true;
            }
        }
        else if(node.getParent().getKind().equals("Index")){
            if(node.getParent() == node.getParent().getParent().getChildren().get(0)){
                return true;
            }
        }
        return false;
    }

}
