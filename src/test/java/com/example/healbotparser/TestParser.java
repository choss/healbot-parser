package com.example.healbotparser;

public class TestParser {
    public static void main(String[] args) {
        // Test the convertToHumanReadable logic directly
        String[] testInputs = {
            "C:A", "C:F", "C:M", "C:T", "C:TM", "C:S", "C:TL",  // Known commands
            "C:D", "C:E", "C:B",  // Unknown/custom commands
            "S:123^Flash Heal",   // Spell code
            "I:12345"             // Item code
        };

        System.out.println("Testing convertToHumanReadable logic:");
        System.out.println("=====================================");

        for (String input : testInputs) {
            String result = convertToHumanReadable(input);
            System.out.println("Input: " + input + " -> Output: " + result);
        }
    }

    // Copy of the convertToHumanReadable logic for testing
    private static String convertToHumanReadable(String rawValue) {
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

        return rawValue;
    }
}