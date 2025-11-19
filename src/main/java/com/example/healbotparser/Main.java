package com.example.healbotparser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Main class for the HealBot parser application.
 */
public class Main {
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java -jar healbot-parser.jar <path-to-HealBot.lua> [output.html]");
            System.out.println();
            System.out.println("Arguments:");
            System.out.println("  <path-to-HealBot.lua>  Path to the HealBot SavedVariables file");
            System.out.println("  [output.html]          Optional output path for the HTML report (default: healbot-report.html)");
            System.exit(1);
        }
        
        String inputFile = args[0];
        String outputFile = args.length > 1 ? args[1] : "healbot-report.html";
        
        try {
            System.out.println("Parsing HealBot configuration from: " + inputFile);
            
            // Parse the HealBot file
            HealBotParser parser = new HealBotParser();
            parser.loadFile(inputFile);
            
            // Generate the HTML report
            System.out.println("Generating HTML report...");
            HtmlReportGenerator generator = new HtmlReportGenerator();
            generator.generateReport(parser, outputFile);
            
            System.out.println("Report generated successfully: " + outputFile);
            System.out.println();
            
            // Print summary
            int characterCount = parser.getCharacters().size();
            System.out.println("Summary:");
            System.out.println("  Characters found: " + characterCount);
            
            var keyCombos = parser.getEnabledKeyCombos();
            int totalBindings = 0;
            for (var characterBindings : keyCombos.values()) {
                for (var profileBindings : characterBindings.values()) {
                    totalBindings += profileBindings.getBindings().size();
                }
            }
            System.out.println("  Total key bindings: " + totalBindings);
            
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error parsing HealBot configuration: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
