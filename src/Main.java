import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;

import java.util.ArrayList;
import java.io.StringReader;
import java.util.regex.Pattern;

public class Main implements JmmParser {

    public JmmParserResult parse(String jmmCode) {
        JMM jmm = new JMM(new StringReader(jmmCode));
        try {
            SimpleNode root = jmm.Program(); // returns reference to root node

            return new JmmParserResult(root, jmm.getReports());
        }

        catch (ParseException e) {
            return new JmmParserResult(null, jmm.getReports());
        }
    }

    public static void main(String[] args) {

    }
}