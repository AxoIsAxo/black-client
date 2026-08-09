package com.blackclient.mixin;

import com.blackclient.hack.HackManager;
import com.blackclient.hack.impl.Xray;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.BlockModelRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * X-ray: skip rendering blocks that are not on the whitelist, and force every
 * face to draw so faces touching hidden blocks are not culled (otherwise ores
 * buried in stone would be culled into invisibility).
 */
@Mixin(BlockModelRenderer.class)
public abstract class BlockModelRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void blackclient$xrayRender(BlockRenderView world, BakedModel model, BlockState state, BlockPos pos,
                                        MatrixStack matrices, VertexConsumer vertexConsumer, boolean cull,
                                        Random random, long seed, int overlay, CallbackInfo ci) {
        Xray xray = HackManager.INSTANCE.get(Xray.class);
        if (xray != null && xray.isEnabled() && !xray.shouldRender(state)) {
            ci.cancel();
        }
    }

    @Redirect(method = {"renderSmooth", "renderFlat"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;shouldDrawSide(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/BlockView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean blackclient$xrayDrawSide(BlockState state, net.minecraft.world.BlockView world, BlockPos pos,
                                             Direction side, BlockPos otherPos) {
        Xray xray = HackManager.INSTANCE.get(Xray.class);
        if (xray != null && xray.isEnabled()) {
            return true; // draw every face so hidden blocks don't cull visible ones
        }
        return Block.shouldDrawSide(state, world, pos, side, otherPos);
    }
}
