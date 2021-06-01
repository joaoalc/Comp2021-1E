
/**
 * Copyright 2021 SPeCS.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License. under the License.
 */

import org.junit.Test;

import pt.up.fe.comp.TestUtils;
import pt.up.fe.specs.util.SpecsIo;

import static org.junit.Assert.assertEquals;

public class OptimizeTest {

    @Test
    public void testHelloWorld() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/HelloWorld.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getReports().toString());
    }

    @Test
    public void testOptimisation() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Optimisation.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getReports().toString());
    }

    @Test
    public void testOptimisationWithOptimisation() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Optimisation.jmm"), true);
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getReports().toString());
    }

    @Test
    public void testSimple() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Simple.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getReports().toString());
    }

    @Test
    public void IfTest() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/If.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getReports().toString());
    }

    @Test
    public void FindMaximumTest() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/FindMaximum.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getReports().toString());
    }

    @Test
    public void LazysortTest() {

        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Lazysort.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getReports().toString());
    }

    @Test
    public void LifeTest() {
        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Life.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getReports().toString());
    }

    @Test
    public void MonteCarloPiTest() {

        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/MonteCarloPi.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getReports().toString());
    }

    @Test
    public void QuicksortTest() {

        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/Quicksort.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getReports().toString());
    }

    @Test
    public void TicTacToeTest() {

        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/TicTacToe.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getReports().toString());
    }

    @Test
    public void WhileAndIFTest() {

        var result = TestUtils.optimize(SpecsIo.getResource("fixtures/public/WhileAndIF.jmm"));
        TestUtils.noErrors(result.getReports());

        System.out.println(result.getReports().toString());
    }

}
