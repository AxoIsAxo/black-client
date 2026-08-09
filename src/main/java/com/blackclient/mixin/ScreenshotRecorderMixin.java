package com.blackclient.mixin;

import com.blackclient.gui.HackMenuScreen;
import com.blackclient.gui.HackSettingsScreen;
import com.blackclient.hack.HackManager;
import com.blackclient.hack.impl.Stealth;
import com.blackclient.util.ScreenshotDeferral;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.function.Consumer;

/**
 * The F2 screenshot (3-arg {@link ScreenshotRecorder#saveScreenshot}) reads
 * the framebuffer after the frame — including the hack menu — was rendered.
 * When one of our menu screens is open, defer the capture by one frame: the
 * menu is skipped on the next frame and {@link RenderSystemMixin} then runs
 * the real capture on a framebuffer without it.
 */
@Mixin(ScreenshotRecorder.class)
public abstract class ScreenshotRecorderMixin {

    @Inject(method = "saveScreenshot(Ljava/io/File;Lnet/minecraft/client/gl/Framebuffer;Ljava/util/function/Consumer;)V", at = @At("HEAD"), cancellable = true)
    private static void blackclient$deferScreenshot(File gameDirectory, Framebuffer framebuffer, Consumer<Text> messageReceiver, CallbackInfo ci) {
        if (ScreenshotDeferral.running) {
            return; // our own deferred capture — let it through
        }
        Stealth stealth = HackManager.INSTANCE.get(Stealth.class);
        if (stealth == null || !stealth.isEnabled() || !stealth.f2Screenshots()) {
            return; // anti-capture for in-game screenshots disabled — capture normally
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        Screen screen = mc.currentScreen;
        if (!(screen instanceof HackMenuScreen || screen instanceof HackSettingsScreen)) {
            return; // nothing capture-sensitive open — take the screenshot normally
        }
        ScreenshotDeferral.pending = true;
        ScreenshotDeferral.directory = gameDirectory;
        ScreenshotDeferral.framebuffer = framebuffer;
        ScreenshotDeferral.callback = messageReceiver;
        ci.cancel();
    }
}
