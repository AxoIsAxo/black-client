package com.blackclient.mixin;

import com.blackclient.gui.GuiUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces the vanilla button texture with the dark/glitchy style. This mixin
 * sits on {@link PressableWidget}, which every button-type widget extends
 * (ButtonWidget, CyclingButtonWidget, ...), so all vanilla menus inherit it.
 * The label text is still drawn by the vanilla code afterwards.
 */
@Mixin(PressableWidget.class)
public abstract class PressableWidgetMixin {

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void blackclient$buttonBackground(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        PressableWidget widget = (PressableWidget) (Object) this;
        boolean selected = widget.isSelected();
        boolean active = widget.active;

        GuiUtil.rect(context, x, y, width, height, active ? (selected ? 0xFF171722 : 0xFF10101A) : GuiUtil.DISABLED);
        GuiUtil.border(context, x, y, width, height, selected ? GuiUtil.ACCENT : GuiUtil.BORDER);
        if (selected) {
            GuiUtil.glitchBars(context, x + 1, y + 1, width - 2, height - 2, System.currentTimeMillis(), 41);
        }
    }
}
