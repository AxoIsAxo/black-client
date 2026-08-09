package com.kineticclient.mixin;

import com.kineticclient.hack.HackManager;
import com.kineticclient.hack.impl.Xray;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.FluidRenderer;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * X-ray: hides fluids (water/lava) that are not on the whitelist.
 */
@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void kinetic$xrayFluid(BlockRenderView world, BlockPos pos, VertexConsumer vertexConsumer,
                                       BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        Xray xray = HackManager.INSTANCE.get(Xray.class);
        if (xray != null && xray.isEnabled() && !xray.shouldRender(blockState)) {
            ci.cancel();
        }
    }
}
