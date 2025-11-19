package com.example.healbotparser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Main entry point for the HealBot SavedVariables Parser.
 * This application parses World of Warcraft HealBot SavedVariables files
 * and generates an HTML report of the click-casting configurations.
 */
public class Main {

    /**
     * Main entry point of the application.
     *
     * @param args Command-line arguments. First arg: WoW directory path. Second (optional): output file name.
     */
    public static void main(String[] args) {
        System.out.println("=== HealBot SavedVariables Parser ===");
        System.out.println("Version: 1.0-SNAPSHOT");
        System.out.println();

        if (args.length < 1) {
            System.err.println("Error: No WoW directory path provided.");
            System.err.println("Usage: java -jar healbot-parser.jar <wow-directory-path> [output-file]");
            System.err.println();
            System.err.println("Example: java -jar healbot-parser.jar \"C:\\Program Files (x86)\\World of Warcraft\"");
            System.exit(1);
        }

        String wowDirectoryPath = args[0];
        String outputFile = args.length > 1 ? args[1] : "healbot-report.html";

        System.out.println("WoW Directory: " + wowDirectoryPath);
        System.out.println("Output File: " + outputFile);
        System.out.println();

        Path wowPath = Paths.get(wowDirectoryPath);

        HealBotParser parser = new HealBotParser();

        try {
            System.out.println("Parsing HealBot SavedVariables...");
            Map<String, Map<String, String>> data = parser.parseDirectory(wowPath);

            System.out.println("Generating HTML report...");
            parser.generateHtmlReport(data, outputFile);

            System.out.println();
            System.out.println("=== Parsing Complete ===");
            System.out.println("Report generated: " + outputFile);
            System.out.println("Found data for " + data.size() + " accounts/characters.");
        } catch (Exception e) {
            System.err.println("Error during parsing: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
