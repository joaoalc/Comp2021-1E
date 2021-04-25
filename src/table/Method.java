package table;

import com.sun.jdi.Value;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class Method {
    private String name;
    private Type return_type;
    private List<Symbol> parameters;
    private final HashMap<String, ValueSymbol> local_variables = new HashMap<>();

    public Method(String name, Type return_type, List<Symbol> parameters) {
        this.name = name;
        this.return_type = return_type;
        this.parameters = parameters;
    }

    public void addLocalVariable(Type type, String name, boolean value) {
        local_variables.put(name, new ValueSymbol(type, name, value));
    }

    public boolean localVariableExists(Symbol symbol){
        if(local_variables.getOrDefault(symbol, null) == null){
            return false;
        }
        return true;
    }

    public String getName() {
        return name;
    }

    public Type getReturnType() {
        return return_type;
    }

    public List<Symbol> getParameters() {
        return parameters;
    }

    public List<Symbol> getLocalVariables() {
        return new ArrayList<>(local_variables.values());
    }

    public Symbol getLocalVariable(String varName) {
        return local_variables.getOrDefault(varName, null);}

    public String getIdentifier() {
        List<String> parameter_types = parameters.stream().map(Symbol::getName).collect(Collectors.toList());

        return String.join("-", name, String.join("-", parameter_types));
    }
}
