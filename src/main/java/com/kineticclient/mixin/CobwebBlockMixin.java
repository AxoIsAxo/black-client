package com.kineticclient.mixin;

import com.kineticclient.hack.HackManager;
import com.kineticclient.hack.impl.NoSlowdown;
import net.minecraft.block.BlockState;
import net.minecraft.block.CobwebBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels the cobweb slowdown for the local player while NoSlowdown is active.
 * This is the 1.20.1 equivalent of the classic MCP trick of clearing the
 * entity's in-web flag every tick: {@code CobwebBlock.onEntityCollision} is the
 * single place where cobwebs apply their 0.25 / 0.05 / 0.25 velocity
 * multiplier, so cancelling it lets the player walk through webs at full speed.
 */
@Mixin(CobwebBlock.class)
public abstract class CobwebBlockMixin {

    @Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
    private void kinetic$onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (entity != MinecraftClient.getInstance().player) {
            return;
        }
        NoSlowdown noSlowdown = HackManager.INSTANCE.get(NoSlowdown.class);
        if (noSlowdown != null && noSlowdown.isEnabled() && noSlowdown.shouldRemoveCobwebs()) {
            ci.cancel();
        }
    }
}
