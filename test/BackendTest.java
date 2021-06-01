
/**
 * Copyright 2021 SPeCS.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

public class BackendTest {
    private String runTest(String filename) {
        OllirResult ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/" + filename), true);
        TestUtils.noErrors(ollirResult.getReports());
        JasminResult result = TestUtils.backend(ollirResult);
        TestUtils.noErrors(result.getReports());

        return SpecsStrings.normalizeFileContents(result.run().trim());
    }

    private String runTest(String filename, String input) {
        OllirResult ollirResult = TestUtils.optimize(SpecsIo.getResource("fixtures/" + filename), true);
        TestUtils.noErrors(ollirResult.getReports());
        JasminResult result = TestUtils.backend(ollirResult);
        TestUtils.noErrors(result.getReports());

        return SpecsStrings.normalizeFileContents(result.run(input).trim());
    }

    @Test
    public void FindMaximum() {
        String output = runTest("public/FindMaximum.jmm");
        String expected = "Result: 28";

        assertEquals(expected, output);
    }

    @Test
    public void HelloWorldTest() {
        String output = runTest("public/HelloWorld.jmm");
        String expected = "Hello, World!";

        assertEquals(expected, output);
    }

    @Test
    public void LazysortTest() {
        String output = runTest("public/Lazysort.jmm");

        String expected = "";

        for (int i = 0; i < 10; i++)
            expected += "([0-9]|1[0-1])\n";

        expected = expected.substring(0, expected.length() - 1); // Remove newline in the end

        assertTrue(output.matches(expected));
    }

    @Test

    public void LifeTest() {
        // We include an extra newline at the end of the input to send an empty string
        // This will cause an exception, since it's expecting a number
        // However, it is necessary to stop the infinite loop of the program
        String input = "1\n2\n3\n4\n5\n6\n7\n8\n9\n\n"; // Execute 10 iterations of the program
        String output = runTest("public/Life.jmm", input);

        // Filter the exception text 
        output = output.replaceAll("[a-zA-Z].+", "").trim();

        String expected = SpecsStrings.normalizeFileContents(SpecsIo.getResource("fixtures/public/Life.txt"));

        assertEquals(expected, output);
    }

    @Test
    public void MonteCarloPiTest() {
        String output = runTest("public/MonteCarloPi.jmm", "10000\n");
        String expected = "Insert number: Result: [0-9]{3}";

        assertTrue(output.matches(expected));
    }

    @Test
    public void QuickSortTest() {
        String output = runTest("public/Quicksort.jmm");
        String expected = SpecsStrings.normalizeFileContents(SpecsIo.getResource("fixtures/public/QuickSort.txt"));

        assertEquals(expected, output);
    }

    @Test
    public void SimpleTest() {
        String output = runTest("public/Simple.jmm");
        String expected = "30";

        assertEquals(expected, output);
    }

    @Test
    public void TicTacToeTest() {
        String input = SpecsStrings.normalizeFileContents(SpecsIo.getResource("fixtures/public/TicTacToe.input"));
        String output = SpecsStrings.normalizeFileContents(runTest("public/TicTacToe.jmm", input));
        String expected = SpecsStrings.normalizeFileContents(SpecsIo.getResource("fixtures/public/TicTacToe.txt"));

        assertEquals(expected, output);
    }

    @Test
    public void WhileAndIf() {
        String output = runTest("public/WhileAndIF.jmm");
        String expected = SpecsStrings.normalizeFileContents(SpecsIo.getResource("fixtures/public/WhileAndIF.txt"));

        assertEquals(expected, output);
    }

    // Custom tests

    @Test
    public void BinaryTest() {
        String output = runTest("public/Binary.jmm", "42\n");
        String expected = "101010";

        assertEquals(expected, output);
    }

    @Test
    public void MontyHallTest() {
        String output = runTest("public/MontyHall.jmm", "32768\n");
        String expected = "[0-9]{5}\n[0-9]{5}";

        assertTrue(output.matches(expected));
    }

    @Test
    public void TransposeTest() {
        String output = runTest("public/Transpose.jmm");
        String expected = SpecsStrings.normalizeFileContents(SpecsIo.getResource("fixtures/public/Transpose.txt"));

        assertEquals(expected, output);
    }

    @Test
    public void FunctionIndexingTest() {
        runTest("public/Optimisation.jmm");
    }
}
