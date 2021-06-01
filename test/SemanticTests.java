import org.junit.Test;
import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.specs.util.SpecsIo;

import java.util.List;

import static pt.up.fe.comp.TestUtils.mustFail;

public class    SemanticTests {
    private void TestTemplate(String filename) {
        String code = SpecsIo.getResource(String.format("fixtures/public/%s", filename));
        JmmSemanticsResult result = TestUtils.analyse(code);
        System.out.println(result.getRootNode().toJson());
    }

    private void failureTest(String filename) {
        String fileContents = SpecsIo.getResource(String.format("fixtures/public/fail/semantic/%s", filename));
        JmmSemanticsResult result = TestUtils.analyse(fileContents);
        List<Report> reports = result.getReports();
        mustFail(reports);
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

    @Test
    public void IfTest() {
        TestTemplate("If.jmm");
    }

    //Failure tests

    @Test
    public void ArrIndexNotInt() {
        failureTest("arr_index_not_int.jmm");
    }

    @Test
    public void ArrSizeNotInt() {
        failureTest("arr_size_not_int.jmm");
    }

    @Test
    public void BadArguments() {
        failureTest("badArguments.jmm");
    }

    @Test
    public void BinopIncomp() {
        failureTest("binop_incomp.jmm");
    }

    @Test
    public void FuncNotFound() {
        failureTest("funcNotFound.jmm");
    }

    @Test
    public void SimpleLength() {
        failureTest("simple_length.jmm");
    }

    @Test
    public void VarExpIncomp() {
        failureTest("var_exp_incomp.jmm");
    }

    @Test
    public void VarLitIncomp() {
        failureTest("var_lit_incomp.jmm");
    }

    @Test
    public void VarUndef() {
        failureTest("var_undef.jmm");
    }
}
