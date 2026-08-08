package com.blackclient.mixin;

import com.blackclient.config.Config;
import com.blackclient.gui.HackMenuScreen;
import com.blackclient.hack.HackManager;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hooks the client tick to:
 * <ul>
 *   <li>load the config once the game is up,</li>
 *   <li>toggle the hack menu on right Shift,</li>
 *   <li>drive all enabled hacks.</li>
 * </ul>
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Unique
    private boolean blackclient$wasShiftDown;

    @Unique
    private boolean blackclient$configLoaded;

    @Inject(method = "tick", at = @At("HEAD"))
    private void blackclient$onTick(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();

        if (!blackclient$configLoaded) {
            blackclient$configLoaded = true;
            Config.INSTANCE.load();
        }

        if (mc.getWindow() != null && mc.getWindow().getHandle() != 0L) {
            boolean shiftDown = GLFW.glfwGetKey(mc.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT)
                    == GLFW.GLFW_PRESS;
            if (shiftDown && !blackclient$wasShiftDown) {
                if (mc.currentScreen == null) {
                    mc.setScreen(new HackMenuScreen());
                } else if (mc.currentScreen instanceof HackMenuScreen) {
                    mc.setScreen(null);
                }
            }
            blackclient$wasShiftDown = shiftDown;
        }

        HackManager.INSTANCE.onTick();
    }
}
