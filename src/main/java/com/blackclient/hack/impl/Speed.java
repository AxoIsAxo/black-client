package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;

/**
 * Multiplies the player's base movement speed by changing the client-side
 * {@code GENERIC_MOVEMENT_SPEED} attribute. Applies to walking and sprinting;
 * the original value is restored when the hack is disabled.
 */
public class Speed extends Hack {

    private final NumberSetting multiplier = add(new NumberSetting("Multiplier", 1.5, 1.0, 5.0, 0.1));

    private double originalBase = 0.1;
    private boolean initialized;

    public Speed() {
        super("Speed", "Walk and sprint faster");
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.currentScreen != null) {
            return;
        }
        EntityAttributeInstance attribute = player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
        if (attribute == null) {
            return;
        }
        if (!initialized) {
            originalBase = attribute.getBaseValue();
            initialized = true;
        }
        attribute.setBaseValue(originalBase * multiplier.getValue());
    }

    @Override
    public void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (initialized && mc.player != null) {
            EntityAttributeInstance attribute = mc.player.getAttributeInstance(EntityAttributes.GENERIC_MOVEMENT_SPEED);
            if (attribute != null) {
                attribute.setBaseValue(originalBase);
            }
        }
        initialized = false;
    }
}
