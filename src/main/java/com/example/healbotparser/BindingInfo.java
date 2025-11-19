package com.example.healbotparser;

/**
 * Simple data class to hold binding information for Thymeleaf templates.
 */
public class BindingInfo {
    private final String button;
    private final String modifier;
    private final String spell;
    private final String filename;

    public BindingInfo(String button, String modifier, String spell, String filename) {
        this.button = button;
        this.modifier = modifier;
        this.spell = spell;
        this.filename = filename;
    }

    public String getButton() { return button; }
    public String getModifier() { return modifier; }
    public String getSpell() { return spell; }
    public String getFilename() { return filename; }
}