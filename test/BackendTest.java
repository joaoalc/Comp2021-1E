
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
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.ollir.OllirUtils;
import pt.up.fe.specs.util.SpecsIo;
import pt.up.fe.specs.util.SpecsStrings;

import java.util.ArrayList;

public class BackendTest {
    private void runTest(String filename, String expected) {
        JasminResult result = TestUtils.backend(SpecsIo.getResource("fixtures/" + filename));
        TestUtils.noErrors(result.getReports());

        String output = SpecsStrings.normalizeFileContents(result.run().trim());
        expected = SpecsStrings.normalizeFileContents(expected);

        assertEquals(output, expected);
    }

    @Test
    public void HelloWorldTest() {
        runTest("public/HelloWorld.jmm", "Hello, World!");
    }

    @Test
    public void SimpleTest() {
        runTest("public/Simple.jmm", "30");
    }

    @Test
    public void Fac() {
        OllirResult ollirResult = new OllirResult(
            OllirUtils.parse(SpecsIo.getResource("fixtures/public/ollir/Fac.ollir")),
            null,
            new ArrayList<>()
        );

        JasminResult result = TestUtils.backend(ollirResult);
        TestUtils.noErrors(result.getReports());

        String output = result.run();
        assertEquals("3628800", output.trim());
    }

    @Test
    public void MyClass1() {
        OllirResult ollirResult = new OllirResult(
            OllirUtils.parse(SpecsIo.getResource("fixtures/public/ollir/myclass1.ollir")),
            null,
            new ArrayList<>()
        );

        JasminResult result = TestUtils.backend(ollirResult);
        TestUtils.noErrors(result.getReports());

        result.run();
    }

    @Test
    public void MyClass2() {
        OllirResult ollirResult = new OllirResult(
            OllirUtils.parse(SpecsIo.getResource("fixtures/public/ollir/myclass2.ollir")),
            null,
            new ArrayList<>()
        );

        JasminResult result = TestUtils.backend(ollirResult);
        TestUtils.noErrors(result.getReports());

        result.run();
    }

    @Test
    public void MyClass3() {
        OllirResult ollirResult = new OllirResult(
            OllirUtils.parse(SpecsIo.getResource("fixtures/public/ollir/myclass3.ollir")),
            null,
            new ArrayList<>()
        );

        JasminResult result = TestUtils.backend(ollirResult);
        TestUtils.noErrors(result.getReports());

        String output = SpecsStrings.normalizeFileContents(result.run());
        assertEquals(SpecsStrings.normalizeFileContents("val = 2\nval = 0\nval = 2"), output.trim());
    }

    @Test
    public void MyClass4() {
        OllirResult ollirResult = new OllirResult(
            OllirUtils.parse(SpecsIo.getResource("fixtures/public/ollir/myclass4.ollir")),
            null,
            new ArrayList<>()
        );

        JasminResult result = TestUtils.backend(ollirResult);
        TestUtils.noErrors(result.getReports());

        result.run();
    }
}
