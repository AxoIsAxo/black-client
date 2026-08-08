package com.blackclient.gui;

import com.blackclient.config.Config;
import com.blackclient.hack.Hack;
import com.blackclient.hack.HackManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/**
 * Main menu: one row per hack with an "open settings" button and an
 * ON/OFF toggle. Opened by pressing right Shift in game.
 */
public class HackMenuScreen extends Screen {

    public HackMenuScreen() {
        super(Text.literal("Black Client"));
    }

    @Override
    protected void init() {
        int x = width / 2 - 100;
        int y = height / 2 - 60;

        for (Hack hack : HackManager.INSTANCE.getHacks()) {
            addDrawableChild(ButtonWidget.builder(
                            Text.literal(hack.getName()),
                            button -> client.setScreen(new HackSettingsScreen(hack)))
                    .dimensions(x, y, 130, 20)
                    .build());

            addDrawableChild(ButtonWidget.builder(
                            enabledText(hack),
                            button -> {
                                hack.toggle();
                                button.setMessage(enabledText(hack));
                                Config.INSTANCE.save();
                            })
                    .dimensions(x + 135, y, 65, 20)
                    .build());
            y += 26;
        }

        addDrawableChild(ButtonWidget.builder(
                        Text.literal("Close"),
                        button -> close())
                .dimensions(x, y + 8, 200, 20)
                .build());
    }

    private static Text enabledText(Hack hack) {
        return Text.literal(hack.isEnabled() ? "\u00A7aON" : "\u00A7cOFF");
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
