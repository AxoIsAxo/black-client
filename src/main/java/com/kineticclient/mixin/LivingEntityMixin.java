package com.kineticclient.mixin;

import com.kineticclient.hack.HackManager;
import com.kineticclient.hack.impl.ElytraFlight;
import com.kineticclient.hack.impl.HighJump;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Patches for living entity physics.
 *
 * <ul>
 *   <li>ElytraFlight: replaces vanilla elytra physics for the local player.</li>
 *   <li>HighJump: modifies the jump velocity for the local player.</li>
 * </ul>
 */
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    private void kinetic$elytraTravel(Vec3d movementInput, CallbackInfo ci) {
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

    @Inject(method = "getJumpVelocity(F)F", at = @At("RETURN"), cancellable = true)
    private void kinetic$highJumpVelocity(float strength, CallbackInfoReturnable<Float> cir) {
        if ((Object) this != MinecraftClient.getInstance().player) {
            return;
        }
        HighJump highJump = HackManager.INSTANCE.get(HighJump.class);
        if (highJump != null && highJump.isEnabled()) {
            cir.setReturnValue(highJump.getEffectiveJumpVelocity(cir.getReturnValueF()));
        }
    }
}
