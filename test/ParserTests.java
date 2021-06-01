import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsIo;

import java.util.List;

import static pt.up.fe.comp.TestUtils.mustFail;

public class ParserTests {
    private void sucessfullTest(String filename) {
        String fileContents = SpecsIo.getResource(String.format("fixtures/public/%s", filename));
        JmmParserResult result = TestUtils.parse(fileContents);
        System.out.println(result.getRootNode().toJson());
    }

    private void failureTest(String filename) {
        String fileContents = SpecsIo.getResource(String.format("fixtures/public/fail/syntactical/%s", filename));
        JmmParserResult result = TestUtils.parse(fileContents);
        List<Report> reports = result.getReports();
        mustFail(reports);
    }

    @Test
	public void FindMaximumTest() {
        sucessfullTest("FindMaximum.jmm");
    }

    @Test
    public void testFunctionIndexing() {
        sucessfullTest("Optimisation.jmm");
    }

    @Test
    public void HelloWorldTest() {
        sucessfullTest("HelloWorld.jmm");
    }

    @Test
    public void LazysortTest() {
        sucessfullTest("Lazysort.jmm");
    }

    @Test
    public void LifeTest() {
        sucessfullTest("Life.jmm");
    }

    @Test
    public void MonteCarloPiTest() {
        sucessfullTest("MonteCarloPi.jmm");
    }

    @Test
    public void QuicksortTest() {
        sucessfullTest("Quicksort.jmm");
    }

    @Test
    public void SimpleTest() {
        sucessfullTest("Simple.jmm");
    }

    @Test
    public void TicTacToeTest() {
        sucessfullTest("TicTacToe.jmm");
    }

    @Test
    public void WhileAndIFTest() {
        sucessfullTest("WhileAndIF.jmm");
    }

    @Test
    public void IFTest() {
        sucessfullTest("If.jmm");
    }

    // Fail Tests

    @Test
    public void BlowUpTest() {
        failureTest("BlowUp.jmm");
    }

    @Test
    public void CompleteWhileTest() {
        failureTest("CompleteWhileTest.jmm");
    }

    @Test
    public void LengthErrorTest() {
        failureTest("LengthError.jmm");
    }

    @Test
    public void MissingRightParTest() {
        failureTest("MissingRightPar.jmm");
    }

    @Test
    public void MultipleSequentialTest() {
        failureTest("MultipleSequential.jmm");
    }

    @Test
    public void NestedLoopTest() {
        failureTest("NestedLoop.jmm");
    }

    // Custom tests

    @Test
    public void BinaryTest() {
        sucessfullTest("Binary.jmm");
    }

    @Test
    public void TransposeTest() {
        sucessfullTest("Transpose.jmm");
    }
}
