package table;

import com.sun.jdi.Value;
import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Method {
    private String name;
    private Type return_type;
    private List<ValueSymbol> parameters;
    private final HashMap<String, ValueSymbol> local_variables = new HashMap<>();

    public Method(String name, Type return_type, List<ValueSymbol> parameters) {
        this.name = name;
        this.return_type = return_type;
        this.parameters = parameters;
        for(ValueSymbol param: parameters){
            local_variables.put(param.getName(), param);
        }
    }

    public void addLocalVariable(Type type, String name, boolean value) {
        local_variables.put(name, new ValueSymbol(type, name, value));
    }

    public boolean localVariableExists(ValueSymbol symbol){
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

    public List<ValueSymbol> getParameters() {
        return parameters;
    }

    public List<Symbol> getLocalVariables() {
        return new ArrayList<>(local_variables.values());
    }

    public ValueSymbol getLocalVariable(String varName) {
        return local_variables.getOrDefault(varName, null);}

    public String getIdentifier() {

        List<String> parameter_types = new ArrayList<>();
        for(Symbol i: parameters){
            String parameterId = i.getType().getName() + (i.getType().isArray() ? "[]" : "");
            parameter_types.add(parameterId);
        }

        return String.join("-", name, String.join("-", parameter_types));
    }
}
