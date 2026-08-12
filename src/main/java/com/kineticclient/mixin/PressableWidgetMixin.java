package com.kineticclient.mixin;

import com.kineticclient.audio.SoundSynthesizer;
import com.kineticclient.gui.GuiUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the vanilla button texture with KineticsLabs Neobrutalist style:
 * Solid black/navy background, crisp category border, offset drop shadow,
 * and tactile mechanical click sounds.
 */
@Mixin(PressableWidget.class)
public abstract class PressableWidgetMixin {

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void kinetic$buttonBackground(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        PressableWidget widget = (PressableWidget) (Object) this;
        boolean selected = widget.isSelected();
        boolean active = widget.active;

        int bg = active ? (selected ? 0xFF0F172A : 0xFF060913) : 0xFF1E293B;
        int border = active ? (selected ? GuiUtil.ACCENT_CYAN : GuiUtil.BORDER_SLATE) : GuiUtil.BORDER_DARK;
        int shadow = active ? (selected ? GuiUtil.ACCENT_PURPLE : 0xFF000000) : 0x00000000;

        GuiUtil.brutalBox(context, x, y, width, height, bg, border, shadow, selected ? 2 : 1);
    }

    @Inject(method = "onClick", at = @At("HEAD"))
    private void kinetic$onClickSound(double mouseX, double mouseY, CallbackInfo ci) {
        SoundSynthesizer.INSTANCE.playMechanical();
    }
}
