import java.util.HashMap;

public class SymbolTable {
    private static HashMap<String, String> symbols = new HashMap<>();

    public static void setValue(String key, String value) {
        symbols.put(key, value);
    }

    public static String getValue(String key) {
        return symbols.get(key);
    }
}