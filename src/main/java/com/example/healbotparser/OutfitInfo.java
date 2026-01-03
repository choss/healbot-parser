package com.example.healbotparser;

import java.util.Map;

/**
 * Simple data class to hold BetterWardrobe outfit information for Thymeleaf templates.
 */
public class OutfitInfo {
    private final String character;
    private final String realm;
    private final String outfitName;
    private final String icon;
    private final Map<String, SlotData> slots; // slot name -> slot data
    private final String filename;

    public OutfitInfo(String character, String realm, String outfitName, String icon, 
                      Map<String, SlotData> slots, String filename) {
        this.character = character;
        this.realm = realm;
        this.outfitName = outfitName;
        this.icon = icon;
        this.slots = slots;
        this.filename = filename;
    }

    public String getCharacter() { return character; }
    public String getRealm() { return realm; }
    public String getOutfitName() { return outfitName; }
    public String getIcon() { return icon; }
    public Map<String, SlotData> getSlots() { return slots; }
    public String getFilename() { return filename; }

    /**
     * Inner class to hold data for a single equipment slot.
     */
    public static class SlotData {
        private final Integer sourceId;
        private final Integer itemId;
        private final String itemName;
        private final Integer enchantId; // For weapon illusions

        public SlotData(Integer sourceId, Integer itemId, String itemName, Integer enchantId) {
            this.sourceId = sourceId;
            this.itemId = itemId;
            this.itemName = itemName;
            this.enchantId = enchantId;
        }

        public Integer getSourceId() { return sourceId; }
        public Integer getItemId() { return itemId; }
        public String getItemName() { return itemName; }
        public Integer getEnchantId() { return enchantId; }
        
        public boolean isEmpty() {
            return sourceId == null || sourceId == 0;
        }
    }
}
