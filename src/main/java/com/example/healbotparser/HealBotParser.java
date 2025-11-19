package com.example.healbotparser;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Parser for HealBot SavedVariables files.
 * This class handles the extraction and processing of HealBot configuration data
 * from World of Warcraft SavedVariables files.
 */
public class HealBotParser {

    // HealBot configuration constants
    private static final String HEALBOT_CONFIG_SPELLS = "HealBot_Config_Spells";
    private static final String ENABLED_KEY_COMBO = "EnabledKeyCombo";

    /**
     * Creates a friendly identifier from an account path.
     * Now includes account/server/character information.
     */
    private static String friendlyIdentifier(Path accountPath, Path serverPath, Path characterPath) {
        String account = accountPath.getFileName().toString();
        String server = serverPath.getFileName().toString();
        String character = characterPath.getFileName().toString();
        return account + "/" + server + "/" + character;
    }

    /**
     * Parses HealBot SavedVariables from the specified WoW directory.
     * Traverses WTF/Account/{account}/{server}/{character}/SavedVariables/HealBot*.lua files.
     *
     * @param wowDirectory The root directory of the World of Warcraft installation
     * @return Map of identifier to Map of filename to list of bindings
     * @throws IOException if file operations fail
     */
    public Map<String, Map<String, List<BindingInfo>>> parseDirectory(Path wowDirectory) throws IOException {
        Map<String, Map<String, List<BindingInfo>>> result = new HashMap<>();

        Path wtfPath = wowDirectory.resolve("WTF");
        if (!Files.exists(wtfPath)) {
            System.out.println("WTF directory not found at " + wtfPath);
            return result;
        }

        Path accountPath = wtfPath.resolve("Account");
        if (!Files.exists(accountPath)) {
            System.out.println("Account directory not found at " + accountPath);
            return result;
        }

        try (Stream<Path> accounts = Files.list(accountPath)) {
            accounts.filter(Files::isDirectory).forEach(account -> {
                try {
                    // For each account, look for server directories
                    try (Stream<Path> servers = Files.list(account)) {
                        servers.filter(Files::isDirectory).forEach(server -> {
                            try {
                                // For each server, look for character directories
                                try (Stream<Path> characters = Files.list(server)) {
                                    characters.filter(Files::isDirectory).forEach(character -> {
                                        try {
                                            Path savedVars = character.resolve("SavedVariables");
                                            if (Files.exists(savedVars)) {
                                                try (Stream<Path> files = Files.list(savedVars)) {
                                                    files.filter(p -> p.getFileName().toString().startsWith("HealBot") && p.toString().endsWith(".lua"))
                                                        .forEach(file -> {
                                                            String identifier = friendlyIdentifier(account, server, character);
                                                            String filename = file.getFileName().toString();
                                                            Map<String, String> spells = parseHealBotFile(file);
                                                            if (!spells.isEmpty()) {
                                                                Map<String, List<BindingInfo>> fileMap = result.computeIfAbsent(identifier, k -> new LinkedHashMap<>());
                                                                List<BindingInfo> bindings = fileMap.computeIfAbsent(filename, k -> new ArrayList<>());
                                                                for (Map.Entry<String, String> entry : spells.entrySet()) {
                                                                    String[] parts = parseButtonName(entry.getKey());
                                                                    bindings.add(new BindingInfo(parts[1], parts[0], entry.getValue(), filename));
                                                                }
                                                            }
                                                        });
                                                }
                                            }
                                        } catch (IOException e) {
                                            System.err.println("Error processing character " + character + ": " + e.getMessage());
                                        }
                                    });
                                }
                            } catch (IOException e) {
                                System.err.println("Error processing server " + server + ": " + e.getMessage());
                            }
                        });
                    }
                } catch (IOException e) {
                    System.err.println("Error processing account " + account + ": " + e.getMessage());
                }
            });
        }

        return result;
    }

    /**
     * Parses a single HealBot.lua file using LuaJ to extract EnabledKeyCombo.
     *
     * @param file Path to the HealBot.lua file
     * @return Map of button to spell, or empty map on failure
     */
    public Map<String, String> parseHealBotFile(Path file) {
        Map<String, String> spells = new HashMap<>();
        try {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.loadfile(file.toString());
            chunk.call();

            LuaValue config = globals.get(HEALBOT_CONFIG_SPELLS);
            if (!config.isnil()) {
                LuaValue enabledKeyCombo = config.get(ENABLED_KEY_COMBO);
                if (enabledKeyCombo.istable()) {
                    LuaValue k = LuaValue.NIL;
                    while (true) {
                        Varargs n = enabledKeyCombo.next(k);
                        if (n.isnil(1)) break;
                        k = n.arg1();
                        LuaValue v = n.arg(2);
                        if (k.isstring() && v.isstring()) {
                            String rawValue = v.tojstring();
                            String humanReadableValue = HealBotValueConverter.convertToHumanReadable(rawValue);
                            spells.put(k.tojstring(), humanReadableValue);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error parsing " + file + ": " + e.getMessage());
        }
        return spells;
    }

    /**
     * Generates an HTML report using Thymeleaf template.
     *
     * @param data The parsed data grouped by character and filename
     * @param outputFile Path to write the HTML report
     * @throws IOException if writing fails
     */
    public void generateHtmlReport(Map<String, Map<String, List<BindingInfo>>> data, String outputFile) throws IOException {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);
        templateEngine.setTemplateResolver(templateResolver);

        // Create TOC first to get the sorting order
        Map<String, Map<String, List<String>>> toc = createTableOfContents(data);

        Context context = new Context();
        context.setVariable("data", sortDataForDisplay(data, toc));
        context.setVariable("toc", toc);

        String html = templateEngine.process("report", context);

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(html);
        }
    }

    /**
     * Creates a table of contents structure sorted by account/server/character.
     * Servers with more characters are sorted first.
     */
    private Map<String, Map<String, List<String>>> createTableOfContents(Map<String, Map<String, List<BindingInfo>>> data) {
        // Structure: Account -> Server -> List of Characters
        Map<String, Map<String, List<String>>> toc = new HashMap<>();

        // Parse all identifiers and group them
        for (String identifier : data.keySet()) {
            String[] parts = identifier.split("/");
            if (parts.length == 3) {
                String account = parts[0];
                String server = parts[1];
                String character = parts[2];

                toc.computeIfAbsent(account, k -> new HashMap<>())
                   .computeIfAbsent(server, k -> new ArrayList<>())
                   .add(character);
            }
        }

        // Sort accounts alphabetically
        Map<String, Map<String, List<String>>> sortedToc = new HashMap<>();
        toc.entrySet().stream()
           .sorted(Map.Entry.comparingByKey())
           .forEach(accountEntry -> {
               String account = accountEntry.getKey();
               Map<String, List<String>> servers = accountEntry.getValue();

               // Sort servers by character count (descending), then alphabetically
               Map<String, List<String>> sortedServers = new HashMap<>();
               servers.entrySet().stream()
                      .sorted((a, b) -> {
                          int countCompare = Integer.compare(b.getValue().size(), a.getValue().size());
                          if (countCompare != 0) return countCompare;
                          return a.getKey().compareTo(b.getKey());
                      })
                      .forEach(serverEntry -> {
                          String server = serverEntry.getKey();
                          List<String> characters = serverEntry.getValue();

                          // Sort characters alphabetically
                          characters.sort(String::compareTo);
                          sortedServers.put(server, characters);
                      });

               sortedToc.put(account, sortedServers);
           });

        return sortedToc;
    }

    /**
     * Extracts the sorted list of identifiers from the TOC structure.
     * This ensures the report sections appear in the same order as the TOC.
     */
    private List<String> getSortedIdentifiersFromTOC(Map<String, Map<String, List<String>>> toc) {
        List<String> sortedIdentifiers = new ArrayList<>();

        // Iterate through accounts in TOC order (already sorted alphabetically)
        for (Map.Entry<String, Map<String, List<String>>> accountEntry : toc.entrySet()) {
            String account = accountEntry.getKey();
            Map<String, List<String>> servers = accountEntry.getValue();

            // Iterate through servers in TOC order (already sorted by character count, then alphabetically)
            for (Map.Entry<String, List<String>> serverEntry : servers.entrySet()) {
                String server = serverEntry.getKey();
                List<String> characters = serverEntry.getValue();

                // Iterate through characters in TOC order (already sorted alphabetically)
                for (String character : characters) {
                    sortedIdentifiers.add(account + "/" + server + "/" + character);
                }
            }
        }

        return sortedIdentifiers;
    }

    /**
     * Sorts the data to match TOC ordering.
     * Sorts files alphabetically, and bindings within each file by modifier and button.
     */
    private Map<String, Map<String, List<BindingInfo>>> sortDataForDisplay(Map<String, Map<String, List<BindingInfo>>> data, Map<String, Map<String, List<String>>> toc) {
        Map<String, Map<String, List<BindingInfo>>> sorted = new LinkedHashMap<>();

        // Get the sorted order from TOC
        List<String> sortedIdentifiers = getSortedIdentifiersFromTOC(toc);

        // Process data in TOC order
        for (String identifier : sortedIdentifiers) {
            Map<String, List<BindingInfo>> fileMap = data.get(identifier);
            if (fileMap != null && !fileMap.isEmpty()) {
                Map<String, List<BindingInfo>> sortedFiles = new LinkedHashMap<>();
                
                // Sort files alphabetically
                fileMap.keySet().stream().sorted().forEach(filename -> {
                    List<BindingInfo> bindings = fileMap.get(filename);
                    List<BindingInfo> sortedBindings = new ArrayList<>(bindings);
                    
                    // Sort bindings by modifier, then button
                    sortedBindings.sort((a, b) -> {
                        int modifierCompare = a.getModifier().compareTo(b.getModifier());
                        if (modifierCompare != 0) return modifierCompare;
                        return a.getButton().compareTo(b.getButton());
                    });
                    
                    sortedFiles.put(filename, sortedBindings);
                });

                sorted.put(identifier, sortedFiles);
            }
        }

        return sorted;
    }

    /**
     * Parses a button name to separate modifier from base button.
     * Returns array where [0] is modifier (empty string if none) and [1] is base button.
     */
    private String[] parseButtonName(String buttonName) {
        // Common modifiers in order of length (longest first to avoid partial matches)
        String[] modifiers = {"Shift", "Ctrl", "Alt"};

        for (String modifier : modifiers) {
            if (buttonName.startsWith(modifier)) {
                String baseButton = buttonName.substring(modifier.length());
                return new String[]{modifier, baseButton};
            }
        }

        // No modifier found
        return new String[]{"", buttonName};
    }
}
