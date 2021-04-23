package visitor;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import table.MySymbolTable;

import java.util.ArrayList;
import java.util.List;

public class MethodVerifier extends PreorderJmmVisitor<List<Report>, Boolean> {
    MySymbolTable symbolTable;
    public MethodVerifier(MySymbolTable symbolTable) {
        addVisit("Call", this::verifyCall);
        this.symbolTable = symbolTable;

    }

    public boolean verifyCall(JmmNode node, List<Report> reports){


        if(node.getParent().getChildren().size() == 2){

            boolean ownFunction = false;
            if(node.getParent().getChildren().get(0).getKind().compareTo("This") == 0){
                ownFunction = true;
            }

            if(ownFunction) {
                ArrayList<Symbol> symbols = new ArrayList<Symbol>();
                //Get arguments
                for(int i = 2; i < node.getParent().getChildren().size(); i++){
                    String varName = node.getParent().getChildren().get(1).get("name");

                    //TODO: See which function we are in so we can get that method's local variables
                }
                //Is the method in the table?
                if (!symbolTable.methodExists(node.getParent().getChildren().get(1).get("name"), new ArrayList<Symbol>())) {
                    System.out.println("Undeclared method, add report here and stop execution");
                    return false;
                }
            }
            else{
                if(!symbolTable.getImports().contains(node.getParent().getChildren().get(0).get("name"))){
                    System.out.println("Undeclared import, add report here and stop execution");
                    return false;
                }
            }
            System.out.println("Method exists.");


            //TODO: check arguments
            return true;



        }
        else if(node.getParent().getChildren().size() == 2){
        }

        for(JmmNode nod: node.getParent().getChildren()){
            System.out.println(nod.getKind());
        }

        return true;
    }

}
