package com.example.healbotparser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for converting BetterWardrobe data to human-readable formats.
 * This class handles WowHead link generation and import/export formatting.
 */
public class BetterWardrobeValueConverter {

    // Equipment slot names in order
    private static final List<String> SLOT_NAMES = List.of(
        "Head", "Shoulder", "Back", "Chest", "Shirt", "Tabard",
        "Wrist", "Hands", "Waist", "Legs", "Feet", "Main Hand", "Off Hand"
    );

    // Mapping of slot indices from BetterWardrobe to display names
    // Based on WoW's InventorySlotId: https://wowpedia.fandom.com/wiki/InventorySlotId
    private static final Map<Integer, String> SLOT_INDEX_MAP = Map.ofEntries(
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

    /**
     * Creates a WowHead item link with tooltip support.
     *
     * @param itemId The WoW item ID
     * @param itemName The item name
     * @return HTML anchor tag with WowHead link and data attributes
     */
    public static String createItemLink(Integer itemId, String itemName) {
        if (itemId == null || itemId == 0) {
            return "<span class=\"empty-slot\">-</span>";
        }

        String displayName = (itemName != null && !itemName.isEmpty()) 
            ? itemName 
            : "Item " + itemId;

        return "<a href=\"https://www.wowhead.com/item=" + itemId + "\" "
             + "data-wowhead=\"item=" + itemId + "\" "
             + "target=\"_blank\" "
             + "class=\"item-link\" "
             + "title=\"View " + displayName + " on Wowhead\">"
             + displayName + "</a>";
    }

    /**
     * Creates a WowHead link for a source ID (transmog appearance source).
     * Now uses CSV data to resolve source ID to actual item ID.
     *
     * @param sourceId The appearance source ID
     * @return HTML anchor tag with WowHead link
     */
    public static String createSourceLink(Integer sourceId) {
        if (sourceId == null || sourceId == 0) {
            return "<span class=\"empty-slot\">-</span>";
        }

        // Try to resolve source ID to item IDs
        List<Integer> itemIds = AppearanceDataLoader.getItemIdsForSource(sourceId);
        
        if (!itemIds.isEmpty()) {
            // Use first item ID for the main link
            Integer primaryItemId = itemIds.get(0);
            StringBuilder sb = new StringBuilder();
            
            sb.append("<a href=\"https://www.wowhead.com/item=").append(primaryItemId).append("\" ")
              .append("data-wowhead=\"item=").append(primaryItemId).append("\" ")
              .append("target=\"_blank\" ")
              .append("class=\"item-link\" ")
              .append("title=\"View Item ").append(primaryItemId).append(" on Wowhead\">")
              .append("Item ").append(primaryItemId).append("</a>");
            
            // Show additional items if there are multiple
            if (itemIds.size() > 1) {
                sb.append(" <span class=\"alt-items\" title=\"Alternative items with same appearance\">(");
                for (int i = 1; i < Math.min(itemIds.size(), 4); i++) {
                    if (i > 1) sb.append(", ");
                    sb.append(itemIds.get(i));
                }
                if (itemIds.size() > 4) {
                    sb.append(", +").append(itemIds.size() - 4).append(" more");
                }
                sb.append(")</span>");
            }
            
            sb.append(" <span class=\"source-id\" style=\"color: #999; font-size: 0.85em;\">[Source: ")
              .append(sourceId).append("]</span>");
            
            return sb.toString();
        } else {
            // Fallback to source ID if not found in CSV
            return "<a href=\"https://www.wowhead.com/item=" + sourceId + "\" "
                 + "data-wowhead=\"item=" + sourceId + "\" "
                 + "target=\"_blank\" "
                 + "class=\"item-link\" "
                 + "title=\"View Source " + sourceId + " on Wowhead\">"
                 + "Source " + sourceId + "</a>";
        }
    }

    /**
     * Creates a Blizzard-compatible outfit export string.
     * Format: BWO:1:sourceID,sourceID,sourceID,...
     *
     * @param outfitInfo The outfit data
     * @return Export string in Blizzard-compatible format
     */
    public static String createBlizzardExport(OutfitInfo outfitInfo) {
        StringBuilder sb = new StringBuilder("BWO:1:");
        
        // Build sourceID list in order (19 slots total for compatibility)
        // We'll use -1 for hidden, 0 for empty
        int[] sourceIds = new int[19];
        for (int i = 0; i < 19; i++) {
            sourceIds[i] = 0; // Default to empty
        }

        // Map our slots to Blizzard slot indices (0-18 array positions)
        // The sources array has 19 elements indexed 0-18, but uses WoW slot IDs (1-19) as values
        Map<String, Integer> blizzardSlotMap = Map.ofEntries(
            Map.entry("Head", 0),      // sources[0] = slot 1
            Map.entry("Shoulder", 2),  // sources[2] = slot 3
            Map.entry("Shirt", 3),     // sources[3] = slot 4
            Map.entry("Chest", 4),     // sources[4] = slot 5
            Map.entry("Waist", 5),     // sources[5] = slot 6
            Map.entry("Legs", 6),      // sources[6] = slot 7
            Map.entry("Feet", 7),      // sources[7] = slot 8
            Map.entry("Wrist", 8),     // sources[8] = slot 9
            Map.entry("Hands", 9),     // sources[9] = slot 10
            Map.entry("Back", 14),     // sources[14] = slot 15
            Map.entry("Tabard", 18),   // sources[18] = slot 19
            Map.entry("Main Hand", 15),// sources[15] = slot 16
            Map.entry("Off Hand", 16)  // sources[16] = slot 17
        );

        for (Map.Entry<String, OutfitInfo.SlotData> entry : outfitInfo.getSlots().entrySet()) {
            String slotName = entry.getKey();
            OutfitInfo.SlotData slotData = entry.getValue();
            
            Integer blizzardIndex = blizzardSlotMap.get(slotName);
            if (blizzardIndex != null && slotData.getSourceId() != null) {
                sourceIds[blizzardIndex] = slotData.getSourceId();
            }
        }

        // Build comma-separated list
        for (int i = 0; i < sourceIds.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(sourceIds[i]);
        }

        return sb.toString();
    }

    /**
     * Creates an enhanced JSON export format with metadata and item names.
     *
     * @param outfitInfo The outfit data
     * @return Export string in enhanced JSON format
     */
    public static String createEnhancedJsonExport(OutfitInfo outfitInfo) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"version\": 1,\n");
        sb.append("  \"name\": \"").append(escapeJson(outfitInfo.getOutfitName())).append("\",\n");
        sb.append("  \"character\": \"").append(escapeJson(outfitInfo.getCharacter())).append("\",\n");
        sb.append("  \"realm\": \"").append(escapeJson(outfitInfo.getRealm())).append("\",\n");
        sb.append("  \"slots\": {\n");

        boolean first = true;
        for (String slotName : SLOT_NAMES) {
            OutfitInfo.SlotData slotData = outfitInfo.getSlots().get(slotName);
            if (slotData != null && !slotData.isEmpty()) {
                if (!first) sb.append(",\n");
                first = false;

                sb.append("    \"").append(slotName.toLowerCase().replace(" ", "_")).append("\": {");
                sb.append("\"sourceID\": ").append(slotData.getSourceId());
                
                if (slotData.getItemId() != null && slotData.getItemId() != 0) {
                    sb.append(", \"itemID\": ").append(slotData.getItemId());
                }
                
                if (slotData.getItemName() != null && !slotData.getItemName().isEmpty()) {
                    sb.append(", \"name\": \"").append(escapeJson(slotData.getItemName())).append("\"");
                }
                
                if (slotData.getEnchantId() != null && slotData.getEnchantId() != 0) {
                    sb.append(", \"enchantID\": ").append(slotData.getEnchantId());
                }
                
                sb.append("}");
            }
        }

        sb.append("\n  }\n");
        sb.append("}");

        return sb.toString();
    }

    /**
     * Creates a WowHead dressing room link for the outfit.
     *
     * @param outfitInfo The outfit data
     * @return WowHead dressing room URL
     */
    public static String createDressingRoomLink(OutfitInfo outfitInfo) {
        // WowHead dressing room format: https://www.wowhead.com/dressing-room#<base64encoded data>
        // For simplicity, we'll create a transmog comparison link with item IDs
        StringBuilder sb = new StringBuilder("https://www.wowhead.com/dressing-room#");
        
        // Build a simple item list for the dressing room
        // Format is more complex, but we can use itemID list approach
        List<Integer> itemIds = new ArrayList<>();
        
        for (String slotName : SLOT_NAMES) {
            OutfitInfo.SlotData slotData = outfitInfo.getSlots().get(slotName);
            if (slotData != null && slotData.getSourceId() != null && slotData.getSourceId() != 0) {
                itemIds.add(slotData.getSourceId());
            }
        }
        
        // Simple encoding - just list the source IDs
        // Real WowHead dressing room uses complex encoding, but this gives a starting point
        sb.append("sl").append(itemIds.stream().map(String::valueOf).reduce((a, b) -> a + "s" + b).orElse(""));
        
        return sb.toString();
    }

    /**
     * Gets the display name for a slot index.
     *
     * @param slotIndex The BetterWardrobe slot index
     * @return The human-readable slot name
     */
    public static String getSlotName(int slotIndex) {
        return SLOT_INDEX_MAP.getOrDefault(slotIndex, "Unknown Slot " + slotIndex);
    }

    /**
     * Gets all slot names in display order.
     *
     * @return List of slot names
     */
    public static List<String> getSlotNames() {
        return SLOT_NAMES;
    }

    /**
     * Gets all item IDs for a given source ID.
     * This is a wrapper around AppearanceDataLoader for use in templates.
     *
     * @param sourceId The source ID
     * @return Comma-separated list of item IDs
     */
    public static String getItemIdsForSource(Integer sourceId) {
        if (sourceId == null || sourceId == 0) {
            return "";
        }
        List<Integer> itemIds = AppearanceDataLoader.getItemIdsForSource(sourceId);
        if (itemIds.isEmpty()) {
            return "";
        }
        return itemIds.stream()
                      .map(String::valueOf)
                      .reduce((a, b) -> a + ", " + b)
                      .orElse("");
    }

    /**
     * Escapes special characters for JSON strings.
     *
     * @param str The string to escape
     * @return Escaped string
     */
    private static String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}
