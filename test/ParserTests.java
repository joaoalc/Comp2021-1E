import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class ParserTests {
    public void TestTemplate(String filename) {
        String fileContents = SpecsIo.getResource(String.format("fixtures/public/%s", filename));
        System.out.println(TestUtils.parse(fileContents).getRootNode().getKind());
    }

    @Test
	public void FindMaximumTest() {
        TestTemplate("FindMaximum.jmm");
    }

    @Test
    public void HelloWorldTest() {
        TestTemplate("HelloWorld.jmm");
    }

    @Test
    public void LazysortTest() {
        TestTemplate("Lazysort.jmm");
    }

    @Test
    public void LifeTest() {
        TestTemplate("Life.jmm");
    }

    @Test
    public void MonteCarloPiTest() {
        TestTemplate("MonteCarloPi.jmm");
    }

    @Test
    public void QuicksortTest() {
        TestTemplate("Quicksort.jmm");
    }

    @Test
    public void SimpleTest() {
        TestTemplate("Simple.jmm");
    }

    @Test
    public void TicTacToeTest() {
        TestTemplate("TicTacToe.jmm");
    }

    @Test
    public void WhileAndIFTest() {
        TestTemplate("WhileAndIF.jmm");
    }

    // Fail tests

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
