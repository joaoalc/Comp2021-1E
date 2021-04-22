package table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MySymbolTable implements SymbolTable {
    private final List<String> imports = new ArrayList<>();
    private String class_name;
    private String super_class_name = null;
    private HashMap<Symbol, Object> fields;
    private HashMap<String, Method> methods;
    private final HashMap<Symbol, Object> localVariables = new HashMap<>();

    public void addImport(String importName) {
        imports.add(importName);
    }

    public void setClassName(String class_name) {
        this.class_name = class_name;
    }

    public void setSuperClassName(String super_class_name) {
        this.super_class_name = super_class_name;
    }

    public void addField(Type type, String name, Object value) {
        fields.put(new Symbol(type, name), value);
    }

    public void addMethod(Type return_type, String name, List<Symbol> parameters) {
        Method method = new Method(name, return_type, parameters);

        methods.put(method.getIdentifier(), method);
    }

    public void addLocalVariable(Type type, String name, Object value) {
        localVariables.put(new Symbol(type, name), value);
    }

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
        return new ArrayList<>(localVariables.keySet());
    }
}
