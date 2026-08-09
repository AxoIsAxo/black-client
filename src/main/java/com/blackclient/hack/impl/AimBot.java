package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.BoolSetting;
import com.blackclient.hack.setting.ModeSetting;
import com.blackclient.hack.setting.NumberSetting;
import com.blackclient.util.Rotations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.MathHelper;

/**
 * Smoothly aims the camera at the nearest target (or the target closest to
 * the crosshair). Does not attack on its own; pair it with KillAura.
 */
public class AimBot extends Hack {

    private final NumberSetting range = add(new NumberSetting("Range", 8.0, 2.0, 16.0, 0.1));
    private final NumberSetting speed = add(new NumberSetting("Speed", 5.0, 1.0, 10.0, 0.5));
    private final ModeSetting targetMode = add(new ModeSetting("Target", "Nearest", "Nearest", "Crosshair"));
    private final BoolSetting targetPlayers = add(new BoolSetting("Target players", true));
    private final BoolSetting targetMobs = add(new BoolSetting("Target mobs", true));
    private final BoolSetting avoidNpcs = add(new BoolSetting("Avoid NPCs", true));

    public AimBot() {
        super("AimBot", "Smoothly aims at the nearest target");
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || mc.currentScreen != null) {
            return;
        }

        Entity target = targetMode.getValue().equals("Crosshair")
                ? findClosestToCrosshair(mc, player)
                : findNearest(mc, player);
        if (target != null) {
            Rotations.aim(player, target, speed.getValueFloat());
        }
    }

    private boolean isValidTarget(MinecraftClient mc, Entity entity) {
        ClientPlayerEntity player = mc.player;
        if (entity == player || !entity.isAlive()) {
            return false;
        }
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        if (entity instanceof PlayerEntity && !targetPlayers.getValue()) {
            return false;
        }
        if (!(entity instanceof PlayerEntity) && !targetMobs.getValue()) {
            return false;
        }
        if (avoidNpcs.getValue() && isServerNpc(mc, entity)) {
            return false; // server-side player NPCs (not in the tab list)
        }
        return player.squaredDistanceTo(entity) <= range.getValue() * range.getValue();
    }

    /** True for player-shaped entities that are not in the tab list (server NPCs, e.g. Citizens). */
    private static boolean isServerNpc(MinecraftClient mc, Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }
        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
        return networkHandler != null && networkHandler.getPlayerListEntry(entity.getUuid()) == null;
    }

    private Entity findNearest(MinecraftClient mc, ClientPlayerEntity player) {
        double best = range.getValue() * range.getValue();
        Entity bestEntity = null;
        for (LivingEntity entity : mc.world.getEntitiesByType(
                TypeFilter.instanceOf(LivingEntity.class),
                player.getBoundingBox().expand(range.getValue()),
                target -> target != player && target.isAlive())) {
            if (!isValidTarget(mc, entity)) {
                continue;
            }
            double distance = player.squaredDistanceTo(entity);
            if (distance <= best) {
                best = distance;
                bestEntity = entity;
            }
        }
        return bestEntity;
    }

    private Entity findClosestToCrosshair(MinecraftClient mc, ClientPlayerEntity player) {
        float bestAngle = Float.MAX_VALUE;
        Entity bestEntity = null;
        for (LivingEntity entity : mc.world.getEntitiesByType(
                TypeFilter.instanceOf(LivingEntity.class),
                player.getBoundingBox().expand(range.getValue()),
                target -> target != player && target.isAlive())) {
            if (!isValidTarget(mc, entity)) {
                continue;
            }
            float[] rots = Rotations.getRotationsTo(player, entity);
            float diff = Math.abs(MathHelper.wrapDegrees(rots[0] - player.getYaw()))
                    + Math.abs(rots[1] - player.getPitch());
            if (diff < bestAngle) {
                bestAngle = diff;
                bestEntity = entity;
            }
        }
        return bestEntity;
    }
}
