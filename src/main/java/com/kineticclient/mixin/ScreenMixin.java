package com.kineticclient.mixin;

import com.kineticclient.gui.GuiUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Darkens the default screen background (pause menu, options, world list, ...)
 * on top of the vanilla darkening/blur.
 */
@Mixin(Screen.class)
public abstract class ScreenMixin {

    @Inject(method = "renderBackground", at = @At("TAIL"))
    private void kinetic$darkenBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        GuiUtil.rect(context, 0, 0, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight(), 0x88000000);
    }
}
