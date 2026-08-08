package com.blackclient.util;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Rotation math shared by AimBot and KillAura.
 */
public final class Rotations {

    private Rotations() {
    }

    /** Yaw/pitch (degrees) needed to look from {@code from} at {@code to}'s eyes. */
    public static float[] getRotationsTo(Entity from, Entity to) {
        Vec3d a = from.getEyePos();
        Vec3d b = to.getEyePos();
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dz = b.z - a.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        return new float[]{MathHelper.wrapDegrees(yaw), MathHelper.clamp(pitch, -90.0f, 90.0f)};
    }

    /**
     * Smoothly rotates the player's camera toward {@code target}.
     *
     * @param speed degrees of rotation per tick
     */
    public static void aim(ClientPlayerEntity player, Entity target, float speed) {
        float[] rots = getRotationsTo(player, target);
        float diffYaw = MathHelper.wrapDegrees(rots[0] - player.getYaw());
        float diffPitch = rots[1] - player.getPitch();

        player.setYaw(MathHelper.wrapDegrees(player.getYaw() + MathHelper.clamp(diffYaw, -speed, speed)));
        player.setPitch(MathHelper.clamp(player.getPitch() + MathHelper.clamp(diffPitch, -speed, speed), -90.0f, 90.0f));
    }
}
