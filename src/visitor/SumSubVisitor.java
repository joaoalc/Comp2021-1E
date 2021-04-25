package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.Method;
import table.MySymbolTable;
import table.ValueSymbol;
import utils.NodeFindingMethods;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SumSubVisitor extends PostorderJmmVisitor<List<Report>, Boolean> {
    MySymbolTable symbolTable;
    public SumSubVisitor(MySymbolTable symbolTable) {
        addVisit("Add", this::verifySumSub);
        addVisit("Sub", this::verifySumSub);
        addVisit("Mult", this::verifySumSub);
        addVisit("Div", this::verifySumSub);

        this.symbolTable = symbolTable;

    }

    public boolean verifySumSub(JmmNode node, List<Report> reports){


        if(node.getOptional("value").isEmpty()){
            JmmNode firstChild = node.getChildren().get(0);
            JmmNode secondChild = node.getChildren().get(1);

            if(firstChild.getOptional("type").isEmpty())
            {
                if(firstChild.getKind().compareTo("Identifier") == 0){
                    //Check if identifier is declared
                    Method method = NodeFindingMethods.FindParentMethod(firstChild);
                    method = symbolTable.getMethod(method);
                    Symbol symbol = NodeFindingMethods.getVariable(method, symbolTable, firstChild.get("name"));
                    if(symbol == null){
                        //TODO: Undeclared variable report
                        System.out.println("This variable hasnt been declared");
                        return false;
                    }
                    if(!((ValueSymbol) symbol).hasValue()){
                        //TODO: Uninitialized variable report
                        System.out.println("This variable hasnt been given a value");
                        return false;
                    }

                }
                //TODO: Distinguish between undeclared and uninitialized variable
                //System.out.println("1st node Doesnt have a value.");
            }
            else if(secondChild.getOptional("type").isEmpty()){
                if(secondChild.getKind().compareTo("Identifier") == 0){
                    //Check if identifier is declared
                    Method method = NodeFindingMethods.FindParentMethod(secondChild);
                    Symbol symbol = NodeFindingMethods.getVariable(method, symbolTable, secondChild.get("name"));
                    if(symbol == null){
                        //TODO: Undeclared variable report
                        System.out.println("This variable hasnt been declared");
                        return false;
                    }
                    if(!((ValueSymbol) symbol).hasValue()){
                        //TODO: Uninitialized variable report
                        System.out.println("This variable hasnt been given a value");
                        return false;
                    }

                }
                //TODO: Distinguish between undeclared and uninitialized variable
                //System.out.println("2nd node Doesnt have a value.");
            }
            else{
                if(firstChild.get("type").compareTo("int") != 0 && firstChild.get("is_array").compareTo("false") != 0){


                    //TODO: Add report
                    System.out.println("Second value isn't an integer");
                    return false;
                }
                if(secondChild.get("type").compareTo("int") != 0 && secondChild.get("is_array").compareTo("false") != 0){

                    //TODO: Add report
                    System.out.println("First value isn't an integer");
                    return false;
                }

                node.put("type", "int");
                node.put("is_array", "false");
                System.out.println("Added var");
            }
        }
        else{
            //Node already has value
            System.out.println("Operation node already has a value?");
        }
        return true;
    }


}
