package table;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;

public class ValueSymbol extends Symbol {
    boolean has_value;

    public ValueSymbol(Type type, String name) {
        super(type, name);

        has_value = false;
    }

    public ValueSymbol(Type type, String name, boolean value) {
        super(type, name);
        this.has_value = value;
    }

    public boolean hasValue(){
        return has_value;
    }

    public void setHas_value(boolean has_value){
        this.has_value = has_value;
    }

    @Override
    public String toString() {
        return "Symbol [type=" + getType() + ", name=" + getName() + "]";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Symbol other = (Symbol) obj;
        if (getName() == null) {
            if (other.getName() != null)
                return false;
        }
        else if (!getName().equals(other.getName()))
            return false;
        if (getType() == null) {
            if (other.getType() != null)
                return false;
        }
        else if (!getType().equals(other.getType()))
            return false;
        return true;
    }

}
