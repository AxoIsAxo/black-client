package com.blackclient.gui;

import com.blackclient.config.Config;
import com.blackclient.hack.setting.NumberSetting;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.text.Text;

import java.util.Locale;

/**
 * Vanilla {@link SliderWidget} bound to a {@link NumberSetting}.
 */
public class NumberSlider extends SliderWidget {

    private final NumberSetting setting;

    public NumberSlider(int x, int y, int width, int height, NumberSetting setting) {
        super(x, y, width, height, Text.literal(setting.getName()), normalize(setting));
        this.setting = setting;
        updateMessage();
    }

    private static double normalize(NumberSetting setting) {
        double range = setting.getMax() - setting.getMin();
        return range <= 0 ? 0 : (setting.getValue() - setting.getMin()) / range;
    }

    @Override
    protected void updateMessage() {
        if (setting == null) {
            return;
        }
        String value = setting.getStep() >= 1
                ? String.valueOf(setting.getValueInt())
                : String.format(Locale.ROOT, "%.1f", setting.getValue());
        setMessage(Text.literal(setting.getName() + ": " + value));
    }

    @Override
    protected void applyValue() {
        if (setting == null) {
            return;
        }
        double value = setting.getMin() + (setting.getMax() - setting.getMin()) * this.value;
        setting.setValue(value);
        Config.INSTANCE.save();
    }
}
