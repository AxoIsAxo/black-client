package com.blackclient.mixin;

import com.blackclient.gui.GuiUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces the vanilla text-field background with the dark style; the text,
 * selection highlight and cursor are still drawn by the vanilla code.
 */
@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin {

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void blackclient$textFieldBackground(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        TextFieldWidget field = (TextFieldWidget) (Object) this;
        GuiUtil.rect(context, x, y, width, height, 0xFF10101A);
        GuiUtil.border(context, x, y, width, height, field.isFocused() ? GuiUtil.ACCENT : GuiUtil.BORDER);
    }
}
