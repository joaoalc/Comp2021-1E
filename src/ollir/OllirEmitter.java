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

    private int identCounter = 0;

    private String[] keywords = {"array", "ret", "field"};

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
        String ollir_code = "";
        for (int i = 0; i < node.getChildren().size(); i++) {
            ollir_code += visit(node.getChildren().get(i), s).getOllirCode();
        }
        return new OllirData("", ollir_code);
    }

    private OllirData generateStatement2(JmmNode node, String s) {
        String ollir_code = "";
        for (int i = 0; i < node.getChildren().size(); i++) {
            ollir_code += visit(node.getChildren().get(i), s).getOllirCode();
        }
        return new OllirData("", ollir_code);
    }

    private OllirData generateIndex(JmmNode node, String s) {
        String return_type = "";
        String ollir_code = "";
        JmmNode arrayNode = node.getChildren().get(0);
        OllirData arrayData = visit(arrayNode, s);
        JmmNode indexNode = node.getChildren().get(1);
        OllirData indexData = visit(indexNode, s);
        ollir_code += arrayData.getOllirCode();
        ollir_code += indexData.getOllirCode();

        JmmNode parent_node = node.getParent();

        String nodename = node.get("name");

        //For getfield
        if (arrayNode.getOptional("ollir_name").isPresent()) {
            nodename = arrayNode.get("ollir_name");
        }

        //For putfield
        if (arrayNode.getOptional("putfield_required").isPresent()) {
            if (arrayNode.get("putfield_required").equals("true")) {
                node.put("putfield_required", "true");
                nodename = arrayNode.get("ollir_name");
            }
        }

        //TODO: Refactoring: Put this inside the getVarAssignmentName function, somehow
        String varname = filter_keywords(nodename);
        
        if (parent_node.getKind().equals("Assignment")) {
            if (indexNode.getKind().equals("Integer")) {
                ollir_code += "    ".repeat(this.identCounter) + "aux" + localVariableCounter + ".i32 :=.i32 " + indexData.getReturnVar() + ";\n";
                return_type = varname + "[" + "aux" + localVariableCounter++ + ".i32" + "]." + getOllirType(new Type(arrayNode.get("type"), false));
            }
            else {
                return_type = varname + "[" + indexData.getReturnVar() + "]." + getOllirType(new Type(arrayNode.get("type"), false));
            }
        }
        
        else {
            if (indexNode.getKind().equals("Integer")) {
                return_type = "aux" + localVariableCounter++ + ".i32";
                ollir_code += "    ".repeat(this.identCounter) + "aux" + localVariableCounter + ".i32 :=.i32 " + indexData.getReturnVar() + ";\n";
                ollir_code += "    ".repeat(this.identCounter) + return_type + " :=." + getVarOllirType(node) + " " + varname + "[" + "aux" + localVariableCounter++ + ".i32" + "]." + getVarOllirType(node) + ";\n";
            }
            
            else {
                return_type = "aux" + localVariableCounter++ + ".i32";
                ollir_code += "    ".repeat(this.identCounter) + return_type + " :=." + getVarOllirType(node) + " " + varname + "[" + indexData.getReturnVar() + "]." + getVarOllirType(node) + ";\n";
            }
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
        this.identCounter++;
        String trueString = visit(trueNode, s).getOllirCode();
        JmmNode elseNode = node.getChildren().get(2);
        String elseString = visit(elseNode, s).getOllirCode();

        this.identCounter--;
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
                conditionString += "1.bool" + " &&.bool " + generateFalse(conditionNode, s).getReturnVar();
                break;
            case "False":
                conditionString += "1.bool" + " &&.bool " + generateTrue(conditionNode, s).getReturnVar();
                break;
            case "Negate":
                OllirData nodeNegate = visit(conditionNode.getChildren().get(0), s);
                ollir_code += nodeNegate.getOllirCode();

                conditionString += "1.bool" + " &&.bool " + nodeNegate.getReturnVar();
                break;
            case "Identifier":
                OllirData nodeIdentifier = visit(conditionNode, s);
                ollir_code += nodeIdentifier.getOllirCode();
                ollir_code += "    ".repeat(this.identCounter) + "aux" + localVariableCounter + ".bool :=.bool " + nodeIdentifier.getReturnVar() + " !.bool " + nodeIdentifier.getReturnVar() + ";\n";
                conditionString += "1.bool" + " &&.bool " + "aux" + localVariableCounter++ + ".bool";
                break;
            case "FCall":
                OllirData nodeFCall = visit(conditionNode, s);
                ollir_code += nodeFCall.getOllirCode();
                ollir_code += "    ".repeat(this.identCounter) + "aux" + localVariableCounter + ".bool" + " :=.bool " + nodeFCall.getReturnVar() + " !.bool " + nodeFCall.getReturnVar() + ";\n";
                conditionString += "1.bool" + " &&.bool " + "aux" + localVariableCounter++ + ".bool";
                break;
            default:
                System.out.println("This condition of the if statement isn't done yet.");
                System.out.println(conditionNode.getKind());
                break;
        }

        ollir_code += "    ".repeat(this.identCounter) + "if(" + conditionString + ")" + "goto else" + labelCounter + ";\n"
            + trueString +
            "\n" + "    ".repeat(this.identCounter) + "goto endif" + labelCounter + ";\n"
            + "    ".repeat(this.identCounter) + "else" + labelCounter + ":\n" + elseString
            + "    ".repeat(this.identCounter) + "endif" + labelCounter + ":\n";

        labelCounter++;

        ollir_code += "    ".repeat(this.identCounter) + "auxvar.i32 :=.i32 0.i32;\n";

        return new OllirData(return_type, ollir_code);
    }

    private OllirData generateWhile(JmmNode node, String s) {
        String return_type = "";
        String ollir_code = "";

        ollir_code += "    ".repeat(this.identCounter) + "Loop" + labelCounter + ":\n";

        this.identCounter++;
        JmmNode conditionNode = node.getChildren().get(0);
        //JmmNode trueNode = node.getChildren().get(1);
        //String trueString = visit(trueNode, s).getOllirCode();

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
                conditionString += "1.bool" + " &&.bool " + generateTrue(conditionNode, s).getReturnVar();
                break;
            case "False":
                conditionString += "1.bool" + " &&.bool " + generateFalse(conditionNode, s).getReturnVar();
                break;
            case "Negate":
                OllirData nodeNegate = visit(conditionNode.getChildren().get(0), s);
                ollir_code += nodeNegate.getOllirCode();
                ollir_code += "    ".repeat(this.identCounter) + "aux" + localVariableCounter + ".bool" + " :=.bool " + nodeNegate.getReturnVar() + " !.bool " + nodeNegate.getReturnVar() + ";\n";
                conditionString += "1.bool" + " &&.bool " + "aux" + localVariableCounter++ + ".bool";
                break;
            case "Identifier":
                OllirData nodeIdentifier = visit(conditionNode, s);
                ollir_code += nodeIdentifier.getOllirCode();
                conditionString += "1.bool" + " &&.bool " + nodeIdentifier.getReturnVar();
                break;
            case "FCall":
                OllirData nodeFCall = visit(conditionNode, s);
                ollir_code += nodeFCall.getOllirCode();
                ollir_code += "    ".repeat(this.identCounter) + "aux" + localVariableCounter + ".bool" + " :=.bool " + nodeFCall.getReturnVar() + ";\n";
                conditionString += "1.bool" + " &&.bool " + "aux" + localVariableCounter++ + ".bool";
                break;
            default:
                System.out.println("This condition of the if statement isn't done yet.");
                break;
        }

        int auxLabel = labelCounter;
        labelCounter++;

        this.identCounter++;
        String code_ollir = "";
        for (int i = 1; i < node.getChildren().size(); i++) {
            JmmNode trueNode = node.getChildren().get(i);
            String trueString = visit(trueNode, s).getOllirCode();
            code_ollir += trueString;
        }
        this.identCounter--;
        String aux_condition = "aux" + localVariableCounter++ + ".bool";
        ollir_code += "    ".repeat(this.identCounter) + aux_condition + " :=.bool " + conditionString + ";\n";
        ollir_code += "    ".repeat(this.identCounter) + "if(" + aux_condition + "!.bool " + aux_condition + ") goto EndLoop" + auxLabel + ";\n";
        ollir_code += code_ollir;
        /*for(int i = 1; i < node.getChildren().size(); i++){
            JmmNode trueNode = node.getChildren().get(i);
            String trueString = visit(trueNode, s).getOllirCode();
            ollir_code += trueString;
        }*/
        //JmmNode trueNode = node.getChildren().get(1);
        //String trueString = visit(trueNode, s).getOllirCode();

        ollir_code += "    ".repeat(this.identCounter) + "if(" + conditionString + ") goto Loop" + auxLabel + ";\n";
        this.identCounter--;
        ollir_code += "    ".repeat(this.identCounter) + "EndLoop" + auxLabel + ":\n";


        //if(isLastThingInMain(node)){
        ollir_code += "    ".repeat(this.identCounter + 1) + "auxvar.i32 :=.i32 0.i32;\n";
        //}
        //   trueString + "\nif(" + conditionString + ")" + "goto Loop" + labelCounter + ";\n" + "else" + labelCounter + ":\n"
        return new OllirData(return_type, ollir_code);
    }

    private OllirData generateAssignment(JmmNode node, String methodId) {
        JmmNode identifierNode = node.getChildren().get(0);
        JmmNode valueNode = node.getChildren().get(1);

        String return_type;
        String ollir_code = "";
        OllirData identifierData = visit(identifierNode, methodId);
        return_type = identifierData.getReturnVar();

        ollir_code += identifierData.getOllirCode();

        OllirData data = visit(valueNode, methodId);
        //if (data.getOllirCode().equals("")) {
        //For things like integers or identifiers (a = 2; a = b)
        //if (valueNode.getOptional("type").isPresent()) {
        //TODO: Is this a class field?
        boolean isfield = false;
        //if(valueNode.get("is_array").equals("false")){
        ollir_code += data.getOllirCode();
        String varname = filter_keywords(identifierNode.get("name"));
        if (identifierNode.getOptional("putfield_required").isPresent()) {
            if (identifierNode.get("putfield_required").equals("true")) {
                isfield = true;
                if (identifierNode.getKind().equals("Index")) {
                    ollir_code += "    ".repeat(this.identCounter) + identifierData.getReturnVar() + " :=." + getVarOllirType(identifierNode) + " " + data.getReturnVar() + ";\n";
                    ollir_code += "    ".repeat(this.identCounter) + "putfield(this, " + varname + "." + getOllirTypeNotVoid(new Type(identifierNode.get("type"), true)) + ", " + identifierNode.getChildren().get(0).get("ollir_var") + ")." + getVarOllirType(identifierNode) + ";\n";
                }
                else if (valueNode.getKind().equals("NewExpression")) {
                    if (identifierNode.get("is_array").equals("true")) {
                        ollir_code += "    ".repeat(this.identCounter) + identifierData.getReturnVar() + " :=." + getVarOllirType(identifierNode) + " " + data.getReturnVar() + ";\n";
                        ollir_code += "    ".repeat(this.identCounter) + "putfield(this, " + varname + "." + getVarOllirType(identifierNode) + ", " + identifierData.getReturnVar() + ")." + getVarOllirType(identifierNode) + ";\n";
                    }
                }
                else {
                    ollir_code += "    ".repeat(this.identCounter) + "putfield(this, " + varname + "." + getVarOllirType(identifierNode) + ", " + data.getReturnVar() + ")." + getVarOllirType(identifierNode) + ";\n";
                }
            }
        }
        if (!isfield) {
            ollir_code += "    ".repeat(this.identCounter) + identifierData.getReturnVar() + " :=." + getVarOllirType(identifierNode) + " " + data.getReturnVar() + ";\n";
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
        ollirCode += "    ".repeat(this.identCounter) + name + ".bool :=.bool " + Op.getReturnVar() + " !.bool " + Op.getReturnVar();

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
        ollirCode += "    ".repeat(this.identCounter) + name + ".bool :=.bool " + firstOp.getReturnVar() + " <.i32 " + secondOp.getReturnVar();

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
        ollirCode += "    ".repeat(this.identCounter) + name + ".i32 :=.i32 " + firstOp.getReturnVar() + " /.i32 " + secondOp.getReturnVar();

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
        ollirCode += "    ".repeat(this.identCounter) + name + ".i32 :=.i32 " + firstOp.getReturnVar() + " *.i32 " + secondOp.getReturnVar();

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
        ollirCode += "    ".repeat(this.identCounter) + name + ".i32 :=.i32 " + firstOp.getReturnVar() + " -.i32 " + secondOp.getReturnVar();

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
        ollirCode += "    ".repeat(this.identCounter) + name + ".i32" + " :=.i32 " + firstOp.getReturnVar() + " +.i32 " + secondOp.getReturnVar();

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
        ollirCode += "    ".repeat(this.identCounter) + name + ".bool :=.bool " + firstOp.getReturnVar() + " &&.bool " + secondOp.getReturnVar();

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
        ollirCode += "    ".repeat(this.identCounter) + name + ".bool :=.bool " + Op.getReturnVar() + " !.bool " + Op.getReturnVar();

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

        boolean isSet = putFieldCodeString(jmmNode, s, return_type);

        String varname = jmmNode.get("name");

        varname = this.filter_keywords(varname);

        //Not a class field
        if (fieldData == null) {
            return new OllirData(varname + "." + return_type, "");
        }
        if (isSet) {
            //putfield
            jmmNode.put("putfield_required", "true");
            if (fieldData == null) {
                return new OllirData(varname + "." + return_type, "");
            }
        }
        jmmNode.put("ollir_var", fieldData.getReturnVar());
        jmmNode.put("ollir_name", fieldData.getReturnVar().substring(0, fieldData.getReturnVar().indexOf('.')));
        return fieldData;

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
            ollirCode += "    ".repeat(this.identCounter) + "import " + importString + ";\n";
        }

        String className = symbolTable.getClassName();
        String superClass = symbolTable.getSuper();

        ollirCode += "    ".repeat(this.identCounter) + className + " " +
            (superClass != null ? ("extends " + superClass) : "") + " {\n";

        this.identCounter++;

        List<Symbol> classFields = symbolTable.getFields();

        for (Symbol field : classFields) {
            Type fieldType = field.getType();
            String fieldName = filter_keywords(field.getName());

            ollirCode += "    ".repeat(this.identCounter) + ".field " + fieldName + "." + getOllirType(fieldType) + ";\n";
        }

        ollirCode += "    ".repeat(this.identCounter) + ".construct " + className + "().V {\n" +
            "    ".repeat(this.identCounter + 1) + "invokespecial(this, \"<init>\").V;\n" +
            "    ".repeat(this.identCounter) + "}\n";


        for (JmmNode child : node.getChildren()) {
            OllirData childData = visit(child, null);
            ollirCode += childData.getOllirCode();
        }

        ollirCode += "}";
        this.identCounter--;

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
        stringBuilder.append("    ".repeat(this.identCounter) + ".method public " + (methodName.equals("main") ? "static " : "") + methodName + "(");
        List<String> parametersOllir = new ArrayList<>();
        for (Symbol parameter : parameters) {
            final Type parameterType = parameter.getType();

            parametersOllir.add(parameter.getName() + "." + getOllirType(parameterType));
        }
        stringBuilder.append(String.join(", ", parametersOllir) + ")." + getOllirType(returnType) + " {\n");
        this.identCounter++;

        for (JmmNode child : node.getChildren()) {
            OllirData childData = visit(child, methodId);
            ollirCode += childData.getOllirCode();
        }
        this.identCounter--;

        return new OllirData(stringBuilder.toString() + ollirCode + "    ".repeat(this.identCounter) + "}\n");
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

        String ollirCode = expression.getOllirCode();
        ollirCode += "    ".repeat(this.identCounter) + "ret." + getOllirType(symbolTable.getReturnType(methodId));

        ollirCode += " " + expression.getReturnVar() + ";\n";

        return new OllirData(ollirCode);
    }

    private OllirData generateNewExpression(JmmNode node, String methodId) {
        OllirData data = visit(node.getChildren().get(0), methodId);

        return data;
    }

    private OllirData generateVarCreation(JmmNode node, String methodId) {
        String return_type = getVarOllirType(node);
        String aux = "aux" + localVariableCounter++;
        String ollir_code = "    ".repeat(this.identCounter) + aux + "." + return_type + " :=." + return_type + " new(" + return_type + ")." + return_type + ";\n";
        ollir_code += "    ".repeat(this.identCounter) + "invokespecial(" + aux + "." + return_type + ",\"<init>\").V;\n";

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
                ollir_code += childData.getOllirCode();
                //ollirCode += childData.getOllirCode();
                args.add(childData.getReturnVar());
                if (child.getKind().equals("Integer")) {
                    types.add(new ValueSymbol(new Type("int", false), "", true));
                }
                else if (child.getKind().equals("True") || child.getKind().equals("False")) {
                    types.add(new ValueSymbol(new Type("boolean", false), "", true));
                }
                else if (child.getOptional("type").isPresent()) {
                    //ollir_code += childData.getOllirCode();
                    types.add(new ValueSymbol(new Type(child.get("type"), Boolean.parseBoolean(child.get("is_array"))), "", true));
                }
                else if (child.getOptional("name").isPresent()) {
                    //TODO: Add value or operation possibility to this
                    types.add(NodeFindingMethods.getVariable(symbolTable.getMethod(methodId), symbolTable, child.get("name")));
                }
            }


            if (identifier_node.getKind().equals("This")) {
                Method method = symbolTable.getMethod(function_name, types);
                if (method != null) {
                    return_var = "aux" + localVariableCounter++ + "." + getOllirType(method.getReturnType());
                    ollir_code += "    ".repeat(this.identCounter) + return_var + " :=." + getOllirType(method.getReturnType()) + " invokevirtual(this, \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + getOllirType(method.getReturnType()) + ";";
                }
                else {
                    //Calling a superclass' method (arguments' ollir code is already written)
                    String type = getFunctionTypeIfNonExistant(node);
                    if (!type.equals("V")) {
                        return_var = getVarAssignmentName(node) + "." + type;
                        ollir_code += "    ".repeat(this.identCounter) + return_var + " :=." + type + " invokevirtual(this, \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
                    }
                    else {
                        ollir_code += "    ".repeat(this.identCounter) + "invokevirtual(this, \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
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
                            return_var = "aux" + localVariableCounter++ + "." + type;
                            ollir_code += "    ".repeat(this.identCounter) + return_var + " :=." + type + " invokestatic(" + identifier_name + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
                        }
                        else {
                            return_var = "";
                            ollir_code += "    ".repeat(this.identCounter) + "invokestatic(" + identifier_name + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ").V;";
                        }

                    }
                    else {
                        //Variable
                        if (symbol.getType().getName().equals(symbolTable.getClassName()) && symbolTable.getSuper() == null) {
                            Method method = symbolTable.getMethod(function_name, types);
                            ValueSymbol identifier = NodeFindingMethods.getVariable(symbolTable.getMethod(methodId), symbolTable, identifier_name);
                            return_var = "aux" + localVariableCounter++ + "." + getOllirType(method.getReturnType());
                            ollir_code += "    ".repeat(this.identCounter) + return_var + " :=." + getOllirType(method.getReturnType()) + " invokevirtual(" + identifier_name + "." + getOllirType(identifier.getType()) + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + getOllirType(method.getReturnType()) + ";";

                        }
                        else if (symbolTable.getSuper() != null) {
                            //TODO
                            Method method = symbolTable.getMethod(function_name, types);
                            ValueSymbol identifier = NodeFindingMethods.getVariable(symbolTable.getMethod(methodId), symbolTable, identifier_name);
                            if (method != null) {
                                return_var = "aux" + localVariableCounter++ + "." + getOllirType(method.getReturnType());
                                ollir_code += "    ".repeat(this.identCounter) + return_var + " :=." + getOllirType(method.getReturnType()) + " invokevirtual(" + identifier_name + "." + getOllirType(identifier.getType()) + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + getOllirType(method.getReturnType()) + ";";
                            }
                            else {
                                String type = getFunctionTypeIfNonExistant(node);
                                if (!type.equals("V")) {
                                    return_var = "aux" + localVariableCounter++ + "." + type;
                                    ollir_code += "    ".repeat(this.identCounter) + return_var + " :=." + type + " invokevirtual(" + identifier_name + "." + getOllirType(identifier.getType()) + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
                                }
                                else {
                                    return_var = "";
                                    ollir_code += "    ".repeat(this.identCounter) + "invokevirtual(" + identifier_name + "." + getOllirType(identifier.getType()) + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
                                }
                            }
                        }

                    }
                }
                else {
                    OllirData data = visit(identifier_node, methodId);
                    ollir_code += data.getOllirCode();
                    //This happens when an object is created and one of it's functions are immediately called. Eg: pi_estimate_times_100 = new MonteCarloPi().estimatePi100(num_samples); in the MonteCarloPi test
                    //TODO: Check if it's its own class and act accordingly
                    String type = getFunctionTypeIfNonExistant(node);
                    if (!type.equals("V")) {
                        return_var = "aux" + localVariableCounter++ + "." + type;
                        ollir_code += "    ".repeat(this.identCounter) + return_var + " :=." + type + " invokevirtual(" + data.getReturnVar() + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
                    }
                    else {
                        return_var = "";
                        ollir_code += "    ".repeat(this.identCounter) + "invokevirtual(" + data.getReturnVar() + ", \"" + function_name + "\"" + (args.isEmpty() ? "" : ", " + String.join(", ", args)) + ")." + type + ";";
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
                ollir_code += "    ".repeat(this.identCounter) + "aux" + localVariableCounter + ".i32 :=.i32 " + "arraylength(" + idData.getReturnVar() + ").i32" + ";\n";
                return_var = "aux" + localVariableCounter++ + ".i32";
            }
            //return_var = "arraylength(" + idData.getReturnVar() + ").i32";
        }

        return new OllirData(return_var, ollir_code);
    }

    public String getVarAssignmentName(JmmNode jmmNode) {
        JmmNode parent_node = jmmNode.getParent();
        String name = "";
        if (parent_node.getChildren().get(0).getKind().equals("Index")) {
            name = "aux" + localVariableCounter++;
        }
        else if (parent_node.getKind().equals("Assignment")) {
            JmmNode identifier_node = parent_node.getChildren().get(0);
            name = identifier_node.get("name");
        }
        else {
            name = "aux" + localVariableCounter++;
        }

        return name;
    }

    private String getFunctionTypeIfNonExistant(JmmNode node) {
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
            Optional<String> opt = node.getOptional("type");
            if (opt.isPresent()) {
                String fcallType = opt.get();
                if (fcallType.isEmpty()) {
                    type = "V";
                } else if (fcallType.equals("int")){
                    type = "i32";
                } else if (fcallType.equals("boolean")){
                    type = "bool";
                } else {
                    type = fcallType;
                }
            } else {
                type = "V";
            }
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

    private OllirData getOrPutFieldCode(JmmNode identifierNode, String methodId, String varType) {
        String varName = identifierNode.get("name");
        String getOrPutFieldString = "";
        String return_var = "";

        //Is it a class field?
        if (NodeFindingMethods.isClassField(symbolTable.getMethod(methodId), symbolTable, varName)) {
            String var = filter_keywords(varName);
            if (isSet(identifierNode)) {
                //putfield can't be done here since it requires things to be placed after operations
                // eg: a = 2 + 2            aux1 = 2 + 2; putfield(a)
                //return new OllirData("", "");
                String auxVarName = "aux" + localVariableCounter++ + "." + varType;
                getOrPutFieldString = "    ".repeat(this.identCounter) + auxVarName + " :=." + varType + " getfield(this, " + var + "." + varType + ")." + varType + ";\n";
                return_var = auxVarName;
                return new OllirData(return_var, getOrPutFieldString);
            }
            else {
                //getfield
                String auxVarName = "aux" + localVariableCounter++ + "." + varType;
                getOrPutFieldString = "    ".repeat(this.identCounter) + auxVarName + " :=." + varType + " getfield(this, " + var + "." + varType + ")." + varType + ";\n";
                return_var = auxVarName;
                return new OllirData(return_var, getOrPutFieldString);
            }
        }

        return null;
    }

    public boolean putFieldCodeString(JmmNode identifierNode, String methodId, String varType) {
        String varName = identifierNode.get("name");
        String getOrPutFieldString = "";
        String return_var = "";

        //Is it a class field?
        if (NodeFindingMethods.isClassField(symbolTable.getMethod(methodId), symbolTable, varName)) {
            if (isSet(identifierNode)) {
                //putfield can't be done here since it requires things to be placed after operations
                // eg: a = 2 + 2            aux1 = 2 + 2; putfield(a)
                return true;
            }
        }
        return false;
    }

    private boolean isSet(JmmNode node) {
        if (node.getParent().getKind().equals("Assignment")) {
            if (node == node.getParent().getChildren().get(0)) {
                return true;
            }
        }
        else if (node.getParent().getKind().equals("Index")) {
            if (node.getParent() == node.getParent().getParent().getChildren().get(0)) {
                return true;
            }
        }
        return false;
    }

    private boolean isLastThingInMain(JmmNode ifNode) {
        if (ifNode.getParent().getKind().equals("MethodBody")) {
            if (ifNode.getParent().getParent().getOptional("name").isPresent()) {
                if (ifNode.getParent().getParent().get("name").equals("main")) {
                    if (ifNode.getParent().getChildren().get(ifNode.getParent().getChildren().size() - 1) == ifNode) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private String filter_keywords(String current_name) {
        String filtered = current_name;
        for (String keyword : this.keywords) {
            if (current_name.startsWith(keyword)) {
                filtered = "var_keyword_" + current_name;
                break;
            }
        }
        return filtered;
    }

}
