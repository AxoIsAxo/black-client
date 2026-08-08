package com.blackclient.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Small drawing/hit-test helpers and the palette for the custom menu.
 * No vanilla widget styling is used anywhere in this UI.
 */
public final class GuiUtil {

    public static final int BG = 0xD0121219;
    public static final int BORDER = 0xFF2E2E3A;
    public static final int HOVER = 0x28FFFFFF;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int MUTED = 0xFF9A9AA5;
    public static final int ACCENT = 0xFF4FA3FF;
    public static final int ON = 0xFF55FF55;
    public static final int OFF = 0xFFFF5555;
    public static final int TRACK = 0xFF33333C;
    public static final int KNOB = 0xFFCCCCCC;

    private GuiUtil() {
    }

    public record Rect(int x, int y, int w, int h) {
        public boolean contains(int px, int py) {
            return px >= x && px < x + w && py >= y && py < y + h;
        }
    }

    public static void rect(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + h, color);
    }

    public static void border(DrawContext context, int x, int y, int w, int h, int color) {
        context.fill(x, y, x + w, y + 1, color);
        context.fill(x, y + h - 1, x + w, y + h, color);
        context.fill(x, y, x + 1, y + h, color);
        context.fill(x + w - 1, y, x + w, y + h, color);
    }

    public static void text(DrawContext context, TextRenderer renderer, String text, int x, int y, int color) {
        context.drawText(renderer, text, x, y, color, true);
    }

    public static void textCentered(DrawContext context, TextRenderer renderer, String text, int centerX, int y, int color) {
        context.drawText(renderer, text, centerX - renderer.getWidth(text) / 2, y, color, true);
    }

    public static boolean hovered(int mouseX, int mouseY, Rect bounds) {
        return bounds.contains(mouseX, mouseY);
    }
}
