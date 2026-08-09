package com.kineticclient.hack;

/**
 * Hack groups shown as collapsible sections in the menu.
 */
public enum HackCategory {
    COMBAT("Combat", 0xFFE74C3C),
    MOVEMENT("Movement", 0xFF3498DB),
    RENDER("Render", 0xFF9B59B6),
    OTHER("Other", 0xFFE67E22);

    private final String name;
    private final int color;

    HackCategory(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public int getColor() {
        return color;
    }
}
