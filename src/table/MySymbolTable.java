package table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySymbolTable implements SymbolTable {
    private final List<String> imports = new ArrayList<>();
    private String class_name;
    private String super_class_name;
    private final Map<String, ValueSymbol> fields = new HashMap<>();
    private final Map<String, Method> methods = new HashMap<>();

    public void addImport(String importName) {
        imports.add(importName);
    }

    public void setClassName(String class_name) {
        this.class_name = class_name;
    }

    public void setSuperClassName(String super_class_name) {
        this.super_class_name = super_class_name;
    }

    public void addField(Type type, String name, boolean value) {
        fields.put(name, new ValueSymbol(type, name, value));
    }

    public boolean fieldExists(String var_name){
        return fields.getOrDefault(var_name, null) != null;
    }

    public boolean fieldExists(Symbol symbol){
        return fields.getOrDefault(symbol.getName(), null) != null;
    }

    public void addMethod(Type return_type, String name, List<ValueSymbol> parameters) {
        Method method = new Method(name, return_type, parameters);
        methods.put(method.getIdentifier(), method);
    }

    public void addField(Type type, String name) {
        fields.put(name, new ValueSymbol(type, name));
    }

    public Method getMethod(Method method){
        return methods.getOrDefault(method.getIdentifier(), null);
    }

    public Method getMethod(String methodId) { return methods.get(methodId);}

    public boolean methodExists(String methodName, List<ValueSymbol> methodArgs){
        Method tmpMethod = new Method(methodName, new Type("", false), methodArgs);
        String methodIdentifier = tmpMethod.getIdentifier();

        return methods.getOrDefault(methodIdentifier, null) != null;
    }

    public Method getMethod(String methodName, List<ValueSymbol> methodArgs){
        Method tmpMethod = new Method(methodName, new Type("", false), methodArgs);
        String methodIdentifier = tmpMethod.getIdentifier();
        return methods.getOrDefault(methodIdentifier, null);
    }

    public boolean importExists(String importName){
        for(String i: imports){
            if(importName.equals(i)){
                return true;
            }
        }
        return false;
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
        return new ArrayList<>(fields.values());
    }

    public ValueSymbol getField(String varName) { return fields.getOrDefault(varName, null);}

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
        //List<Symbol> parameters = (List<Symbol>)methods.get(methodName).getParameters();
        return new ArrayList<>();
    }

    public List<ValueSymbol> getParams(String methodName) {
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
