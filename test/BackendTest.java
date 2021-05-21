
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

    @Test
    public void FindMaximum() {
        runTest("public/FindMaximum.jmm", "");
    }

    @Test
    public void HelloWorldTest() {
        runTest("public/HelloWorld.jmm", "Hello, World!");
    }

    @Test
    public void IfTest() {
        runTest("public/If.jmm", "");
    }

    @Test
    public void LazysortTest() {
        runTest("public/Lazysort.jmm", "");
    }

    @Test
    public void SimpleTest() {
        runTest("public/Simple.jmm", "30");
    }


}
