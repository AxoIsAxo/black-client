package com.kineticclient.mixin;

import com.kineticclient.audio.SoundSynthesizer;
import com.kineticclient.gui.GuiUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the vanilla slider bar/handle textures with KineticsLabs Neobrutalist style.
 */
@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin {

    @Shadow
    protected double value;

    @Unique
    private double kinetic$lastTickVal;

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void kinetic$sliderRender(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        if (texture.getPath().contains("handle")) {
            // Knob: square brutalist knob with black border
            int knobSize = 10;
            int knobX = x - (knobSize - 8) / 2;
            int knobY = y + (height - knobSize) / 2;
            GuiUtil.brutalBox(context, knobX, knobY, knobSize, knobSize, GuiUtil.ACCENT_CYAN, 0xFF000000, 0xFF000000, 1);
        } else {
            // Track with fill to the current value.
            int trackY = y + height / 2 - 2;
            GuiUtil.rect(context, x, trackY, width, 5, 0xFF000000);
            GuiUtil.border(context, x, trackY, width, 5, GuiUtil.BORDER_SLATE);
            int fillWidth = (int) (width * this.value);
            if (fillWidth > 0) {
                GuiUtil.rect(context, x, trackY, fillWidth, 5, GuiUtil.ACCENT_CYAN);
            }
        }
    }

    @Inject(method = "setValue", at = @At("HEAD"))
    private void kinetic$onSetValue(double val, CallbackInfo ci) {
        if (Math.abs(val - kinetic$lastTickVal) > 0.05) {
            kinetic$lastTickVal = val;
            SoundSynthesizer.INSTANCE.playTick();
        }
    }
}
