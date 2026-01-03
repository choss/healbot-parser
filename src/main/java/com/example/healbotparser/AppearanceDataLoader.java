package com.example.healbotparser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Loads and caches appearance ID to item ID mappings from CSV data.
 */
public class AppearanceDataLoader {

    private static final String CSV_FILE = "ItemModifiedAppearance.12.0.1.64914.csv";
    private static Map<Integer, List<Integer>> sourceIdToItemIds = null;
    private static boolean loaded = false;

    /**
     * Loads the CSV file and builds a map of source ID -> list of item IDs.
     * Uses lazy loading and caching.
     *
     * @return Map of source ID to list of item IDs
     */
    public static synchronized Map<Integer, List<Integer>> getSourceToItemMapping() {
        if (loaded) {
            return sourceIdToItemIds;
        }

        sourceIdToItemIds = new HashMap<>();
        
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    AppearanceDataLoader.class.getClassLoader().getResourceAsStream(CSV_FILE),
                    StandardCharsets.UTF_8))) {
            
            String line;
            boolean firstLine = true;
            
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    firstLine = false; // Skip header
                    continue;
                }
                
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    try {
                        int sourceId = Integer.parseInt(parts[0].trim());
                        int itemId = Integer.parseInt(parts[1].trim());
                        
                        sourceIdToItemIds.computeIfAbsent(sourceId, k -> new ArrayList<>()).add(itemId);
                    } catch (NumberFormatException e) {
                        // Skip malformed lines
                    }
                }
            }
            
            System.out.println("Loaded appearance data: " + sourceIdToItemIds.size() + " source IDs mapped to items");
            
        } catch (IOException | NullPointerException e) {
            System.err.println("Warning: Could not load appearance data from " + CSV_FILE + ": " + e.getMessage());
            System.err.println("Source IDs will be displayed without item ID resolution.");
        }
        
        loaded = true;
        return sourceIdToItemIds;
    }

    /**
     * Gets the list of item IDs for a given source ID.
     *
     * @param sourceId The source/appearance ID
     * @return List of item IDs, or empty list if not found
     */
    public static List<Integer> getItemIdsForSource(Integer sourceId) {
        if (sourceId == null) {
            return Collections.emptyList();
        }
        
        Map<Integer, List<Integer>> mapping = getSourceToItemMapping();
        return mapping.getOrDefault(sourceId, Collections.emptyList());
    }

    /**
     * Gets the first (primary) item ID for a given source ID.
     *
     * @param sourceId The source/appearance ID
     * @return The first item ID, or null if not found
     */
    public static Integer getPrimaryItemIdForSource(Integer sourceId) {
        List<Integer> items = getItemIdsForSource(sourceId);
        return items.isEmpty() ? null : items.get(0);
    }
}
