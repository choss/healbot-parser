package com.example.healbotparser;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
            // Try to detect default WoW path
            String defaultPath = getDefaultWowPath();
            if (defaultPath != null) {
                wowDirectoryPath = defaultPath;
                System.out.println("Using detected WoW directory: " + wowDirectoryPath);
            } else {
                // Check if we're running in a native image (GraalVM)
                boolean isNativeImage = System.getProperty("org.graalvm.nativeimage.imagecode") != null;
                
                if (isNativeImage) {
                    // In native image mode, use current directory as fallback
                    wowDirectoryPath = System.getProperty("user.dir");
                    System.out.println("No WoW directory detected. Using current directory: " + wowDirectoryPath);
                    System.out.println("To specify a different directory, use: healbot-parser <wow-directory> [output-file]");
                    System.out.println();
                } else {
                    // Show file chooser dialog (only in JAR mode)
                    wowDirectoryPath = showDirectoryChooser();
                    if (wowDirectoryPath == null) {
                        System.out.println("No directory selected. Exiting.");
                        System.exit(0);
                    }
                }
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
            Map<String, Map<String, List<BindingInfo>>> data = parser.parseDirectory(wowPath);

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
     * Attempts to detect the default World of Warcraft installation directory.
     * @return the path to WoW if found, null otherwise.
     */
    private static String getDefaultWowPath() {
        String os = System.getProperty("os.name").toLowerCase();
        String[] candidates;

        if (os.contains("win")) {
            // Try to read from Windows Registry first
            try {
                Process process = Runtime.getRuntime().exec("reg query \"HKEY_LOCAL_MACHINE\\SOFTWARE\\WOW6432Node\\Blizzard Entertainment\\World of Warcraft\" /v InstallPath");
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.trim().startsWith("InstallPath")) {
                        String[] parts = line.trim().split("\\s+", 3);
                        if (parts.length >= 3) {
                            String path = parts[2];
                            Path wowPath = Paths.get(path);
                            if (Files.exists(wowPath) && Files.isDirectory(wowPath)) {
                                return path;
                            }
                        }
                    }
                }
                process.waitFor();
            } catch (Exception e) {
                // Ignore exceptions and fall back to hardcoded paths
            }

            // Fallback to hardcoded paths
            candidates = new String[] {
                "C:\\Program Files (x86)\\World of Warcraft",
                "C:\\Program Files\\World of Warcraft",
                "D:\\Program Files (x86)\\World of Warcraft",
                "D:\\Program Files\\World of Warcraft"
            };
        } else if (os.contains("mac")) {
            candidates = new String[] {
                "/Applications/World of Warcraft"
            };
        } else if (os.contains("linux") || os.contains("unix")) {
            String home = System.getProperty("user.home");
            candidates = new String[] {
                home + "/.wine/drive_c/Program Files (x86)/World of Warcraft",
                home + "/.wine/drive_c/Program Files/World of Warcraft"
            };
        } else {
            return null;
        }

        for (String candidate : candidates) {
            Path path = Paths.get(candidate);
            if (Files.exists(path) && Files.isDirectory(path)) {
                return candidate;
            }
        }
        return null;
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
