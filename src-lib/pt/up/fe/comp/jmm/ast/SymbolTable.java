package pt.up.fe.comp.jmm.ast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SymbolTable {
    HashMap<String, ArrayList<String>> globalEntries = new HashMap<>(); //Var name, List including var type and attributes

    public int insert(String key, ArrayList<String> value){
        if(globalEntries.get(key) != null){
            globalEntries.put(key, value);
        }
        return -1;
    }

    public List<String> lookup(String symbol){
        List<String> result = globalEntries.get(symbol);
        return result;
    }

}
