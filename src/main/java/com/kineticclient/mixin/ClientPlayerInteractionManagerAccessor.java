package com.kineticclient.mixin;

import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Exposes {@link ClientPlayerInteractionManager#isCurrentlyBreaking(BlockPos)},
 * which is private, so Tunneler can continue a block-break started by
 * {@code attackBlock} instead of restarting it every tick.
 */
@Mixin(ClientPlayerInteractionManager.class)
public interface ClientPlayerInteractionManagerAccessor {

    @Invoker("isCurrentlyBreaking")
    boolean kinetic$isCurrentlyBreaking(BlockPos pos);
}
