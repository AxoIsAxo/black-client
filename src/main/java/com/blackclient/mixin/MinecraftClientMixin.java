package com.blackclient.mixin;

import com.blackclient.BlackClient;
import com.blackclient.config.Config;
import com.blackclient.gui.HackMenuScreen;
import com.blackclient.hack.HackManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.InputStream;

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
            blackclient$checkTitleLogo(mc);
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

    /** Diagnostic: logs whether the title-logo resource exists and decodes. */
    @Unique
    private static void blackclient$checkTitleLogo(MinecraftClient mc) {
        Identifier logo = Identifier.of("blackclient", "textures/gui/title/logo.png");
        try {
            boolean present = mc.getResourceManager().getResource(logo).isPresent();
            BlackClient.LOGGER.info("[BlackClient] title logo resource present: {}", present);
            if (present) {
                try (InputStream in = mc.getResourceManager().getResource(logo).get().getInputStream()) {
                    NativeImage image = NativeImage.read(in);
                    BlackClient.LOGGER.info("[BlackClient] title logo decodes OK: {}x{}", image.getWidth(), image.getHeight());
                } catch (Exception e) {
                    BlackClient.LOGGER.error("[BlackClient] title logo decode FAILED", e);
                }
            }
        } catch (Exception e) {
            BlackClient.LOGGER.error("[BlackClient] title logo resource check failed", e);
        }
    }
}
