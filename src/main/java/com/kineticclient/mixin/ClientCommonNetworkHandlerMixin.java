package com.kineticclient.mixin;

import com.kineticclient.hack.HackManager;
import com.kineticclient.hack.impl.NoHunger;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Filters outgoing packets. NoHunger drops the {@code START_SPRINTING} command
 * so the server never marks the player as sprinting and never charges the
 * per-tick sprint exhaustion (the client still sprints locally).
 */
@Mixin(ClientCommonNetworkHandler.class)
public abstract class ClientCommonNetworkHandlerMixin {

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    private void kinetic$filterPackets(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof ClientCommandC2SPacket command
                && command.getMode() == ClientCommandC2SPacket.Mode.START_SPRINTING) {
            NoHunger noHunger = HackManager.INSTANCE.get(NoHunger.class);
            if (noHunger != null && noHunger.isEnabled() && noHunger.cancelSprint()) {
                ci.cancel();
            }
        }
    }
}
