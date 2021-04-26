package table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class ValueSymbol extends Symbol {
    boolean hasValue;

    public ValueSymbol(Type type, String name) {
        super(type, name);
        hasValue = false;
    }

    public ValueSymbol(Type type, String name, boolean value) {
        super(type, name);
        this.hasValue = value;
    }

    public boolean hasValue(){
        return hasValue;
    }

    public void setHasValue(boolean hasValue){
        this.hasValue = hasValue;
    }

    @Override
    public String toString() {
        return "Symbol [type=" + type + ", name=" + name + "]";
    }

    /*
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((hasValue == false) ? 0 : hasValue.hashCode());
        return result;
    }*/

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Symbol other = (Symbol) obj;
        if (name == null) {
            if (other.getName() != null)
                return false;
        }
        else if (!name.equals(other.getName()))
            return false;
        if (type == null) {
            if (other.getType() != null)
                return false;
        }
        else if (!type.equals(other.getType()))
            return false;
        return true;
    }

}
