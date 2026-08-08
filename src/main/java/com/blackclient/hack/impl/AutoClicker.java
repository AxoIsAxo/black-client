package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.BoolSetting;
import com.blackclient.hack.setting.ModeSetting;
import com.blackclient.hack.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

/**
 * Clicks at a fixed rate. Left/right/Both, and can require the mouse button
 * to actually be held down.
 */
public class AutoClicker extends Hack {

    private final NumberSetting cps = add(new NumberSetting("CPS", 12, 1, 30, 1));
    private final ModeSetting mode = add(new ModeSetting("Mode", "Left", "Left", "Right", "Both"));
    private final BoolSetting hold = add(new BoolSetting("Hold to click", true));

    private long lastLeftClick;
    private long lastRightClick;

    public AutoClicker() {
        super("AutoClicker", "Clicks automatically at a configurable rate");
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.currentScreen != null) {
            return;
        }

        long now = System.currentTimeMillis();
        long interval = (long) (1000.0 / cps.getValue());

        boolean wantLeft = mode.getValue().equals("Left") || mode.getValue().equals("Both");
        boolean wantRight = mode.getValue().equals("Right") || mode.getValue().equals("Both");

        if (wantLeft && (!hold.getValue() || isMouseDown(GLFW.GLFW_MOUSE_BUTTON_LEFT))
                && now - lastLeftClick >= interval) {
            lastLeftClick = now;
            clickLeft(mc);
        }
        if (wantRight && (!hold.getValue() || isMouseDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT))
                && now - lastRightClick >= interval) {
            lastRightClick = now;
            clickRight(mc);
        }
    }

    private static boolean isMouseDown(int button) {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc.getWindow() != null
                && GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), button) == GLFW.GLFW_PRESS;
    }

    private static void clickLeft(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        ClientPlayerInteractionManager interaction = mc.interactionManager;
        if (player == null || interaction == null) {
            return;
        }
        HitResult target = mc.crosshairTarget;
        if (target instanceof EntityHitResult entityHit && entityHit.getEntity() != null) {
            interaction.attackEntity(player, entityHit.getEntity());
        } else if (target instanceof BlockHitResult blockHit) {
            interaction.attackBlock(blockHit.getBlockPos(), blockHit.getSide());
        }
        player.swingHand(Hand.MAIN_HAND);
    }

    private static void clickRight(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        ClientPlayerInteractionManager interaction = mc.interactionManager;
        if (player == null || interaction == null) {
            return;
        }
        interaction.interactItem(player, Hand.MAIN_HAND);
        player.swingHand(Hand.MAIN_HAND);
    }
}
