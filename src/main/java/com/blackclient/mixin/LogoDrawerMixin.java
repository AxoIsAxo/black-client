package com.blackclient.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Replaces the big "MINECRAFT" title-screen logo with the custom logo asset
 * ({@code logo.png}, 592x78). It is drawn scaled to the vanilla logo height
 * (44 px) with the aspect ratio preserved and re-centered, so the vanilla
 * "JAVA EDITION" line below keeps its position.
 */
@Mixin(LogoDrawer.class)
public abstract class LogoDrawerMixin {

    private static final Identifier CUSTOM_LOGO = Identifier.of("blackclient", "textures/gui/title/logo.png");
    private static final int LOGO_WIDTH = 592;
    private static final int LOGO_HEIGHT = 78;
    private static final int DRAW_HEIGHT = 44;
    private static final int DRAW_WIDTH = DRAW_HEIGHT * LOGO_WIDTH / LOGO_HEIGHT;

    @Redirect(method = "draw", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIFFIIII)V"))
    private void blackclient$titleLogo(DrawContext context, Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        // x is the left edge of the vanilla 256-wide logo; re-center for our width.
        int left = x + (width - DRAW_WIDTH) / 2;
        context.drawTexture(CUSTOM_LOGO, left, y, 0.0F, 0.0F, DRAW_WIDTH, DRAW_HEIGHT, LOGO_WIDTH, LOGO_HEIGHT);
    }
}
