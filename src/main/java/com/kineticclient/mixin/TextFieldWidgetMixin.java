package com.kineticclient.mixin;

import com.kineticclient.gui.GuiUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces the vanilla text-field background with KineticsLabs Neobrutalist input box.
 */
@Mixin(TextFieldWidget.class)
public abstract class TextFieldWidgetMixin {

    @Redirect(method = "renderWidget", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V"))
    private void kinetic$textFieldBackground(DrawContext context, Identifier texture, int x, int y, int width, int height) {
        TextFieldWidget field = (TextFieldWidget) (Object) this;
        boolean focused = field.isFocused();
        GuiUtil.brutalBox(context, x, y, width, height, 0xFF000000, focused ? GuiUtil.ACCENT_CYAN : GuiUtil.BORDER_SLATE, 0xFF000000, 1);
    }
}
