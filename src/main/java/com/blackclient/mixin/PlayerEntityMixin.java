package com.blackclient.mixin;

import com.blackclient.hack.HackManager;
import com.blackclient.hack.impl.NoFall;
import com.blackclient.hack.impl.Reach;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Cancels the client-side fall damage application for the local player while
 * NoFall is active, so landing produces no damage flash, sound or predicted
 * health loss. (The server-side protection is the onGround packet spoof in
 * {@link ClientPlayerEntityMixin}.)
 */
@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {

    @Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
    private void blackclient$noFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this != MinecraftClient.getInstance().player || !damageSource.isOf(DamageTypes.FALL)) {
            return;
        }
        NoFall noFall = HackManager.INSTANCE.get(NoFall.class);
        if (noFall != null && noFall.isEnabled()) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "getBlockInteractionRange", at = @At("RETURN"), cancellable = true)
    private void blackclient$reachBlock(CallbackInfoReturnable<Double> cir) {
        Reach reach = HackManager.INSTANCE.get(Reach.class);
        if (reach != null && reach.isEnabled()) {
            cir.setReturnValue(cir.getReturnValueD() + reach.getExtraRange());
        }
    }

    @Inject(method = "getEntityInteractionRange", at = @At("RETURN"), cancellable = true)
    private void blackclient$reachEntity(CallbackInfoReturnable<Double> cir) {
        Reach reach = HackManager.INSTANCE.get(Reach.class);
        if (reach != null && reach.isEnabled()) {
            cir.setReturnValue(cir.getReturnValueD() + reach.getExtraRange());
        }
    }
}
