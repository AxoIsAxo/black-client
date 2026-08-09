package com.kineticclient.mixin;

import com.kineticclient.hack.HackManager;
import com.kineticclient.hack.impl.Xray;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * X-ray: hides block entities (spawners, chests, furnaces, ...) whose block is
 * not on the whitelist. Rendered per frame, so this applies immediately.
 */
@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {

    @Inject(method = "render(Lnet/minecraft/block/entity/BlockEntity;FLnet/minecraft/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;)V", at = @At("HEAD"), cancellable = true)
    private void kinetic$xrayBlockEntity(BlockEntity blockEntity, float tickDelta, MatrixStack matrices,
                                             VertexConsumerProvider vertexConsumers, CallbackInfo ci) {
        Xray xray = HackManager.INSTANCE.get(Xray.class);
        if (xray != null && xray.isEnabled() && !xray.shouldRender(blockEntity.getCachedState())) {
            ci.cancel();
        }
    }
}
