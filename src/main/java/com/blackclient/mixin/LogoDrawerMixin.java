package com.blackclient.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.LogoDrawer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the title-screen logo entirely (the big wordmark and the
 * "JAVA EDITION" line below it).
 */
@Mixin(LogoDrawer.class)
public abstract class LogoDrawerMixin {

    @Inject(method = "draw(Lnet/minecraft/client/gui/DrawContext;IFI)V", at = @At("HEAD"), cancellable = true)
    private void blackclient$hideLogo(DrawContext context, int screenWidth, float alpha, int y, CallbackInfo ci) {
        ci.cancel();
    }
}
