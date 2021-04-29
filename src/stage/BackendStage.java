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

import javax.naming.NoPermissionException;

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

            ollirClass.checkMethodLabels();  // Check the use of labels in the OLLIR loaded
            ollirClass.buildCFGs();          // Build the CFG of each method
            ollirClass.outputCFGs();         // Output to .dot files the CFGs, one per method
            ollirClass.buildVarTables();     // Build the table of variables for each method
            ollirClass.show();               // Print to console main information about the input OLLIR

            // Convert the OLLIR to a String containing the equivalent Jasmin code
            String jasminCode = "";

            String classAccessModifier = acessModifierToString(ollirClass.getClassAccessModifier());

            if (classAccessModifier.isEmpty())
                classAccessModifier = "public";

            String className = ollirClass.getClassName();

            jasminCode += String.format(".class %s %s\n", classAccessModifier, className);

            String superClassName = ollirClass.getSuperClass();

            if (superClassName == null)
                superClassName = "java/lang/Object";

            jasminCode += String.format(".super %s\n\n", superClassName);

            for (Method method : ollirClass.getMethods()) {
                // Method name
                String methodName = method.getMethodName();

                if (method.isConstructMethod())
                    methodName = "<init>";

                // Method access modifier
                String methodAccessModifier = acessModifierToString(method.getMethodAccessModifier());

                if (!methodAccessModifier.isEmpty())
                    methodAccessModifier += " ";

                // Method parameters
                String paramsCode = "";

                for (Element parameter : method.getParams())
                    paramsCode += parameter;

                jasminCode += String.format(".method %s%s(%s)\n", methodAccessModifier, methodName, paramsCode);

                String instructionCode = "";

                // Iterate over method's instructions
                for (Instruction instruction : method.getInstructions()) {
                    switch (instruction.getInstType()) {
                        case CALL:
                            generate((CallInstruction) instruction);
                            break;
                        case GOTO:
                            generate((GotoInstruction) instruction);
                            break;
                        case NOPER:
                            break;
                        case ASSIGN:
                            generate((AssignInstruction) instruction);
                            break;
                        case BRANCH:
                            generate((CondBranchInstruction) instruction);
                            break;
                        case RETURN:
                            instructionCode = generate((ReturnInstruction) instruction);
                            break;
                        case GETFIELD:
                            generate((GetFieldInstruction) instruction);
                            break;
                        case PUTFIELD:
                            generate((PutFieldInstruction) instruction);
                            break;
                        case UNARYOPER:
                            generate((UnaryOpInstruction) instruction);
                            break;
                        case BINARYOPER:
                            generate((BinaryOpInstruction) instruction);
                            break;
                    }
                }

                jasminCode += instructionCode;
                jasminCode += ".end method\n\n";
            }

            // More reports from this stage
            List<Report> reports = new ArrayList<>();

            System.out.println(jasminCode);

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

    private String acessModifierToString(AccessModifiers accessModifier) {
        switch (accessModifier) {
            case PUBLIC:
                return "public";

            case PRIVATE:
                return "private";

            case PROTECTED:
                return "protected";

            case DEFAULT:
                return "";
        }

        return "";
    }

    private String generate(CallInstruction instruction) {
        instruction.show();

        return "";
    }

    private void generate(GotoInstruction instruction) {

    }

    private void generate(AssignInstruction instruction) {

    }

    private void generate(CondBranchInstruction instruction) {

    }

    private String generate(ReturnInstruction instruction) {
        return "return";
    }

    private void generate(GetFieldInstruction instruction) {

    }

    private void generate(PutFieldInstruction instruction) {

    }

    private void generate(UnaryOpInstruction instruction) {

    }

    private void generate(BinaryOpInstruction instruction) {

    }
}
