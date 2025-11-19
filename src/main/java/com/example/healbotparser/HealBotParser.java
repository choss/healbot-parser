package com.example.healbotparser;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Parser for HealBot SavedVariables files.
 * Extracts HealBot_Config_Spells.EnabledKeyCombo mappings and other configuration data.
 */
public class HealBotParser {
    
    private Globals globals;
    private LuaTable healbotConfig;
    
    /**
     * Creates a new HealBotParser instance.
     */
    public HealBotParser() {
        this.globals = JsePlatform.standardGlobals();
    }
    
    /**
     * Loads and parses a HealBot.lua SavedVariables file.
     * 
     * @param filePath Path to the HealBot.lua file
     * @throws IOException if the file cannot be read
     * @throws LuaError if the file cannot be parsed
     */
    public void loadFile(String filePath) throws IOException {
        String content = Files.readString(Paths.get(filePath));
        loadFromString(content);
    }
    
    /**
     * Loads and parses HealBot SavedVariables from a string.
     * 
     * @param luaContent The Lua content as a string
     * @throws LuaError if the content cannot be parsed
     */
    public void loadFromString(String luaContent) {
        try {
            LuaValue chunk = globals.load(luaContent);
            chunk.call();
            
            // Access HealBot_Config from globals
            LuaValue healbotConfigValue = globals.get("HealBot_Config");
            if (healbotConfigValue.istable()) {
                this.healbotConfig = healbotConfigValue.checktable();
            }
        } catch (LuaError e) {
            throw new RuntimeException("Failed to parse Lua content", e);
        }
    }
    
    /**
     * Extracts the EnabledKeyCombo mappings from HealBot_Config_Spells.
     * Returns a map of spell names to their key combo configurations.
     * 
     * @return Map of character/profile to spell configurations
     */
    public Map<String, Map<String, KeyComboConfig>> getEnabledKeyCombos() {
        Map<String, Map<String, KeyComboConfig>> result = new HashMap<>();
        
        if (healbotConfig == null) {
            return result;
        }
        
        // Navigate to HealBot_Config_Spells
        LuaValue spellsValue = healbotConfig.get("Spells");
        if (!spellsValue.istable()) {
            return result;
        }
        
        LuaTable spells = spellsValue.checktable();
        
        // Iterate through characters/realms
        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs next = spells.next(key);
            key = next.arg1();
            if (key.isnil()) break;
            
            LuaValue profilesValue = next.arg(2);
            if (profilesValue.istable()) {
                String character = key.tojstring();
                result.put(character, extractKeyComboFromProfiles(profilesValue.checktable()));
            }
        }
        
        return result;
    }
    
    /**
     * Extracts key combo configurations from a profiles table.
     */
    private Map<String, KeyComboConfig> extractKeyComboFromProfiles(LuaTable profiles) {
        Map<String, KeyComboConfig> combos = new HashMap<>();
        
        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs next = profiles.next(key);
            key = next.arg1();
            if (key.isnil()) break;
            
            LuaValue profileData = next.arg(2);
            if (profileData.istable()) {
                LuaTable profile = profileData.checktable();
                
                // Look for EnabledKeyCombo
                LuaValue enabledKeyCombo = profile.get("EnabledKeyCombo");
                if (enabledKeyCombo.istable()) {
                    String profileName = key.tojstring();
                    combos.put(profileName, parseKeyComboConfig(enabledKeyCombo.checktable()));
                }
            }
        }
        
        return combos;
    }
    
    /**
     * Parses a KeyCombo configuration table.
     */
    private KeyComboConfig parseKeyComboConfig(LuaTable keyComboTable) {
        KeyComboConfig config = new KeyComboConfig();
        
        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs next = keyComboTable.next(key);
            key = next.arg1();
            if (key.isnil()) break;
            
            LuaValue bindingData = next.arg(2);
            if (bindingData.istable()) {
                LuaTable binding = bindingData.checktable();
                
                String comboKey = key.tojstring();
                KeyBinding keyBinding = new KeyBinding();
                keyBinding.setCombo(comboKey);
                
                // Extract spell name
                LuaValue spellName = binding.get("SpellName");
                if (!spellName.isnil()) {
                    keyBinding.setSpellName(spellName.tojstring());
                }
                
                // Extract spell ID
                LuaValue spellId = binding.get("SpellID");
                if (!spellId.isnil()) {
                    keyBinding.setSpellId(spellId.toint());
                }
                
                // Extract button
                LuaValue button = binding.get("Button");
                if (!button.isnil()) {
                    keyBinding.setButton(button.toint());
                }
                
                // Extract target type
                LuaValue targetType = binding.get("TargetType");
                if (!targetType.isnil()) {
                    keyBinding.setTargetType(targetType.tojstring());
                }
                
                config.addBinding(keyBinding);
            }
        }
        
        return config;
    }
    
    /**
     * Gets all character names configured in HealBot.
     * 
     * @return Set of character names
     */
    public Set<String> getCharacters() {
        Set<String> characters = new HashSet<>();
        
        if (healbotConfig == null) {
            return characters;
        }
        
        LuaValue spellsValue = healbotConfig.get("Spells");
        if (!spellsValue.istable()) {
            return characters;
        }
        
        LuaTable spells = spellsValue.checktable();
        LuaValue key = LuaValue.NIL;
        while (true) {
            Varargs next = spells.next(key);
            key = next.arg1();
            if (key.isnil()) break;
            characters.add(key.tojstring());
        }
        
        return characters;
    }
    
    /**
     * Configuration for key combinations.
     */
    public static class KeyComboConfig {
        private List<KeyBinding> bindings = new ArrayList<>();
        
        public void addBinding(KeyBinding binding) {
            bindings.add(binding);
        }
        
        public List<KeyBinding> getBindings() {
            return bindings;
        }
    }
    
    /**
     * Represents a single key binding.
     */
    public static class KeyBinding {
        private String combo;
        private String spellName;
        private Integer spellId;
        private Integer button;
        private String targetType;
        
        public String getCombo() {
            return combo;
        }
        
        public void setCombo(String combo) {
            this.combo = combo;
        }
        
        public String getSpellName() {
            return spellName;
        }
        
        public void setSpellName(String spellName) {
            this.spellName = spellName;
        }
        
        public Integer getSpellId() {
            return spellId;
        }
        
        public void setSpellId(Integer spellId) {
            this.spellId = spellId;
        }
        
        public Integer getButton() {
            return button;
        }
        
        public void setButton(Integer button) {
            this.button = button;
        }
        
        public String getTargetType() {
            return targetType;
        }
        
        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }
        
        @Override
        public String toString() {
            return String.format("KeyBinding[combo=%s, spell=%s (ID:%d), button=%d, target=%s]",
                combo, spellName, spellId, button, targetType);
        }
    }
}
