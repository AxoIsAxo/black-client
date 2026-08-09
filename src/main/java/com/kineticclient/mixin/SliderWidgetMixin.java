package com.kineticclient.mixin;

import com.kineticclient.gui.GuiUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces the vanilla slider bar/handle textures with the dark/glitchy style.
 * Both drawGuiTexture calls in {@code renderWidget} are redirected; the two
 * variants are told apart by the texture id (bar vs handle).
 */
@Mixin(SliderWidget.class)
public abstract class SliderWidgetMixin {

    @Shadow
    protected double value;

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void kinetic$sliderRender(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        if (texture.getPath().contains("handle")) {
            // Knob: centered on the vanilla handle position, 10x10.
            int knobSize = 10;
            int knobX = x - (knobSize - 8) / 2;
            int knobY = y + (height - knobSize) / 2;
            GuiUtil.rect(context, knobX, knobY, knobSize, knobSize, GuiUtil.KNOB);
            GuiUtil.border(context, knobX, knobY, knobSize, knobSize, GuiUtil.BG_DARK);
        } else {
            // Track with fill to the current value.
            int trackY = y + height / 2 - 2;
            GuiUtil.rect(context, x, trackY, width, 4, GuiUtil.TRACK);
            int fillWidth = (int) (width * this.value);
            if (fillWidth > 0) {
                GuiUtil.rect(context, x, trackY, fillWidth, 4, GuiUtil.ACCENT);
            }
            int knobX = x + fillWidth - 1;
            GuiUtil.rect(context, knobX - 2, trackY - 2, 4, 8, GuiUtil.KNOB);
        }
    }
}
