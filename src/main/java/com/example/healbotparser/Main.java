package com.example.healbotparser;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Main entry point for the HealBot SavedVariables Parser.
 * This application parses World of Warcraft HealBot SavedVariables files
 * and generates an HTML report of the click-casting configurations.
 */
public class Main {
    
    /**
     * Main entry point of the application.
     * 
     * @param args Command-line arguments. Expects the path to the WoW directory as the first argument.
     */
    public static void main(String[] args) {
        System.out.println("=== HealBot SavedVariables Parser ===");
        System.out.println("Version: 1.0-SNAPSHOT");
        System.out.println();
        
        // Validate command-line arguments
        if (args.length == 0) {
            System.err.println("Error: No WoW directory path provided.");
            System.err.println("Usage: java -jar healbot-parser.jar <wow-directory-path>");
            System.err.println();
            System.err.println("Example: java -jar healbot-parser.jar C:\\Program Files (x86)\\World of Warcraft");
            System.exit(1);
        }
        
        String wowDirectoryPath = args[0];
        System.out.println("WoW Directory: " + wowDirectoryPath);
        
        Path wowPath = Paths.get(wowDirectoryPath);
        System.out.println("Resolved Path: " + wowPath.toAbsolutePath());
        System.out.println();
        
        // Initialize the parser
        System.out.println("Initializing HealBot Parser...");
        HealBotParser parser = new HealBotParser();
        
        try {
            // TODO: Full implementation in follow-up PR
            System.out.println("Starting parsing process...");
            parser.parse(wowPath);
            
            System.out.println("Generating HTML report...");
            parser.generateReport();
            
            System.out.println();
            System.out.println("=== Parsing Complete ===");
            System.out.println("Report generated successfully (stub implementation).");
        } catch (Exception e) {
            System.err.println("Error during parsing: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
