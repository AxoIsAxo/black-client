package com.blackclient.mixin;

import com.blackclient.hack.HackManager;
import com.blackclient.hack.impl.Tunneler;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Lets Tunneler take control of the local player's movement. The input is
 * overridden right after {@code KeyboardInput.tick} recomputes it from the
 * key states, i.e. before {@code ClientPlayerEntity} consumes it for travel,
 * so the forced forward movement actually drives the player. This runs only
 * when no screen is open and Tunneler is enabled.
 */
@Mixin(KeyboardInput.class)
public abstract class KeyboardInputMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void blackclient$onTick(boolean slowDown, float sneakSpeed, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen != null || mc.player == null) {
            return;
        }
        Tunneler tunneler = HackManager.INSTANCE.get(Tunneler.class);
        if (tunneler == null || !tunneler.isEnabled()) {
            return;
        }

        ClientPlayerEntity player = mc.player;
        if (!player.isAlive() || player.hasVehicle()) {
            return;
        }

        Input input = (Input) (Object) this;
        input.pressingForward = true;
        input.pressingBack = false;
        input.pressingLeft = false;
        input.pressingRight = false;
        input.movementSideways = 0.0F;
        input.jumping = false;
        input.sneaking = false;

        boolean canMove = tunneler.onControlTick(player);
        input.movementForward = canMove ? 1.0F : 0.0F;
    }
}
