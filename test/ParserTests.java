import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

public class ParserTests {
    public void TestTemplate(String filename) {
        String fileContents = SpecsIo.getResource(String.format("fixtures/public/%s", filename));
        // System.out.println(TestUtils.parse(fileContents).getRootNode().getKind());
        System.out.println(TestUtils.parse(fileContents).getRootNode().toJson());
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
}
