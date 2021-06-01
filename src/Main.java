import pt.up.fe.comp.jmm.JmmParser;
import pt.up.fe.comp.jmm.JmmParserResult;
import pt.up.fe.comp.jmm.analysis.JmmSemanticsResult;
import pt.up.fe.comp.jmm.jasmin.JasminResult;
import pt.up.fe.comp.jmm.jasmin.JasminUtils;
import pt.up.fe.comp.jmm.ollir.OllirResult;
import pt.up.fe.specs.util.SpecsIo;
import stage.AnalysisStage;
import stage.BackendStage;
import stage.OptimizationStage;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Main implements JmmParser {

    public JmmParserResult parse(String jmmCode) {
        JMM jmm = new JMM(new StringReader(jmmCode));
        try {
            SimpleNode root = jmm.Program(); // returns reference to root node

            return new JmmParserResult(root, jmm.getReports());
        }

        catch (ParseException e) {
            return new JmmParserResult(null, jmm.getReports());
        }
    }

    public static void main(String[] args) {
        String filePath;

        boolean optimise = false;

        if (args.length < 1){
            System.out.print("Usage: java -jar <jarfile> [-o] file");
            return;
        }

        if (args[0].equals("-o")) {
            optimise = true;
            filePath = args[1];
        } else {
            filePath = args[0];
        }

        // Read code from input file
        String code = SpecsIo.read(filePath);
        String fileName = new File(filePath).getName();
        
        JmmParserResult parserResult = new Main().parse(code);
        JmmSemanticsResult semanticsResult = new AnalysisStage().semanticAnalysis(parserResult);

        if (optimise)
            semanticsResult = new OptimizationStage().optimize(semanticsResult);

        OllirResult ollirResult = new OptimizationStage().toOllir(semanticsResult);
        JasminResult jasminResult = new BackendStage().toJasmin(ollirResult);
        
        // JSON
        try (BufferedWriter jsonWriter = Files.newBufferedWriter(Paths.get(fileName + ".json"))) {
            jsonWriter.write(parserResult.toJson());
        } catch (IOException e) {
            System.err.println("Failed to write JSON file");
        }

        // SymbolTable
        try (BufferedWriter symbolWriter = Files.newBufferedWriter(Paths.get(fileName + ".symbols.txt"))) {
            symbolWriter.write(semanticsResult.getSymbolTable().print());
        } catch (IOException e) {
            System.err.println("Failed to write SymbolTable file");
        }

        // OLLIR
        try (BufferedWriter ollirWriter = Files.newBufferedWriter(Paths.get(fileName + ".ollir"))) {
            ollirWriter.write(ollirResult.getOllirCode());
        } catch (IOException e) {
            System.err.println("Failed to write OLLIR file");
        }

        // Jasmin
        try (BufferedWriter jasminWriter = Files.newBufferedWriter(Paths.get(fileName + ".j"))) {
            jasminWriter.write(jasminResult.getJasminCode());
        } catch (IOException e) {
            System.err.println("Failed to write Jasmin file");
        }
        
        // Compiled class
        try {
            JasminUtils.assemble(new File(fileName + ".j"), new File("./"));
        } catch (Exception e) {
            System.err.println("Failed to write Compiled classfile");
        }
    }
}