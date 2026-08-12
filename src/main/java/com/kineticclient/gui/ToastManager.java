package com.kineticclient.gui;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Toast Notification system matching KineticsLabs Neobrutalist toasts.
 */
public final class ToastManager {

    public static final ToastManager INSTANCE = new ToastManager();

    public enum ToastType {
        CYAN(0xFF00F0FF),
        PURPLE(0xFFA855F7),
        EMERALD(0xFF10B981),
        WHITE(0xFFFFFFFF);

        private final int color;

        ToastType(int color) {
            this.color = color;
        }

        public int getColor() {
            return color;
        }
    }

    public static class ToastItem {
        private final String message;
        private final ToastType type;
        private final long createdAt;
        private final long durationMs;

        public ToastItem(String message, ToastType type, long durationMs) {
            this.message = message;
            this.type = type;
            this.createdAt = System.currentTimeMillis();
            this.durationMs = durationMs;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() - createdAt > durationMs;
        }

        public float getProgress() {
            long elapsed = System.currentTimeMillis() - createdAt;
            return Math.min(1.0f, (float) elapsed / durationMs);
        }

        public float getAlpha() {
            long elapsed = System.currentTimeMillis() - createdAt;
            if (elapsed < 200) {
                return (float) elapsed / 200.0f;
            } else if (elapsed > durationMs - 300) {
                return (float) (durationMs - elapsed) / 300.0f;
            }
            return 1.0f;
        }
    }

    private final List<ToastItem> toasts = new ArrayList<>();

    private ToastManager() {
    }

    public synchronized void show(String message, ToastType type) {
        toasts.add(new ToastItem(message, type, 3200));
        // Keep max 5 active toasts
        while (toasts.size() > 5) {
            toasts.remove(0);
        }
    }

    public synchronized void render(DrawContext context, TextRenderer textRenderer, int screenWidth, int screenHeight) {
        if (toasts.isEmpty()) return;

        Iterator<ToastItem> it = toasts.iterator();
        long now = System.currentTimeMillis();
        while (it.hasNext()) {
            if (it.next().isExpired()) {
                it.remove();
            }
        }

        int startY = screenHeight - 24;
        for (int i = toasts.size() - 1; i >= 0; i--) {
            ToastItem toast = toasts.get(i);
            int textWidth = textRenderer.getWidth(toast.message);
            int toastW = textWidth + 24;
            int toastH = 18;
            int toastX = screenWidth - toastW - 12;
            int toastY = startY - toastH;

            float alpha = toast.getAlpha();
            int alphaInt = (int) (alpha * 255);
            if (alphaInt <= 0) continue;

            int bg = (Math.min(alphaInt, 0xF0) << 24) | 0x00060913;
            int border = (alphaInt << 24) | (toast.type.getColor() & 0x00FFFFFF);
            int shadow = (alphaInt << 24) | (toast.type.getColor() & 0x00FFFFFF);

            // Brutalist Toast Box with colored offset shadow
            GuiUtil.brutalBox(context, toastX, toastY, toastW, toastH, bg, border, shadow, 2);

            // Blinking status dot
            boolean dotOn = ((now / 350) % 2) == 0;
            int dotColor = dotOn ? border : (alphaInt << 24) | 0x00475569;
            GuiUtil.rect(context, toastX + 5, toastY + 6, 4, 4, dotColor);

            // Toast Message Text
            int textColor = (alphaInt << 24) | 0x00FFFFFF;
            GuiUtil.text(context, textRenderer, toast.message, toastX + 14, toastY + 5, textColor);

            startY -= (toastH + 6);
        }
    }
}
