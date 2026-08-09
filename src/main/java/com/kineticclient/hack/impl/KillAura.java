package com.kineticclient.hack.impl;

import com.kineticclient.hack.Hack;
import com.kineticclient.hack.setting.BoolSetting;
import com.kineticclient.hack.setting.NumberSetting;
import com.kineticclient.util.Rotations;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypeFilter;

/**
 * Automatically attacks the nearest target entity within range, at a rate
 * capped by the attack-cooldown system (1.9+ combat).
 */
public class KillAura extends Hack {

    private final NumberSetting cps = add(new NumberSetting("CPS", 8, 1, 20, 1));
    private final NumberSetting range = add(new NumberSetting("Range", 4.2, 3.0, 6.0, 0.1));
    private final BoolSetting rotate = add(new BoolSetting("Rotate to target", true));
    private final BoolSetting targetPlayers = add(new BoolSetting("Target players", true));
    private final BoolSetting targetMobs = add(new BoolSetting("Target mobs", true));
    private final BoolSetting avoidNpcs = add(new BoolSetting("Avoid NPCs", true));

    private long lastAttack;

    public KillAura() {
        super("KillAura", "Automatically attacks nearby entities");
    }

    @Override
    public void onTick() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.world == null || mc.currentScreen != null) {
            return;
        }

        Entity target = findTarget(mc, player);
        if (target == null) {
            return;
        }

        // Keep looking at the target while it is in range.
        if (rotate.getValue()) {
            Rotations.aim(player, target, 360.0f);
        }

        long now = System.currentTimeMillis();
        long interval = (long) (1000.0 / cps.getValue());
        if (now - lastAttack < interval) {
            return;
        }

        // Respect the 1.9+ attack cooldown so hits actually register.
        if (player.getAttackCooldownProgress(0.5f) < 1.0f) {
            return;
        }

        if (mc.interactionManager != null) {
            mc.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
            lastAttack = now;
        }
    }

    private Entity findTarget(MinecraftClient mc, ClientPlayerEntity player) {
        double bestDistance = range.getValue() * range.getValue();
        Entity best = null;
        for (LivingEntity entity : mc.world.getEntitiesByType(
                TypeFilter.instanceOf(LivingEntity.class),
                player.getBoundingBox().expand(range.getValue()),
                target -> target != player && target.isAlive())) {
            if (entity instanceof PlayerEntity && !targetPlayers.getValue()) {
                continue;
            }
            if (!(entity instanceof PlayerEntity) && !targetMobs.getValue()) {
                continue;
            }
            if (avoidNpcs.getValue() && isServerNpc(mc, entity)) {
                continue; // server-side player NPCs (not in the tab list)
            }
            double distance = player.squaredDistanceTo(entity);
            if (distance <= bestDistance) {
                bestDistance = distance;
                best = entity;
            }
        }
        return best;
    }

    /** True for player-shaped entities that are not in the tab list (server NPCs, e.g. Citizens). */
    private static boolean isServerNpc(MinecraftClient mc, Entity entity) {
        if (!(entity instanceof PlayerEntity)) {
            return false;
        }
        ClientPlayNetworkHandler networkHandler = mc.getNetworkHandler();
        return networkHandler != null && networkHandler.getPlayerListEntry(entity.getUuid()) == null;
    }
}
