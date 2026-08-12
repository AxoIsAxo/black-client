package com.kineticclient.hack;

/**
 * Hack categories matching KineticsLabs design system.
 */
public enum HackCategory {
    COMBAT("COMBAT", "⚔", 0xFF00F0FF),
    MOVEMENT("MOVEMENT", "⚡", 0xFFA855F7),
    RENDER("RENDER", "👁", 0xFF10B981),
    WORLD("WORLD", "🌐", 0xFF6366F1),
    OTHER("OTHER", "⚙", 0xFF818CF8);

    private final String name;
    private final String icon;
    private final int color;

    HackCategory(String name, String icon, int color) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public String getIcon() {
        return icon;
    }

    public int getColor() {
        return color;
    }
}
