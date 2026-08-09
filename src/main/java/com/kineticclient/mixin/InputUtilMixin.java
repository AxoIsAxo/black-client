package com.kineticclient.mixin;

import com.kineticclient.hack.HackManager;
import com.kineticclient.hack.impl.Keypresser;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Keypresser: while enabled, the configured key reports as pressed everywhere
 * {@link InputUtil#isKeyPressed} is polled (every vanilla key binding), so the
 * game behaves as if the key were physically held. Stops while a screen is
 * open.
 */
@Mixin(InputUtil.class)
public abstract class InputUtilMixin {

    @Inject(method = "isKeyPressed", at = @At("HEAD"), cancellable = true)
    private static void kinetic$holdKey(long handle, int code, CallbackInfoReturnable<Boolean> cir) {
        if (MinecraftClient.getInstance().currentScreen != null) {
            return;
        }
        Keypresser keypresser = HackManager.INSTANCE.get(Keypresser.class);
        if (keypresser != null && keypresser.isEnabled() && keypresser.getHeldKey() != -1
                && keypresser.getHeldKey() == code) {
            cir.setReturnValue(true);
        }
    }
}
