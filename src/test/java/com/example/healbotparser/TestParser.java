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

        // Create an instance of the actual parser to test
        // HealBotParser parser = new HealBotParser();

        for (String input : testInputs) {
            String result = HealBotValueConverter.convertToHumanReadable(input);
            System.out.println("Input: " + input + " -> Output: " + result);
        }
    }
}