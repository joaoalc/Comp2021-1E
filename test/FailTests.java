import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsIo;

import java.util.List;

import static pt.up.fe.comp.TestUtils.mustFail;

public class FailTests {
    public void TestTemplate(String filename) {
        String fileContents = SpecsIo.getResource(String.format("fixtures/public/%s", filename));
        JmmParserResult result = TestUtils.parse(fileContents);
        List<Report> reports = result.getReports();
        mustFail(reports);
    }

    @Test(expected = RuntimeException.class)
    public void BlowUpTest() {
        TestTemplate("fail/syntactical/BlowUp.jmm");
    }

    @Test(expected = RuntimeException.class)
    public void CompleteWhileTest() {
        TestTemplate("fail/syntactical/CompleteWhiteTest.jmm");
    }

    @Test(expected = RuntimeException.class)
    public void LengthErrorTest() {
        TestTemplate("fail/syntactical/LengthError.jmm");
    }

    @Test(expected = RuntimeException.class)
    public void MissingRightParTest() {
        TestTemplate("fail/syntactical/MissingRightPar.jmm");
    }

    @Test(expected = RuntimeException.class)
    public void MultipleSequentialTest() {
        TestTemplate("fail/syntactical/MultipleSequential.jmm");
    }

    @Test(expected = RuntimeException.class)
    public void NestedLoopTest() {
        TestTemplate("fail/syntactical/NestedLoop.jmm");
    }
}
