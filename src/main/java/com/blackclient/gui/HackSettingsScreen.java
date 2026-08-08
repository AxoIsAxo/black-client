package com.blackclient.gui;

import com.blackclient.config.Config;
import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.BoolSetting;
import com.blackclient.hack.setting.ModeSetting;
import com.blackclient.hack.setting.NumberSetting;
import com.blackclient.hack.setting.Setting;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Per-hack settings screen: master toggle plus one widget per setting
 * (toggle button for booleans, slider for numbers, cycle button for modes).
 */
public class HackSettingsScreen extends Screen {

    private final Hack hack;

    public HackSettingsScreen(Hack hack) {
        super(Text.literal(hack.getName()));
        this.hack = hack;
    }

    @Override
    protected void init() {
        int x = width / 2 - 100;
        int y = 40;

        addDrawableChild(ButtonWidget.builder(
                        enabledText(),
                        button -> {
                            hack.toggle();
                            button.setMessage(enabledText());
                            Config.INSTANCE.save();
                        })
                .dimensions(x, y, 200, 20)
                .build());
        y += 28;

        for (Setting setting : hack.getSettings()) {
            if (setting instanceof BoolSetting bool) {
                addDrawableChild(ButtonWidget.builder(
                                boolText(bool),
                                button -> {
                                    bool.toggle();
                                    button.setMessage(boolText(bool));
                                    Config.INSTANCE.save();
                                })
                        .dimensions(x, y, 200, 20)
                        .build());
            } else if (setting instanceof NumberSetting number) {
                addDrawableChild(new NumberSlider(x, y, 200, 20, number));
            } else if (setting instanceof ModeSetting mode) {
                addDrawableChild(ButtonWidget.builder(
                                modeText(mode),
                                button -> {
                                    mode.cycle();
                                    button.setMessage(modeText(mode));
                                    Config.INSTANCE.save();
                                })
                        .dimensions(x, y, 200, 20)
                        .build());
            }
            y += 28;
        }

        addDrawableChild(ButtonWidget.builder(
                        Text.literal("Back"),
                        button -> client.setScreen(new HackMenuScreen()))
                .dimensions(x, y + 8, 200, 20)
                .build());
    }

    private Text enabledText() {
        return Text.literal("Enabled: " + (hack.isEnabled() ? "\u00A7aON" : "\u00A7cOFF"));
    }

    private static Text boolText(BoolSetting setting) {
        return Text.literal(setting.getName() + ": " + (setting.getValue() ? "\u00A7aON" : "\u00A7cOFF"));
    }

    private static Text modeText(ModeSetting setting) {
        return Text.literal(setting.getName() + ": " + setting.getValue());
    }

    @Override
    public void close() {
        Config.INSTANCE.save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
