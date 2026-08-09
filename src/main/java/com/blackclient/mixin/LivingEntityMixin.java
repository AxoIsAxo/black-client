package com.blackclient.mixin;

import com.blackclient.hack.HackManager;
import com.blackclient.hack.impl.ElytraFlight;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Replaces the vanilla elytra physics for the local player while ElytraFlight
 * is active. The fall-flying branch of {@code travel} computes its own
 * gravity/boost and applies drag every tick, which fights the hack's velocity;
 * instead that branch is skipped entirely and the entity moves purely with the
 * velocity ElytraFlight sets at the end of {@code tickMovement}.
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void blackclient$elytraTravel(Vec3d movementInput, CallbackInfo ci) {
        if ((Object) this != MinecraftClient.getInstance().player) {
            return;
        }
        ElytraFlight elytraFlight = HackManager.INSTANCE.get(ElytraFlight.class);
        if (elytraFlight == null || !elytraFlight.isEnabled()) {
            return;
        }
        LivingEntity self = (LivingEntity) (Object) this;
        if (!self.isFallFlying() || self.isOnGround()) {
            return;
        }

        // Replace limitFallDistance(): flying must not accumulate fall damage.
        self.fallDistance = 0.0F;
        self.move(MovementType.SELF, self.getVelocity());
        ci.cancel();
    }
}
