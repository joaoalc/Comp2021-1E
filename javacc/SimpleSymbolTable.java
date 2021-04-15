import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

class SimpleSymbolTable implements SymbolTable {
    private List<String> imports;
    private String class_name;
    private String super_class_name;
    private HashMap<Symbol, ?> fields;
    private HashMap<String, Method> methods;

    /**
     * @return a list of fully qualified names of imports
     */
    @Override
    public List<String> getImports() {
        return imports;
    }

    /**
     * @return the name of the main class
     */
    @Override
    public String getClassName() {
        return class_name;
    }

    /**
     * @return the name that the classes extends, or null if the class does not extend another class
     */
    @Override
    public String getSuper() {
        return super_class_name;
    }

    /**
     * @return a list of Symbols that represent the fields of the class
     */
    @Override
    public List<Symbol> getFields() {
        return new ArrayList<>(fields.keySet());
    }

    /**
     * @return a list with the names of the methods of the class
     */
    @Override
    public List<String> getMethods() {
        return new ArrayList<>(methods.keySet());
    }

    /**
     * @return the return type of the given method
     */
    @Override
    public Type getReturnType(String methodName) {
        return methods.get(methodName).getReturnType();
    }

    /**
     * @param methodName
     * @return a list of parameters of the given method
     */
    @Override
    public List<Symbol> getParameters(String methodName) {
        return methods.get(methodName).getParameters();
    }

    /**
     * @param methodName
     * @return a list of local variables declared in the given method
     */
    @Override
    public List<Symbol> getLocalVariables(String methodName) {
        return null;
    }
}