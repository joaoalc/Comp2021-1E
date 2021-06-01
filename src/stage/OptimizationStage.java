package stage;

import java.util.ArrayList;
import java.util.List;

import ollir.OllirData;
import ollir.OllirEmitter;
import pt.up.fe.comp.jmm.JmmNode;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.ollir.JmmOptimization;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.ReportType;
import pt.up.fe.comp.jmm.report.Stage;
import pt.up.fe.specs.util.SpecsIo;
import table.MySymbolTable;
import visitor.ConstantFoldingVisitor;
import visitor.ConstantPropagationVisitor;
import visitor.UnusedVariableVisitor;

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

public class OptimizationStage implements JmmOptimization {

    @Override
    public OllirResult toOllir(JmmSemanticsResult semanticsResult) {

        JmmNode node = semanticsResult.getRootNode();

        // Convert the AST to a String containing the equivalent OLLIR code
        OllirEmitter ollirEmitter = new OllirEmitter((MySymbolTable)semanticsResult.getSymbolTable());

        OllirData ollirData = ollirEmitter.visit(node, null);

        String ollirCode = ollirData.getOllirCode();

        // More reports from this stage
        List<Report> reports = new ArrayList<>();
        Report report = new Report(ReportType.LOG, Stage.OPTIMIZATION, 0, 0, ollirCode);
        reports.add(report);

        return new OllirResult(semanticsResult, ollirCode, reports);
    }

    @Override
    public JmmSemanticsResult optimize(JmmSemanticsResult semanticsResult) {
        JmmNode root = semanticsResult.getRootNode();

        MySymbolTable symbolTable = (MySymbolTable)semanticsResult.getSymbolTable();
        List<Report> report_list = semanticsResult.getReports();

        ConstantFoldingVisitor c_f_vis = new ConstantFoldingVisitor(symbolTable, report_list);
        ConstantPropagationVisitor c_prop_vis = new ConstantPropagationVisitor(symbolTable, report_list);

        UnusedVariableVisitor unusedVar_vis = new UnusedVariableVisitor(symbolTable, report_list);

        do {
            c_f_vis.isChanged = false;
            c_prop_vis.isChanged = false;
            unusedVar_vis.isChanged = false;
            c_f_vis.visit(root, true);
            c_f_vis.makeChanges();
            c_prop_vis.visit(root, true);
            c_prop_vis.makeChanges();
            unusedVar_vis.visit(root, false);
            unusedVar_vis.makeChanges();
        } while(c_prop_vis.isChanged || c_f_vis.isChanged || unusedVar_vis.isChanged);


        return semanticsResult;
    }

    @Override
    public OllirResult optimize(OllirResult ollirResult) {
        // THIS IS JUST FOR CHECKPOINT 3
        return ollirResult;
    }

}
