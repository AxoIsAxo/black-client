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
 * Darkens the title screen panorama. The title screen's own
 * renderBackground is empty, so the darkening has to happen here, after
 * everything else has been drawn. (No glitch effect: glitch is reserved for
 * buttons on hover.)
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void blackclient$titleDarken(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        GuiUtil.rect(context, 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), 0x88000000);
    }
}
