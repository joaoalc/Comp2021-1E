package utils;

import pt.up.fe.comp.jmm.JmmNode;

public class OllirUtils {

    public static boolean IsEndOfLine(JmmNode currentNode){
        switch(currentNode.getParent().getKind()) {
            case "IfStatement":
                //If it's in the if condition
                /*if(currentNode.getParent().getChildren().get(0) == currentNode){
                    return false;
                }*/
                return true;
            /*case "Add":
            case "Sub":
            case "Mult":
            case "Div":
            case "And":
            case "LessThan":
                return false;*/
            //otherwise. Example: a<b has to be end of line because it's not inside the condition
                /*
                if(true){
                    a < b;
                }
                */
            default:
                return true;
        }
    }
}
