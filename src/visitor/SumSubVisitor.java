package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.PostorderJmmVisitor;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.MySymbolTable;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SumSubVisitor extends PostorderJmmVisitor<List<Report>, Boolean> {
    MySymbolTable symbolTable;
    public SumSubVisitor(MySymbolTable symbolTable) {
        addVisit("Add", this::verifySum);
        addVisit("Sub", this::verifySubtraction);

        this.symbolTable = symbolTable;

    }

    public boolean verifySum(JmmNode node, List<Report> reports){


        if(node.getOptional("value").isEmpty()){
            JmmNode firstChild = node.getChildren().get(0);
            JmmNode secondChild = node.getChildren().get(1);

            if(firstChild.getOptional("value").isEmpty())
            {
                System.out.println("1st node Doesnt have a value.");
            }
            else if(secondChild.getOptional("value").isEmpty()){

                System.out.println("2nd node Doesnt have a value.");
            }
            else{
                int result = 0;
                if(!Utils.isInteger(firstChild.get("value"))){
                    //TODO: Add report
                }
                if(!Utils.isInteger(secondChild.get("value"))){
                    //TODO: Add report
                }
                result += Integer.parseInt(firstChild.get("value"));
                result += Integer.parseInt(secondChild.get("value"));
                node.put("value", Integer.toString(result));
            }
        }
        else{
            //Node already has value
            System.out.println("Adition node already has a value?");
        }
        return true;
    }

    public boolean verifySubtraction(JmmNode node, List<Report> reports){


        if(node.getOptional("value").isEmpty()){
            JmmNode firstChild = node.getChildren().get(0);
            JmmNode secondChild = node.getChildren().get(1);

            if(firstChild.getOptional("value").isEmpty())
            {
                System.out.println("1st node Doesnt have a value.");
            }
            else if(secondChild.getOptional("value").isEmpty()){

                System.out.println("2nd node Doesnt have a value.");
            }
            else{
                int result = 0;
                if(!Utils.isInteger(firstChild.get("value"))){
                    //TODO: Add report
                }
                if(!Utils.isInteger(secondChild.get("value"))){
                    //TODO: Add report
                }
                result += Integer.parseInt(firstChild.get("value"));
                result -= Integer.parseInt(secondChild.get("value"));
                node.put("value", Integer.toString(result));
            }
        }
        else{
            //Node already has value
            System.out.println("Subtraction node already has a value?");
        }
        return true;
    }

}
