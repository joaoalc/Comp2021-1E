import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.ArrayList;
import java.io.StringReader;

public class Main implements JmmParser {
    public JmmParserResult parse(String jmmCode) {
        try {
            JMM jmm = new JMM(new StringReader(jmmCode));
            SimpleNode root = jmm.Program(); // returns reference to root node

            root.dump(""); // prints the tree on the screen

            return new JmmParserResult(root, new ArrayList<Report>());
        }

        catch (ParseException e) {
            throw new RuntimeException("Error while parsing", e);
        }
    }

    public static void main(String[] args) {
        String program =
            "import io;\n" +
            "class Fac {\n" +
            "}";

        Main main = new Main();
        main.parse(program);
    }
}