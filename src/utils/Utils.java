package utils;

import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;


public class Utils {
    public static InputStream toInputStream(String text) {
        try {
            return new ByteArrayInputStream(text.getBytes("UTF-8"));
        }

        catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static List<JmmNode> getChildrenOfKind(JmmNode node, String kind) {
        return node.getChildren()
                .stream()
                .filter(c -> c.getKind().equals(kind))
                .collect(Collectors.toList());
    }

    public static boolean isInteger(String s) {
        if (s.isEmpty())
            return false;

        int i = 0;

        if (s.charAt(0) == '-') {
            if (s.length() == 1)
                return false;

            i = 1;
        }

        for (int loopVar = i; loopVar < s.length(); loopVar++)
            if (!Character.isDigit(s.charAt(i)))
                return false;

        return true;
    }

    public static Report newSemanticReport(JmmNode node, ReportType type, String message) {
        int line = Integer.parseInt(node.get("line"));
        int column = Integer.parseInt(node.get("col"));

        return new Report(type, Stage.SEMANTIC, line, column, message);
    }
}