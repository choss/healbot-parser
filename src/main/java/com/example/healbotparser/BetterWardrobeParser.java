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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for BetterWardrobe SavedVariables files.
 * This class handles the extraction and processing of BetterWardrobe outfit data
 * from World of Warcraft SavedVariables files.
 */
public class BetterWardrobeParser {

    private static final String BETTERWARDROBE_SAVEDSETDATA = "BetterWardrobe_SavedSetData";
    private static final String BETTERWARDROBE_FILE = "BetterWardrobe.lua";
    
    // Pattern to extract item info from WoW item links: |Hitem:itemId:...
    private static final Pattern ITEM_LINK_PATTERN = Pattern.compile("\\|Hitem:(\\d+):[^\\|]*\\|h\\[([^\\]]+)\\]\\|h");

    /**
     * Creates a friendly identifier from account/server/character info.
     */
    private static String createIdentifier(String account, String realm, String character) {
        return account + "/" + realm + "/" + character;
    }

    /**
     * Parses BetterWardrobe SavedVariables from the specified WoW directory.
     * Traverses WTF/Account/{account}/SavedVariables/BetterWardrobe.lua files.
     *
     * @param wowDirectory The root directory of the World of Warcraft installation
     * @return Map of identifier to list of outfits for that character
     * @throws IOException if file operations fail
     */
    public Map<String, List<OutfitInfo>> parseDirectory(Path wowDirectory) throws IOException {
        Map<String, List<OutfitInfo>> result = new HashMap<>();

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
                    Path savedVars = account.resolve("SavedVariables");
                    if (Files.exists(savedVars)) {
                        Path bwFile = savedVars.resolve(BETTERWARDROBE_FILE);
                        if (Files.exists(bwFile)) {
                            String accountName = account.getFileName().toString();
                            parseBetterWardrobeFile(bwFile, accountName, result);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing account " + account + ": " + e.getMessage());
                }
            });
        }

        return result;
    }

    /**
     * Parses a single BetterWardrobe.lua file using LuaJ.
     *
     * @param file Path to the BetterWardrobe.lua file
     * @param accountName The account name
     * @param result The result map to populate
     */
    private void parseBetterWardrobeFile(Path file, String accountName, Map<String, List<OutfitInfo>> result) {
        try {
            Globals globals = JsePlatform.standardGlobals();
            LuaValue chunk = globals.loadfile(file.toString());
            chunk.call();

            LuaValue savedSetData = globals.get(BETTERWARDROBE_SAVEDSETDATA);
            if (savedSetData.isnil()) {
                System.out.println("No BetterWardrobe_SavedSetData found in " + file);
                return;
            }

            // Navigate to global.sets which contains the outfit data
            LuaValue globalData = savedSetData.get("global");
            if (globalData.isnil() || !globalData.istable()) {
                System.out.println("No global data found in BetterWardrobe_SavedSetData");
                return;
            }

            LuaValue sets = globalData.get("sets");
            if (sets.isnil() || !sets.istable()) {
                System.out.println("No sets found in BetterWardrobe_SavedSetData");
                return;
            }

            // Iterate through each character's sets
            LuaValue k = LuaValue.NIL;
            while (true) {
                Varargs n = sets.next(k);
                if (n.isnil(1)) break;
                k = n.arg1();
                LuaValue characterSets = n.arg(2);

                if (k.isstring()) {
                    String characterKey = k.tojstring();
                    // Character key format: "CharacterName - RealmName"
                    String[] parts = characterKey.split(" - ");
                    if (parts.length == 2) {
                        String character = parts[0].trim();
                        String realm = parts[1].trim();

                        // Parse outfits for this character
                        if (characterSets.istable()) {
                            List<OutfitInfo> outfits = parseOutfits(characterSets, character, realm, accountName, file.getFileName().toString());
                            if (!outfits.isEmpty()) {
                                String identifier = createIdentifier(accountName, realm, character);
                                result.put(identifier, outfits);
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error parsing " + file + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parses outfit data from the SavedOutfits table.
     *
     * @param savedOutfits The Lua table containing saved outfits
     * @param character Character name
     * @param realm Realm name
     * @param accountName Account name
     * @param filename Source filename
     * @return List of parsed outfits
     */
    private List<OutfitInfo> parseOutfits(LuaValue savedOutfits, String character, String realm, 
                                           String accountName, String filename) {
        List<OutfitInfo> outfits = new ArrayList<>();

        LuaValue k = LuaValue.NIL;
        while (true) {
            Varargs n = savedOutfits.next(k);
            if (n.isnil(1)) break;
            k = n.arg1();
            LuaValue outfitData = n.arg(2);

            if (outfitData.istable()) {
                OutfitInfo outfit = parseOutfit(outfitData, character, realm, filename);
                if (outfit != null) {
                    outfits.add(outfit);
                }
            }
        }

        return outfits;
    }

    /**
     * Parses a single outfit from Lua table.
     *
     * @param outfitData The Lua table for one outfit
     * @param character Character name
     * @param realm Realm name
     * @param filename Source filename
     * @return Parsed OutfitInfo or null
     */
    private OutfitInfo parseOutfit(LuaValue outfitData, String character, String realm, String filename) {
        // Get outfit name
        LuaValue nameLua = outfitData.get("name");
        String outfitName = nameLua.isstring() ? nameLua.tojstring() : "Unnamed Outfit";

        // Get icon
        LuaValue iconLua = outfitData.get("icon");
        String icon = iconLua.isstring() ? iconLua.tojstring() : "";

        // Parse slots
        Map<String, OutfitInfo.SlotData> slots = new LinkedHashMap<>();
        
        // Get sources array (19 elements for all transmog slots)
        LuaValue sources = outfitData.get("sources");
        
        // Mapping from sources array index to slot names
        // Based on WoW's InventorySlotId: https://wowpedia.fandom.com/wiki/InventorySlotId
        Map<Integer, String> sourceIndexMap = Map.ofEntries(
            Map.entry(1, "Head"),
            Map.entry(3, "Shoulder"),
            Map.entry(4, "Shirt"),
            Map.entry(5, "Chest"),
            Map.entry(6, "Waist"),
            Map.entry(7, "Legs"),
            Map.entry(8, "Feet"),
            Map.entry(9, "Wrist"),
            Map.entry(10, "Hands"),
            Map.entry(15, "Back"),
            Map.entry(19, "Tabard"),
            Map.entry(16, "Main Hand"),
            Map.entry(17, "Off Hand")
        );

        if (!sources.isnil() && sources.istable()) {
            for (Map.Entry<Integer, String> entry : sourceIndexMap.entrySet()) {
                Integer sourceIndex = entry.getKey();
                String slotName = entry.getValue();
                
                LuaValue sourceLua = sources.get(sourceIndex);
                Integer sourceId = null;
                
                if (sourceLua.isnumber()) {
                    sourceId = sourceLua.toint();
                }

                // For now, we don't have item IDs/names from sources alone
                // The sourceID is what we have
                OutfitInfo.SlotData slotData = new OutfitInfo.SlotData(sourceId, null, null, null);
                slots.put(slotName, slotData);
            }
        }

        return new OutfitInfo(character, realm, outfitName, icon, slots, filename);
    }

    /**
     * Generates an HTML report using Thymeleaf template.
     *
     * @param data The parsed outfit data grouped by character
     * @param outputFile Path to write the HTML report
     * @throws IOException if writing fails
     */
    public void generateHtmlReport(Map<String, List<OutfitInfo>> data, String outputFile) throws IOException {
        TemplateEngine templateEngine = new TemplateEngine();
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("templates/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCacheable(false);
        templateEngine.setTemplateResolver(templateResolver);

        // Create TOC structure
        Map<String, Map<String, List<String>>> toc = createTableOfContents(data);

        // Create utility object for template access
        BetterWardrobeValueConverter converterInstance = new BetterWardrobeValueConverter();

        Context context = new Context();
        context.setVariable("data", sortDataForDisplay(data, toc));
        context.setVariable("toc", toc);
        context.setVariable("converter", converterInstance);
        context.setVariable("slotNames", BetterWardrobeValueConverter.getSlotNames());

        String html = templateEngine.process("betterwardrobe-report", context);

        Files.writeString(Path.of(outputFile), html, StandardCharsets.UTF_8);
    }

    /**
     * Creates a table of contents structure sorted by account/realm/character.
     */
    private Map<String, Map<String, List<String>>> createTableOfContents(Map<String, List<OutfitInfo>> data) {
        Map<String, Map<String, List<String>>> toc = new HashMap<>();

        for (String identifier : data.keySet()) {
            String[] parts = identifier.split("/");
            if (parts.length == 3) {
                String account = parts[0];
                String realm = parts[1];
                String character = parts[2];

                toc.computeIfAbsent(account, k -> new HashMap<>())
                   .computeIfAbsent(realm, k -> new ArrayList<>())
                   .add(character);
            }
        }

        // Sort accounts alphabetically
        Map<String, Map<String, List<String>>> sortedToc = new LinkedHashMap<>();
        toc.entrySet().stream()
           .sorted(Map.Entry.comparingByKey())
           .forEach(accountEntry -> {
               String account = accountEntry.getKey();
               Map<String, List<String>> realms = accountEntry.getValue();

               // Sort realms by character count (descending), then alphabetically
               Map<String, List<String>> sortedRealms = new LinkedHashMap<>();
               realms.entrySet().stream()
                      .sorted((a, b) -> {
                          int countCompare = Integer.compare(b.getValue().size(), a.getValue().size());
                          if (countCompare != 0) return countCompare;
                          return a.getKey().compareTo(b.getKey());
                      })
                      .forEach(realmEntry -> {
                          String realm = realmEntry.getKey();
                          List<String> characters = realmEntry.getValue();

                          // Sort characters alphabetically
                          characters.sort(String::compareTo);
                          sortedRealms.put(realm, characters);
                      });

               sortedToc.put(account, sortedRealms);
           });

        return sortedToc;
    }

    /**
     * Sorts the data to match TOC ordering.
     */
    private Map<String, List<OutfitInfo>> sortDataForDisplay(Map<String, List<OutfitInfo>> data, 
                                                               Map<String, Map<String, List<String>>> toc) {
        Map<String, List<OutfitInfo>> sorted = new LinkedHashMap<>();

        // Iterate in TOC order
        for (Map.Entry<String, Map<String, List<String>>> accountEntry : toc.entrySet()) {
            String account = accountEntry.getKey();
            for (Map.Entry<String, List<String>> realmEntry : accountEntry.getValue().entrySet()) {
                String realm = realmEntry.getKey();
                for (String character : realmEntry.getValue()) {
                    String identifier = createIdentifier(account, realm, character);
                    List<OutfitInfo> outfits = data.get(identifier);
                    if (outfits != null && !outfits.isEmpty()) {
                        sorted.put(identifier, outfits);
                    }
                }
            }
        }

        return sorted;
    }
}
