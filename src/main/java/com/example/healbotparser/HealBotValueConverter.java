package com.example.healbotparser;

/**
 * Utility class for converting HealBot internal codes to human-readable names.
 * This class contains only the conversion logic with no external dependencies.
 */
public class HealBotValueConverter {

    /**
     * Converts HealBot internal codes to human-readable names.
     *
     * @param rawValue The raw value from HealBot configuration
     * @return Human-readable representation
     */
    public static String convertToHumanReadable(String rawValue) {
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
                default -> "Custom Command: " + commandCode;
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
                return "<a href=\"https://www.wowhead.com/spell=" + spellId + "\" data-wowhead=\"spell=" + spellId + "\" target=\"_blank\" class=\"spell-link\" title=\"View " + spellName + " on Wowhead\">" + spellName + "</a>";
            } else {
                // Format: S:spellId:spellName or just S:spellId
                int colonIndex = spellPart.indexOf(':');
                if (colonIndex > 0) {
                    String spellId = spellPart.substring(0, colonIndex);
                    String spellName = spellPart.substring(colonIndex + 1);
                    return "<a href=\"https://www.wowhead.com/spell=" + spellId + "\" data-wowhead=\"spell=" + spellId + "\" target=\"_blank\" class=\"spell-link\" title=\"View " + spellName + " on Wowhead\">" + spellName + "</a>";
                } else {
                    // Just spellId
                    return "<a href=\"https://www.wowhead.com/spell=" + spellPart + "\" data-wowhead=\"spell=" + spellPart + "\" target=\"_blank\" class=\"spell-link\" title=\"View Spell " + spellPart + " on Wowhead\">Spell: " + spellPart + "</a>";
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