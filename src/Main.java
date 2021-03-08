
import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;

import java.util.Arrays;
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
        String code = "import io;\n" +
        "class Fac {\n" +
        " public int ComputeFac(int num){\n" +
        " int num_aux ;\n" +
        " if (num < 1)\n" +
        " num_aux = 1;\n" +
        " else\n" +
        " num_aux = num * (this.ComputeFac(num-1));\n" +
        " return num_aux;\n" +
        " }\n" +
        " public static void main(String[] args){\n" +
        " io.println(new Fac().ComputeFac(10)); //assuming the existence\n" +
        " // of the classfile io.class\n" +
        " }\n" +
        "}";

        Main main = new Main();
        main.parse(code);
    }
}