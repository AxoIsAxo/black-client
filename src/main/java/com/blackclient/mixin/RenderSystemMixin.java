package com.blackclient.mixin;

import com.blackclient.util.ScreenshotDeferral;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.ScreenshotRecorder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Runs a deferred in-game screenshot at the start of the next frame swap, by
 * which time the hack menu was skipped on that frame's render.
 */
@Mixin(RenderSystem.class)
public abstract class RenderSystemMixin {

    @Inject(method = "flipFrame", at = @At("HEAD"))
    private static void blackclient$flushDeferredScreenshot(long window, CallbackInfo ci) {
        if (!ScreenshotDeferral.pending) {
            return;
        }
        ScreenshotDeferral.pending = false;
        ScreenshotDeferral.running = true;
        try {
            ScreenshotRecorder.saveScreenshot(
                    ScreenshotDeferral.directory,
                    ScreenshotDeferral.framebuffer,
                    ScreenshotDeferral.callback);
        } finally {
            ScreenshotDeferral.running = false;
            ScreenshotDeferral.directory = null;
            ScreenshotDeferral.framebuffer = null;
            ScreenshotDeferral.callback = null;
        }
    }
}
