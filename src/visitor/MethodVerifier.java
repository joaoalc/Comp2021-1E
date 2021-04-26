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
        addVisit("FCall", this::verifyCall);
        this.symbolTable = symbolTable;
    }

    public boolean verifyCall(JmmNode node, List<Report> reports){
        if (node.getChildren().size() == 2){

            boolean ownFunction = node.getChildren().get(0).getKind().equals("This");

            //Check if it's a length call, if it is, the thing calling it has to be an array
            if(node.getChildren().get(1).getKind().equals("Length")){
                //TODO: Check if .length's variable is an array (int or string in case of main)
                if (node.getChildren().get(0).getOptional("is_array").isEmpty() || node.getChildren().get(0).getOptional("type").isEmpty()) {
                    //TODO: Undeclared variable report
                    System.out.println("Undeclared variable");
                    return false;
                }
                else if(!node.getChildren().get(0).getOptional("name").isEmpty()){

                }
                else if(node.getChildren().get(0).get("is_array").equals("true")) {
                    node.put("type", "int");
                    node.put("is_array", "false");
                    return true;
                }
                else{
                    //TODO: Variable isn't an array
                    System.out.println("Variable isn't an array");
                    return false;
                }
            }

            //this.etc()
            if(ownFunction) {
                ArrayList<Symbol> symbols = new ArrayList<>();
                //Get arguments
                //Is the method in the table?

                List<Symbol> arguments = new ArrayList<>();

                //Get function arguments;
                for (int i = 0; i < node.getChildren().get(1).getChildren().size(); i++){
                    // String varName = node.getChildren().get(1).getChildren().get(i).get("name");
                    //System.out.println("Argument number: " + i + " " + varName);
                    //TODO: See which function we are in so we can get that method's local variables
                }

                if (!symbolTable.methodExists(node.getChildren().get(1).get("name"), arguments)) {
                    System.out.println("Undeclared method, add report here and stop execution");
                    return false;
                }
            }

            //<import_name>.etc()
            else{
//                if (!symbolTable.getImports().contains(node.getChildren().get(0).get("name"))){
//                    System.out.println("Undeclared import, add report here and stop execution");
//                    return false;
//                }
            }

            //TODO: check arguments
            return true;
        }
        return true;
    }

}
