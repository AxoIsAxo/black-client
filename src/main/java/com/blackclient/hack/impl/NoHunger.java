package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

/**
 * Prevents hunger from draining.
 *
 * <p>The exhaustion → food conversion is applied on the player's server-side
 * entity. {@link com.blackclient.mixin.PlayerEntityMixin} cancels
 * {@code PlayerEntity.addExhaustion} for the local player (matched by UUID,
 * which also covers the single-player integrated-server entity), and this tick
 * keeps the client's food bar full. On remote servers the bar stays full
 * client-side, but the server still drains hunger.
 */
public class NoHunger extends Hack {

    public NoHunger() {
        super("NoHunger", "Stops hunger from draining");
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
