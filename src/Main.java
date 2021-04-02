import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;

import java.util.ArrayList;
import java.io.StringReader;

public class Main implements JmmParser {
    public JmmParserResult parse(String jmmCode) {
        JMM jmm = new JMM(new StringReader(jmmCode));
        try {
            SimpleNode root = jmm.Program(); // returns reference to root node

            root.dump(""); // prints the tree on the screen

            return new JmmParserResult(root, jmm.getReports());
        }

        catch (ParseException e) {
            return new JmmParserResult(null, jmm.getReports());
        }
    }

    public static void main(String[] args) {

    }
}