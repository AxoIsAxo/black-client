package com.kineticclient.gui;

import com.kineticclient.audio.SoundSynthesizer;
import com.kineticclient.config.Config;
import com.kineticclient.hack.setting.NumberSetting;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.Locale;

/**
 * KineticsLabs Neobrutalist custom slider with tactile audio ticks.
 */
public class CustomSlider {

    private final NumberSetting setting;
    private final int accentColor;
    private GuiUtil.Rect bounds;
    private boolean dragging;
    private double lastSoundValue;

    public CustomSlider(NumberSetting setting) {
        this(setting, GuiUtil.ACCENT_CYAN);
    }

    public CustomSlider(NumberSetting setting, int accentColor) {
        this.setting = setting;
        this.accentColor = accentColor;
        this.lastSoundValue = setting.getValue();
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
        int trackX = bounds.x() + 2;
        int trackW = bounds.w() - 4;
        double t = (mouseX - trackX) / (double) trackW;
        t = Math.max(0.0, Math.min(1.0, t));
        setting.setValue(setting.getMin() + (setting.getMax() - setting.getMin()) * t);
        Config.INSTANCE.save();

        if (Math.abs(setting.getValue() - lastSoundValue) >= Math.max(0.1, setting.getStep())) {
            lastSoundValue = setting.getValue();
            SoundSynthesizer.INSTANCE.playTick();
        }
    }

    public void render(DrawContext context, TextRenderer renderer) {
        if (bounds == null) {
            return;
        }
        int x = bounds.x();
        int y = bounds.y();
        int w = bounds.w();
        int h = bounds.h();

        // Control label row: [SETTING NAME] on left, [VALUE] on right in accent color
        GuiUtil.text(context, renderer, setting.getName().toUpperCase(), x + 2, y, GuiUtil.TEXT_SLATE);
        String valStr = formatValue();
        int valWidth = renderer.getWidth(valStr);
        GuiUtil.text(context, renderer, valStr, x + w - valWidth - 2, y, accentColor);

        // Slider track
        int trackY = y + 11;
        int trackH = 5;
        GuiUtil.rect(context, x, trackY, w, trackH, 0xFF000000);
        GuiUtil.border(context, x, trackY, w, trackH, GuiUtil.BORDER_SLATE);

        // Fill
        int fillWidth = (int) (w * ratio());
        if (fillWidth > 0) {
            GuiUtil.rect(context, x, trackY, fillWidth, trackH, accentColor);
        }

        // Neobrutalist Square Knob
        int knobSize = 9;
        int knobX = Math.max(x, Math.min(x + w - knobSize, x + fillWidth - knobSize / 2));
        int knobY = trackY - 2;
        GuiUtil.brutalBox(context, knobX, knobY, knobSize, knobSize, accentColor, 0xFF000000, 0xFF000000, 1);
    }

    public void onClick(double mouseX, int button) {
        if (button == 0) {
            setFromMouse(mouseX);
            dragging = true;
            SoundSynthesizer.INSTANCE.playTick();
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

    public String formatValue() {
        return setting.getStep() >= 1
                ? String.valueOf(setting.getValueInt())
                : String.format(Locale.ROOT, "%.1f", setting.getValue());
    }
}
