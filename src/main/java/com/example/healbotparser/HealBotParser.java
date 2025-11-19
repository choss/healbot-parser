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
     * @return Map of identifier to button-spell mappings
     * @throws IOException if file operations fail
     */
    public Map<String, Map<String, String>> parseDirectory(Path wowDirectory) throws IOException {
        Map<String, Map<String, String>> result = new HashMap<>();

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
                                                            Map<String, String> spells = parseHealBotFile(file);
                                                            if (!spells.isEmpty()) {
                                                                result.put(identifier, spells);
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
                            String humanReadableValue = convertToHumanReadable(rawValue);
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
     * @param data The parsed data
     * @param outputFile Path to write the HTML report
     * @throws IOException if writing fails
     */
    public void generateHtmlReport(Map<String, Map<String, String>> data, String outputFile) throws IOException {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("/templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);
        templateEngine.setTemplateResolver(templateResolver);

        Context context = new Context();
        context.setVariable("data", restructureDataForDisplay(data));

        String html = templateEngine.process("report", context);

        try (FileWriter writer = new FileWriter(outputFile)) {
            writer.write(html);
        }
    }

    /**
     * Restructures the data to separate modifiers from base buttons.
     * Creates a list of binding objects for each account.
     */
    private Map<String, List<BindingInfo>> restructureDataForDisplay(Map<String, Map<String, String>> data) {
        Map<String, List<BindingInfo>> restructured = new HashMap<>();

        for (Map.Entry<String, Map<String, String>> accountEntry : data.entrySet()) {
            String account = accountEntry.getKey();
            Map<String, String> bindings = accountEntry.getValue();
            List<BindingInfo> accountBindings = new ArrayList<>();

            for (Map.Entry<String, String> bindingEntry : bindings.entrySet()) {
                String buttonName = bindingEntry.getKey();
                String spell = bindingEntry.getValue();

                // Parse modifier and base button
                String[] parts = parseButtonName(buttonName);
                String modifier = parts[0];
                String baseButton = parts[1];

                accountBindings.add(new BindingInfo(baseButton, modifier, spell));
            }

            // Sort by button name, then by modifier
            accountBindings.sort((a, b) -> {
                int buttonCompare = a.getButton().compareTo(b.getButton());
                if (buttonCompare != 0) return buttonCompare;
                return a.getModifier().compareTo(b.getModifier());
            });

            restructured.put(account, accountBindings);
        }

        return restructured;
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

    /**
     * Converts HealBot internal codes to human-readable names.
     *
     * @param rawValue The raw value from HealBot configuration
     * @return Human-readable representation
     */
    private String convertToHumanReadable(String rawValue) {
        if (rawValue == null || rawValue.isEmpty()) {
            return rawValue;
        }

        // Handle command codes (C:code)
        if (rawValue.startsWith("C:")) {
            String commandCode = rawValue.substring(2);
            return switch (commandCode) {
                case "A" -> "Assist";
                case "F" -> "Focus";
                case "M" -> "Menu";
                case "T" -> "MainTank";
                case "TM" -> "MainAssist";
                case "S" -> "Stop";
                case "TL" -> "Tell";
                default -> "Command: " + commandCode;
            };
        }

        // Handle spell codes (S:spellId^spellName)
        if (rawValue.startsWith("S:")) {
            String spellPart = rawValue.substring(2);
            int caretIndex = spellPart.indexOf('^');
            if (caretIndex > 0) {
                // Format: S:spellId^spellName
                String spellId = spellPart.substring(0, caretIndex);
                String spellName = spellPart.substring(caretIndex + 1);
                return "<a href=\"https://www.wowhead.com/spell=" + spellId + "\" target=\"_blank\" class=\"spell-link\">🔗 " + spellName + "</a>";
            } else {
                // Format: S:spellId:spellName or just S:spellId
                int colonIndex = spellPart.indexOf(':');
                if (colonIndex > 0) {
                    String spellId = spellPart.substring(0, colonIndex);
                    String spellName = spellPart.substring(colonIndex + 1);
                    return "<a href=\"https://www.wowhead.com/spell=" + spellId + "\" target=\"_blank\" class=\"spell-link\">🔗 " + spellName + "</a>";
                } else {
                    // Just spellId
                    return "<a href=\"https://www.wowhead.com/spell=" + spellPart + "\" target=\"_blank\" class=\"spell-link\">🔗 Spell: " + spellPart + "</a>";
                }
            }
        }

        // Handle item codes (I:itemId)
        if (rawValue.startsWith("I:")) {
            return "Item: " + rawValue.substring(2);
        }

        // Return as-is for other values
        return rawValue;
    }
}
