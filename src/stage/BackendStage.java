package stage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.specs.comp.ollir.*;

import pt.up.fe.comp.jmm.jasmin.JasminBackend;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.ollir.OllirUtils;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;

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

public class BackendStage implements JasminBackend {
    @Override
    public JasminResult toJasmin(OllirResult ollirResult) {
        ClassUnit ollirClass = ollirResult.getOllirClass();

        try {
            // Example of what you can do with the OLLIR class

            ollirClass.checkMethodLabels();  // check the use of labels in the OLLIR loaded
            ollirClass.buildCFGs();          // build the CFG of each method
            ollirClass.outputCFGs();         // output to .dot files the CFGs, one per method
            ollirClass.buildVarTables();     // build the table of variables for each method
            ollirClass.show();               // print to console main information about the input OLLIR

            for (Method method : ollirClass.getMethods()) {
                System.out.println("Method name: " + method.getMethodName());

                for (Instruction instruction : method.getInstructions()) {
                    System.out.println("Instruction: " + instruction.getInstType());

                    switch (instruction.getInstType()) {
                        case CALL:
                            break;
                        case GOTO:
                            break;
                        case NOPER:
                            break;
                        case ASSIGN:
                            break;
                        case BRANCH:
                            break;
                        case RETURN:
                            break;
                        case GETFIELD:
                            break;
                        case PUTFIELD:
                            break;
                        case UNARYOPER:
                            break;
                        case BINARYOPER:
                            break;
                    }
                }
            }

            // Convert the OLLIR to a String containing the equivalent Jasmin code
            String jasminCode = ""; // Convert node ...

            // More reports from this stage
            List<Report> reports = new ArrayList<>();

            return new JasminResult(ollirResult, jasminCode, reports);
        }

        catch (OllirErrorException e) {
            return new JasminResult(
                    ollirClass.getClassName(),
                    null,
                    Collections.singletonList(
                            Report.newError(
                                    Stage.GENERATION,
                                    -1,
                                    -1,
                                    "Exception during Jasmin generation",
                                    e
                            )
                    )
            );
        }
    }

    private void generate(AssignInstruction instruction) {

    }
}
