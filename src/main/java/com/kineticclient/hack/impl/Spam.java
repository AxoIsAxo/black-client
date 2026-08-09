package com.kineticclient.hack.impl;

import com.kineticclient.hack.Hack;
import com.kineticclient.hack.setting.BoolSetting;
import com.kineticclient.hack.setting.NumberSetting;
import com.kineticclient.hack.setting.StringSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.Random;

/**
 * Sends a configured chat message every interval. Pauses while any screen
 * (chat, menu, ...) is open.
 */
public class Spam extends Hack {

    private final StringSetting message = add(new StringSetting("Message", ""));
    private final NumberSetting interval = add(new NumberSetting("Interval (s)", 3.0, 0.5, 30.0, 0.5));
    private final BoolSetting randomSuffix = add(new BoolSetting("Random suffix", false));

    private long lastSent;
    private final Random random = new Random();

    public Spam() {
        super("Spam", "Sends a chat message every interval");
    }

    @Override
    public void onEnable() {
        lastSent = 0; // send the first message immediately
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || mc.currentScreen != null || mc.getNetworkHandler() == null) {
            return;
        }
        String content = message.getValue();
        if (content == null || content.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        long intervalMs = (long) (interval.getValue() * 1000.0);
        if (now - lastSent >= intervalMs) {
            lastSent = now;
            String toSend = randomSuffix.getValue() ? content + " " + randomSuffix() : content;
            mc.getNetworkHandler().sendChatMessage(toSend);
        }
    }

    private String randomSuffix() {
        int length = 2 + random.nextInt(2);
        StringBuilder suffix = new StringBuilder();
        for (int i = 0; i < length; i++) {
            suffix.append((char) ('a' + random.nextInt(26)));
        }
        return suffix.toString();
    }
}
