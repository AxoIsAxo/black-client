package com.kineticclient.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * KineticsLabs Neobrutalist & Liquid Glass UI Toolkit.
 */
public final class GuiUtil {

    // Backgrounds & Surfaces
    public static final int BG_BLACK = 0xFF06070C;
    public static final int BG_DARK = 0xFF0A0D18;
    public static final int BG_NAVY = 0xFF0E1326;
    public static final int BG_PURPLE_DEEP = 0xFF13102B;
    public static final int BG_CARD = 0xFF060913;
    public static final int BG_CARD_HOVER = 0xFF0C1122;
    public static final int SURFACE_GLASS = 0xE60C101E;

    // Borders
    public static final int BORDER_DARK = 0xFF1E293B;
    public static final int BORDER_SLATE = 0xFF334155;
    public static final int BORDER_MUTED = 0xFF475569;

    // Accents
    public static final int ACCENT_CYAN = 0xFF00F0FF;
    public static final int ACCENT_PURPLE = 0xFFA855F7;
    public static final int ACCENT_EMERALD = 0xFF10B981;
    public static final int ACCENT_INDIGO = 0xFF6366F1;
    public static final int ACCENT_WHITE = 0xFFFFFFFF;

    // Typography
    public static final int TEXT_WHITE = 0xFFFFFFFF;
    public static final int TEXT_SLATE = 0xFF94A3B8;
    public static final int TEXT_MUTED = 0xFF64748B;
    public static final int TEXT_BLACK = 0xFF000000;

    // States
    public static final int ON = 0xFF00F0FF;
    public static final int OFF = 0xFF64748B;
    public static final int HOVER = 0x1AFFFFFF;
    public static final int DISABLED = 0xFF1E293B;

    // Legacy aliases
    public static final int BG = BG_BLACK;
    public static final int BORDER = BORDER_DARK;
    public static final int TEXT = TEXT_WHITE;
    public static final int MUTED = TEXT_MUTED;
    public static final int ACCENT = ACCENT_CYAN;
    public static final int ACCENT2 = ACCENT_PURPLE;
    public static final int TRACK = 0xFF000000;
    public static final int KNOB = ACCENT_CYAN;

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
        border(context, x, y, w, h, 1, color);
    }

    public static void border(DrawContext context, int x, int y, int w, int h, int thickness, int color) {
        // Top
        context.fill(x, y, x + w, y + thickness, color);
        // Bottom
        context.fill(x, y + h - thickness, x + w, y + h, color);
        // Left
        context.fill(x, y + thickness, x + thickness, y + h - thickness, color);
        // Right
        context.fill(x + w - thickness, y + thickness, x + w, y + h - thickness, color);
    }

    /**
     * Renders a Neobrutalist card with solid fill, thick border, and offset drop shadow.
     */
    public static void brutalBox(DrawContext context, int x, int y, int w, int h, int bgColor, int borderColor, int shadowColor, int offset) {
        // Drop shadow (offset to bottom right)
        if (offset > 0 && (shadowColor & 0xFF000000) != 0) {
            context.fill(x + offset, y + offset, x + w + offset, y + h + offset, shadowColor);
        }
        // Background fill
        context.fill(x, y, x + w, y + h, bgColor);
        // Border outline
        border(context, x, y, w, h, 1, borderColor);
    }

    public static void brutalBoxThick(DrawContext context, int x, int y, int w, int h, int bgColor, int borderColor, int shadowColor, int offset) {
        if (offset > 0 && (shadowColor & 0xFF000000) != 0) {
            context.fill(x + offset, y + offset, x + w + offset, y + h + offset, shadowColor);
        }
        context.fill(x, y, x + w, y + h, bgColor);
        border(context, x, y, w, h, 2, borderColor);
    }

    public static void tagPill(DrawContext context, TextRenderer renderer, String text, int x, int y, int bgColor, int textColor, int borderColor) {
        int textWidth = renderer.getWidth(text);
        int w = textWidth + 8;
        int h = 12;
        brutalBox(context, x, y, w, h, bgColor, borderColor, 0xFF000000, 2);
        context.drawText(renderer, text, x + 4, y + 2, textColor, false);
    }

    public static void keyPill(DrawContext context, TextRenderer renderer, String keyText, int x, int y, boolean active) {
        int textWidth = renderer.getWidth(keyText);
        int w = textWidth + 6;
        int h = 11;
        int border = active ? ACCENT_CYAN : ACCENT_PURPLE;
        brutalBox(context, x, y, w, h, 0xFF0F172A, border, 0xFF000000, 1);
        context.drawText(renderer, keyText, x + 3, y + 2, TEXT_WHITE, false);
    }

    public static void toggleButton(DrawContext context, TextRenderer renderer, int x, int y, int w, int h, boolean enabled, boolean hovered) {
        int bg = enabled ? ACCENT_CYAN : (hovered ? 0xFF1E293B : 0xFF0F172A);
        int textCol = enabled ? TEXT_BLACK : (hovered ? TEXT_WHITE : TEXT_MUTED);
        int borderCol = enabled ? 0xFF000000 : (hovered ? BORDER_MUTED : BORDER_SLATE);
        int shadowCol = enabled ? 0xFF000000 : 0xFF000000;

        brutalBox(context, x, y, w, h, bg, borderCol, shadowCol, 1);
        String label = enabled ? "ON" : "OFF";
        int tx = x + (w - renderer.getWidth(label)) / 2;
        int ty = y + (h - 8) / 2;
        context.drawText(renderer, label, tx, ty, textCol, false);
    }

    public static void text(DrawContext context, TextRenderer renderer, String text, int x, int y, int color) {
        context.drawText(renderer, text, x, y, color, true);
    }

    public static void textCentered(DrawContext context, TextRenderer renderer, String text, int centerX, int y, int color) {
        context.drawText(renderer, text, centerX - renderer.getWidth(text) / 2, y, color, true);
    }

    /** KineticsLabs RGB-split glitch title */
    public static void textGlitch(DrawContext context, TextRenderer renderer, String text, int x, int y) {
        context.drawText(renderer, text, x - 1, y, ACCENT_PURPLE, true);
        context.drawText(renderer, text, x + 1, y, ACCENT_CYAN, true);
        context.drawText(renderer, text, x, y, TEXT_WHITE, true);
    }

    public static void textGlitchCentered(DrawContext context, TextRenderer renderer, String text, int centerX, int y) {
        int x = centerX - renderer.getWidth(text) / 2;
        textGlitch(context, renderer, text, x, y);
    }

    public static void glitchBars(DrawContext context, int x, int y, int w, int h, long time, int seed) {
        Random random = new Random(seed + time / 150);
        int count = 1 + random.nextInt(3);
        for (int i = 0; i < count; i++) {
            int barY = y + random.nextInt(Math.max(1, h));
            int barH = 1 + random.nextInt(2);
            int barW = w / 2 + random.nextInt(w / 2);
            int offset = random.nextInt(9) - 4;
            int color = random.nextBoolean() ? 0x22A855F7 : 0x2200F0FF;
            rect(context, x + offset, barY, barW, barH, color);
        }
    }

    public static boolean hovered(int mouseX, int mouseY, Rect bounds) {
        return bounds != null && bounds.contains(mouseX, mouseY);
    }

    // ----------------------------------------------------
    // Animated Particle & Grid Backdrop (matching particles.js)
    // ----------------------------------------------------
    private static class Particle {
        float x, y;
        float vx, vy;
        boolean isCube;
        int color;

        void reset(int width, int height, Random rng) {
            x = rng.nextFloat() * width;
            y = rng.nextFloat() * height;
            vx = (rng.nextFloat() - 0.5f) * 0.4f;
            vy = (rng.nextFloat() - 0.5f) * 0.4f;
            isCube = rng.nextFloat() > 0.75f;
            color = rng.nextBoolean() ? ACCENT_CYAN : ACCENT_PURPLE;
        }

        void update(int width, int height, int mouseX, int mouseY) {
            x += vx;
            y += vy;
            if (x < 0) x = width;
            if (x > width) x = 0;
            if (y < 0) y = height;
            if (y > height) y = 0;

            float dx = mouseX - x;
            float dy = mouseY - y;
            float dist = (float) Math.hypot(dx, dy);
            if (dist < 80 && dist > 0) {
                x -= (dx / dist) * 0.8f;
                y -= (dy / dist) * 0.8f;
            }
        }
    }

    private static final List<Particle> PARTICLES = new ArrayList<>();
    private static int lastW = 0, lastH = 0;

    public static void drawParticleBackground(DrawContext context, int mouseX, int mouseY, int width, int height) {
        // Base dark radial gradient backdrop
        context.fill(0, 0, width, height, BG_BLACK);
        context.fillGradient(0, 0, width, height, 0xAA13102B, 0xEE06070C);

        // Grid lines (subtle blueprint grid)
        int gridSize = 32;
        int gridColor = 0x121E293B;
        for (int x = 0; x < width; x += gridSize) {
            context.fill(x, 0, x + 1, height, gridColor);
        }
        for (int y = 0; y < height; y += gridSize) {
            context.fill(0, y, width, y + 1, gridColor);
        }

        // Initialize particles if needed
        if (PARTICLES.isEmpty() || lastW != width || lastH != height) {
            lastW = width;
            lastH = height;
            PARTICLES.clear();
            Random rng = new Random(42);
            int count = Math.min(35, Math.max(15, width / 20));
            for (int i = 0; i < count; i++) {
                Particle p = new Particle();
                p.reset(width, height, rng);
                PARTICLES.add(p);
            }
        }

        // Update and draw particles
        for (int i = 0; i < PARTICLES.size(); i++) {
            Particle p = PARTICLES.get(i);
            p.update(width, height, mouseX, mouseY);

            // Connect nearby particles with subtle lines
            for (int j = i + 1; j < PARTICLES.size(); j++) {
                Particle p2 = PARTICLES.get(j);
                float dx = p.x - p2.x;
                float dy = p.y - p2.y;
                float dist = (float) Math.hypot(dx, dy);
                if (dist < 45) {
                    int alpha = (int) (0x33 * (1.0f - dist / 45.0f));
                    int lineCol = (alpha << 24) | (ACCENT_PURPLE & 0x00FFFFFF);
                    context.fill((int) p.x, (int) p.y, (int) p2.x + 1, (int) p.y + 1, lineCol);
                }
            }

            int px = (int) p.x;
            int py = (int) p.y;
            if (p.isCube) {
                border(context, px - 2, py - 2, 4, 4, 0x66000000 | (p.color & 0x00FFFFFF));
            } else {
                rect(context, px - 1, py - 1, 2, 2, 0x88000000 | (p.color & 0x00FFFFFF));
            }
        }
    }
}
