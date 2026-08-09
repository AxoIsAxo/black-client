package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.BoolSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Prevents most hunger drain, also on servers ("AntiHunger"):
 * <ul>
 *   <li>the {@code START_SPRINTING} packet is cancelled so the server never
 *       charges per-tick sprint exhaustion (client still sprints locally),</li>
 *   <li>movement packets pretend to be airborne while standing still so the
 *       server never detects jumps (no jump/sprint-jump exhaustion),</li>
 *   <li>the client's food bar is kept full.</li>
 * </ul>
 * Distance-based drains (walking/swimming), mining and attacks still apply
 * server-side — like all clients this reduces but cannot fully remove hunger
 * on vanilla multiplayer. In single-player the {@code addExhaustion} cancel
 * still applies fully.
 */
public class NoHunger extends Hack {

    private final BoolSetting sprint = add(new BoolSetting("Sprint", true));

    public NoHunger() {
        super("NoHunger", "Prevents most hunger drain (works on servers)");
    }

    public boolean cancelSprint() {
        return sprint.getValue();
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.currentScreen != null) {
            return;
        }
        var hunger = player.getHungerManager();
        hunger.setFoodLevel(20);
        hunger.setSaturationLevel(20.0F);
        hunger.setExhaustion(0.0F);
    }
}
