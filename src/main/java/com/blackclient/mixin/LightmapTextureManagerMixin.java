package com.blackclient.mixin;

import com.blackclient.hack.HackManager;
import com.blackclient.hack.impl.NightVision;
import com.blackclient.hack.impl.Xray;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Gives the local player permanent night vision without touching their status
 * effects. In {@code LightmapTextureManager.update(float)} the night-vision
 * branch is taken when {@code hasStatusEffect(NIGHT_VISION)} is true, and its
 * strength comes from {@code GameRenderer.getNightVisionStrength(player, delta)}
 * (which returns 0 without a real effect instance), so both calls are
 * redirected while the hack is enabled.
 *
 * <p>The lightmap is recomputed every frame ({@code tick()} sets the dirty
 * flag), so toggling the hack takes effect immediately.
 */
@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;hasStatusEffect(Lnet/minecraft/registry/entry/RegistryEntry;)Z", ordinal = 0))
    private boolean blackclient$nightVisionHasEffect(ClientPlayerEntity player, RegistryEntry<StatusEffect> effect) {
        if (fullbrightActive()) {
            return true;
        }
        return player.hasStatusEffect(effect);
    }

    @Redirect(method = "update", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F"))
    private float blackclient$nightVisionStrength(LivingEntity entity, float tickDelta) {
        if (fullbrightActive()) {
            return 1.0F;
        }
        return GameRenderer.getNightVisionStrength(entity, tickDelta);
    }

    /**
     * Fullbright is active while NightVision is on, or while X-ray is on with
     * its Fullbright setting enabled (otherwise the revealed blocks would be
     * pitch black underground).
     */
    private static boolean fullbrightActive() {
        NightVision nightVision = HackManager.INSTANCE.get(NightVision.class);
        if (nightVision != null && nightVision.isEnabled()) {
            return true;
        }
        Xray xray = HackManager.INSTANCE.get(Xray.class);
        return xray != null && xray.isEnabled() && xray.fullbright();
    }
}
