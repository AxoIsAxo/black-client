package com.blackclient.mixin;

import com.blackclient.gui.GuiUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reworked title screen: no panorama, no logo — instead an almost-black
 * purple background with a purple vignette, and the buttons re-centered
 * vertically since the logo no longer occupies the top of the screen.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    private static final Identifier VIGNETTE = Identifier.ofVanilla("textures/misc/vignette.png");
    private static final int VIGNETTE_SIZE = 192;

    @Inject(method = "render", at = @At("HEAD"))
    private void blackclient$background(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();

        // Almost-black purple base.
        GuiUtil.rect(context, 0, 0, width, height, 0xFF0B0714);

        // Vertical edge darkening (vignette top/bottom).
        context.fillGradient(0, 0, width, height / 2, 0x5504020A, 0x00000000);
        context.fillGradient(0, height / 2, width, height, 0x00000000, 0x5504020A);

        // Radial vignette: the vanilla vignette texture, tinted purple.
        context.setShaderColor(0.30F, 0.18F, 0.45F, 1.0F);
        context.drawTexture(VIGNETTE, 0, 0, 0.0F, 0.0F, width, height, VIGNETTE_SIZE, VIGNETTE_SIZE);
        context.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    private void blackclient$hidePanorama(DrawContext context, float delta, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void blackclient$centerButtons(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int height = mc.getWindow().getScaledHeight();

        // Vanilla lays the button block out starting at height/4 + 48 (below
        // the logo). Without the logo, center the block vertically.
        int vanillaStart = height / 4 + 48;
        int blockHeight = 104; // 3 x 20px buttons + 24 spacing + second row
        int centeredStart = height / 2 - blockHeight / 2;
        int shift = centeredStart - vanillaStart;
        if (shift == 0) {
            return;
        }

        for (Element element : ((TitleScreen) (Object) this).children()) {
            if (element instanceof ClickableWidget widget) {
                widget.setY(widget.getY() + shift);
            }
        }
    }
}
