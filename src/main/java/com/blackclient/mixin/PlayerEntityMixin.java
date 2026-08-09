package com.blackclient.mixin;

import com.blackclient.hack.HackManager;
import com.blackclient.hack.impl.NoFall;
import com.blackclient.hack.impl.NoHunger;
import com.blackclient.hack.impl.NoKinetic;
import com.blackclient.hack.impl.Reach;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

    /**
     * NoKinetic: cancels kinetic damage (fall + fly-into-wall) at the single
     * damage choke point, so it also protects during ElytraFlight where the
     * NoFall packet spoof is disabled.
     */
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void blackclient$noKinetic(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this != MinecraftClient.getInstance().player) {
            return;
        }
        NoKinetic noKinetic = HackManager.INSTANCE.get(NoKinetic.class);
        if (noKinetic != null && noKinetic.isEnabled()
                && (source.isOf(DamageTypes.FALL) || source.isOf(DamageTypes.FLY_INTO_WALL))) {
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

    /**
     * NoHunger: cancels exhaustion for the local player. The exhaustion → food
     * conversion runs on the player's server-side entity, so the UUID match
     * also covers the single-player integrated-server entity.
     */
    @Inject(method = "addExhaustion", at = @At("HEAD"), cancellable = true)
    private void blackclient$noHunger(float exhaustion, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null) {
            return;
        }
        NoHunger noHunger = HackManager.INSTANCE.get(NoHunger.class);
        if (noHunger != null && noHunger.isEnabled() && ((Entity) (Object) this).getUuid().equals(player.getUuid())) {
            ci.cancel();
        }
    }
}
