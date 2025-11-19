package com.example.healbotparser;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

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

        String wowDirectoryPath;
        if (args.length < 1) {
            // Show file chooser dialog
            wowDirectoryPath = showDirectoryChooser();
            if (wowDirectoryPath == null) {
                System.out.println("No directory selected. Exiting.");
                System.exit(0);
            }
        } else {
            wowDirectoryPath = args[0];
        }

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

    /**
     * Shows a directory chooser dialog for selecting the WoW folder.
     * @return the selected directory path, or null if cancelled.
     */
    private static String showDirectoryChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select World of Warcraft Directory");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setAlwaysOnTop(true);

        int returnVal = chooser.showOpenDialog(frame);
        frame.dispose();

        if (returnVal == JFileChooser.APPROVE_OPTION) {
            return chooser.getSelectedFile().getAbsolutePath();
        }
        return null;
    }
}
