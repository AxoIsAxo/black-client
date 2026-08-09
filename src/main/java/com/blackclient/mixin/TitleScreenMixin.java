package com.blackclient.mixin;

import com.blackclient.gui.GuiUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Darkens the title screen panorama and overlays glitch bars + scanlines.
 * The title screen's own renderBackground is empty, so the darkening has to
 * happen here, after everything else has been drawn.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void blackclient$titleGlitch(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();

        GuiUtil.rect(context, 0, 0, width, height, 0x88000000);
        for (int y = 0; y < height; y += 4) {
            GuiUtil.rect(context, 0, y, width, 1, 0x0F000000);
        }
        GuiUtil.glitchBars(context, 0, 0, width, height, System.currentTimeMillis(), 61);
    }
}
