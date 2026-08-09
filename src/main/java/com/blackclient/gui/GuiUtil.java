package com.blackclient.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Random;

/**
 * Drawing/hit-test helpers and the dark/glitchy palette shared by the custom
 * menu and the vanilla-menu restyle mixins.
 */
public final class GuiUtil {

    public static final int BG = 0xE60A0A12;
    public static final int BG_DARK = 0xFF10101A;
    public static final int BORDER = 0xFF2A2A38;
    public static final int HOVER = 0x26FFFFFF;
    public static final int TEXT = 0xFFE8E8F0;
    public static final int MUTED = 0xFF8A8A99;
    public static final int ACCENT = 0xFF4FE3FF;
    public static final int ACCENT2 = 0xFFFF4FD8;
    public static final int ON = 0xFF55FF88;
    public static final int OFF = 0xFFFF5566;
    public static final int TRACK = 0xFF1E1E28;
    public static final int KNOB = 0xFF4FE3FF;
    public static final int DISABLED = 0xFF1A1A22;

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

    /** RGB-split "glitch" title text: magenta/cyan offset copies behind white. */
    public static void textGlitch(DrawContext context, TextRenderer renderer, String text, int x, int y) {
        context.drawText(renderer, text, x - 1, y, 0xFFFF4FD8, true);
        context.drawText(renderer, text, x + 1, y, 0xFF4FE3FF, true);
        context.drawText(renderer, text, x, y, 0xFFFFFFFF, true);
    }

    public static void textGlitchCentered(DrawContext context, TextRenderer renderer, String text, int centerX, int y) {
        int x = centerX - renderer.getWidth(text) / 2;
        textGlitch(context, renderer, text, x, y);
    }

    /**
     * A few animated horizontal glitch bars inside a region. The pattern is
     * seeded from {@code time} so it changes a few times per second instead
     * of flickering every frame.
     */
    public static void glitchBars(DrawContext context, int x, int y, int w, int h, long time, int seed) {
        Random random = new Random(seed + time / 150);
        int count = 1 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            int barY = y + random.nextInt(Math.max(1, h));
            int barH = 1 + random.nextInt(2);
            int barW = w / 2 + random.nextInt(w / 2);
            int offset = random.nextInt(9) - 4;
            int color = random.nextBoolean() ? 0x22FF4FD8 : 0x224FE3FF;
            rect(context, x + offset, barY, barW, barH, color);
        }
    }

    public static boolean hovered(int mouseX, int mouseY, Rect bounds) {
        return bounds.contains(mouseX, mouseY);
    }
}
