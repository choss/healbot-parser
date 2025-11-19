package com.example.healbotparser;

/**
 * Simple data class to hold binding information for Thymeleaf templates.
 */
public class BindingInfo {
    private final String button;
    private final String modifier;
    private final String spell;

    public BindingInfo(String button, String modifier, String spell) {
        this.button = button;
        this.modifier = modifier;
        this.spell = spell;
    }

    public String getButton() { return button; }
    public String getModifier() { return modifier; }
    public String getSpell() { return spell; }
}