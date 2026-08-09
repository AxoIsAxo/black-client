package com.blackclient.gui;

import com.blackclient.config.Config;
import com.blackclient.hack.setting.NumberSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Locale;

/**
 * Custom-drawn slider bound to a {@link NumberSetting}: label with the current
 * value, a track with fill, and a draggable knob. No vanilla widgets.
 */
public class CustomSlider {

    private final NumberSetting setting;
    private GuiUtil.Rect bounds;
    private boolean dragging;

    public CustomSlider(NumberSetting setting) {
        this.setting = setting;
    }

    public void setBounds(GuiUtil.Rect bounds) {
        this.bounds = bounds;
    }

    public GuiUtil.Rect getBounds() {
        return bounds;
    }

    public boolean isDragging() {
        return dragging;
    }

    private double ratio() {
        double range = setting.getMax() - setting.getMin();
        return range <= 0 ? 0 : (setting.getValue() - setting.getMin()) / range;
    }

    private void setFromMouse(double mouseX) {
        if (bounds == null) {
            return;
        }
        double t = (mouseX - bounds.x()) / bounds.w();
        t = Math.max(0.0, Math.min(1.0, t));
        setting.setValue(setting.getMin() + (setting.getMax() - setting.getMin()) * t);
        Config.INSTANCE.save();
    }

    public void render(DrawContext context, TextRenderer renderer) {
        if (bounds == null) {
            return;
        }
        int x = bounds.x();
        int y = bounds.y();
        int w = bounds.w();

        GuiUtil.text(context, renderer, setting.getName() + ": " + formatValue(), x, y, GuiUtil.TEXT);

        int trackY = y + 13;
        GuiUtil.rect(context, x, trackY, w, 4, GuiUtil.TRACK);
        int fillWidth = (int) (w * ratio());
        if (fillWidth > 0) {
            GuiUtil.rect(context, x, trackY, fillWidth, 4, GuiUtil.ACCENT);
        }
        int knobX = x + fillWidth - 1;
        GuiUtil.rect(context, knobX - 2, trackY - 2, 4, 8, GuiUtil.KNOB);
        GuiUtil.glitchBars(context, x, trackY - 1, Math.max(fillWidth, 8), 6, System.currentTimeMillis(), 31);
    }

    public void onClick(double mouseX, int button) {
        if (button == 0) {
            setFromMouse(mouseX);
            dragging = true;
        }
    }

    public void onDrag(double mouseX) {
        if (dragging) {
            setFromMouse(mouseX);
        }
    }

    public void onRelease() {
        dragging = false;
    }

    private String formatValue() {
        return setting.getStep() >= 1
                ? String.valueOf(setting.getValueInt())
                : String.format(Locale.ROOT, "%.1f", setting.getValue());
    }
}
