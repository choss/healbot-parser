package com.example.healbotparser;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for HealBotParser.
 */
class HealBotParserTest {
    
    private HealBotParser parser;
    
    @BeforeEach
    void setUp() {
        parser = new HealBotParser();
    }
    
    @Test
    void testLoadSampleFile() throws IOException {
        String sampleFile = Paths.get("src/test/resources/sample-HealBot.lua").toString();
        assertDoesNotThrow(() -> parser.loadFile(sampleFile));
    }
    
    @Test
    void testGetCharacters() throws IOException {
        String sampleFile = Paths.get("src/test/resources/sample-HealBot.lua").toString();
        parser.loadFile(sampleFile);
        
        Set<String> characters = parser.getCharacters();
        
        assertNotNull(characters);
        assertEquals(2, characters.size());
        assertTrue(characters.contains("Priest@RealmName"));
        assertTrue(characters.contains("Paladin@RealmName"));
    }
    
    @Test
    void testGetEnabledKeyCombos() throws IOException {
        String sampleFile = Paths.get("src/test/resources/sample-HealBot.lua").toString();
        parser.loadFile(sampleFile);
        
        Map<String, Map<String, HealBotParser.KeyComboConfig>> keyCombos = parser.getEnabledKeyCombos();
        
        assertNotNull(keyCombos);
        assertTrue(keyCombos.containsKey("Priest@RealmName"));
        assertTrue(keyCombos.containsKey("Paladin@RealmName"));
        
        // Check Priest profiles
        Map<String, HealBotParser.KeyComboConfig> priestProfiles = keyCombos.get("Priest@RealmName");
        assertTrue(priestProfiles.containsKey("Default"));
        assertTrue(priestProfiles.containsKey("Raid"));
        
        // Check Default profile bindings
        HealBotParser.KeyComboConfig defaultConfig = priestProfiles.get("Default");
        assertEquals(5, defaultConfig.getBindings().size());
        
        // Verify a specific binding
        HealBotParser.KeyBinding ctrlLeft = defaultConfig.getBindings().stream()
            .filter(b -> "Ctrl+Left".equals(b.getCombo()))
            .findFirst()
            .orElse(null);
        
        assertNotNull(ctrlLeft);
        assertEquals("Flash Heal", ctrlLeft.getSpellName());
        assertEquals(2061, ctrlLeft.getSpellId());
        assertEquals(1, ctrlLeft.getButton());
        assertEquals("target", ctrlLeft.getTargetType());
    }
    
    @Test
    void testLoadFromString() {
        String luaContent = "HealBot_Config = {\n" +
            "    [\"Spells\"] = {\n" +
            "        [\"TestChar\"] = {\n" +
            "            [\"TestProfile\"] = {\n" +
            "                [\"EnabledKeyCombo\"] = {\n" +
            "                    [\"Left\"] = {\n" +
            "                        [\"SpellName\"] = \"Test Spell\",\n" +
            "                        [\"SpellID\"] = 123,\n" +
            "                        [\"Button\"] = 1,\n" +
            "                        [\"TargetType\"] = \"self\"\n" +
            "                    }\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}\n";
        
        assertDoesNotThrow(() -> parser.loadFromString(luaContent));
        
        Set<String> characters = parser.getCharacters();
        assertEquals(1, characters.size());
        assertTrue(characters.contains("TestChar"));
        
        Map<String, Map<String, HealBotParser.KeyComboConfig>> keyCombos = parser.getEnabledKeyCombos();
        assertTrue(keyCombos.containsKey("TestChar"));
        
        Map<String, HealBotParser.KeyComboConfig> profiles = keyCombos.get("TestChar");
        assertTrue(profiles.containsKey("TestProfile"));
        
        HealBotParser.KeyComboConfig config = profiles.get("TestProfile");
        assertEquals(1, config.getBindings().size());
        
        HealBotParser.KeyBinding binding = config.getBindings().get(0);
        assertEquals("Test Spell", binding.getSpellName());
        assertEquals(123, binding.getSpellId());
    }
}
