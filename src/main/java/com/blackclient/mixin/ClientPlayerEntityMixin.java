package com.blackclient.mixin;

import com.blackclient.hack.HackManager;
import com.blackclient.hack.impl.ElytraFlight;
import com.blackclient.hack.impl.NoFall;
import com.blackclient.hack.impl.NoSlowdown;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Client-side patches for the local player entity.
 *
 * <ul>
 *   <li>NoSlowdown: neutralises the using-item movement slowdown in
 *       {@code tickMovement()} (the two 0.2F constants are the only ones in
 *       the method, so a {@code @ModifyConstant} is safe).</li>
 *   <li>NoFall: spoofs onGround=true in the movement packets while falling,
 *       so the server never accumulates fall distance. The methodref owner
 *       in the bytecode is {@code ClientPlayerEntity} even though
 *       {@code isOnGround} is declared on {@code Entity}.</li>
 * </ul>
 */
@Mixin(ClientPlayerEntity.class)
public abstract class ClientPlayerEntityMixin {

    @ModifyConstant(method = "tickMovement", constant = @Constant(floatValue = 0.2F))
    private float blackclient$noSlowdownUsingItems(float original) {
        NoSlowdown noSlowdown = HackManager.INSTANCE.get(NoSlowdown.class);
        if (noSlowdown != null && noSlowdown.isEnabled() && noSlowdown.shouldRemoveItemSlowdown()) {
            return 1.0F;
        }
        return original;
    }

    @Redirect(method = "sendMovementPackets", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isOnGround()Z"))
    private boolean blackclient$noFallOnGround(ClientPlayerEntity player) {
        if (player.isOnGround()) {
            return true;
        }
        NoFall noFall = HackManager.INSTANCE.get(NoFall.class);
        if (noFall == null || !noFall.isEnabled() || !noFall.shouldSpoofOnGround()) {
            return false;
        }
        // Only lie about being on the ground while actually falling through
        // air; riding, elytra flight and swimming keep their real state so we
        // do not disturb those movement modes.
        if (player.hasVehicle() || player.isFallFlying() || player.isTouchingWater()) {
            return false;
        }
        return true;
    }

    /**
     * ElytraFlight: applied at the end of the player's movement tick, after
     * the vanilla elytra physics have run.
     */
    @Inject(method = "tickMovement", at = @At("TAIL"))
    private void blackclient$elytraFlight(CallbackInfo ci) {
        ElytraFlight elytraFlight = HackManager.INSTANCE.get(ElytraFlight.class);
        if (elytraFlight != null && elytraFlight.isEnabled()) {
            elytraFlight.onControlTick((ClientPlayerEntity) (Object) this);
        }
    }
}
