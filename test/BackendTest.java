
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

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

public class BackendTest {
    private void runTest(String filename, String expected) {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/" + filename));
        TestUtils.noErrors(result.getReports());

        String output = SpecsStrings.normalizeFileContents(result.run().trim());
        expected = SpecsStrings.normalizeFileContents(expected);

        assertEquals(output, expected);
    }

    private void runTest(String filename, String expected, String input) {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/" + filename));
        TestUtils.noErrors(result.getReports());

        String output = SpecsStrings.normalizeFileContents(result.run(input).trim());
        expected = SpecsStrings.normalizeFileContents(expected);

        assertEquals(output, expected);
    }

    @Test
    public void FindMaximum() {
        runTest("public/FindMaximum.jmm", "Result: 28");
    }

    @Test
    public void HelloWorldTest() {
        runTest("public/HelloWorld.jmm", "Hello, World!");
    }

    @Test
    public void LazysortTest() {
        // This tests might sometimes fail because of randomness
        runTest("public/Lazysort.jmm", "1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
    }

    @Test
    public void LifeTest() {
        runTest("public/Life.jmm", "");
    }

    @Test
    public void MonteCarloPiTest() {
        runTest("public/MonteCarloPi.jmm", "", "10\n");
    }

    @Test
    public void QuickSortTest() {
        runTest("public/QuickSort.jmm", "1\n2\n3\n4\n5\n6\n7\n8\n9\n10");
    }

    @Test
    public void SimpleTest() {
        runTest("public/Simple.jmm", "30");
    }

    @Test
    public void TicTacToeTest() {
        runTest("public/TicTacToe.jmm", "");
    }

    @Test
    public void WhileAndIf() {
        runTest("public/WhileAndIF.jmm", "10\n10\n10\n10\n10\n10\n10\n10\n10\n10");
    }
}
