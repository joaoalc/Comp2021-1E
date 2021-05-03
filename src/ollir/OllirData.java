package ollir;

public class OllirData {

    private final String returnVar;
    private final String ollirCode;

    public OllirData(String returnVar, String ollirCode) {
        this.returnVar = returnVar;
        this.ollirCode = ollirCode;
    }

    public OllirData(String ollirCode) {
        this(null, ollirCode);
    }

    public boolean hasReturnVar() {
        return this.returnVar != null;
    }

    public String getReturnVar() {
        return returnVar;
    }

    public String getOllirCode() {
        return ollirCode;
    }

}
