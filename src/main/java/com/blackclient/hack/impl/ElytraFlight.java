package com.blackclient.hack.impl;

import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.BoolSetting;
import com.blackclient.hack.setting.ModeSetting;
import com.blackclient.hack.setting.NumberSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Elytra flight with three modes:
 * <ul>
 *   <li><b>Control</b> — hold space to climb, release to glide down; slight
 *       forward boost from the look direction.</li>
 *   <li><b>Free</b> — fly in the look direction at a fixed speed.</li>
 *   <li><b>Creative</b> — fly like in creative mode: WASD moves relative to
 *       the look direction, space climbs, sneak descends.</li>
 * </ul>
 * With <b>Auto start</b> (default on), flight begins automatically when
 * falling while wearing an elytra. The velocity is applied at the end of the
 * player's movement tick, after the vanilla elytra physics.
 */
public class ElytraFlight extends Hack {

    private final ModeSetting mode = add(new ModeSetting("Mode", "Control", "Control", "Free", "Creative"));
    private final BoolSetting autoStart = add(new BoolSetting("Auto start", true));
    private final NumberSetting speed = add(new NumberSetting("Speed", 1.2, 0.5, 3.0, 0.1));

    public ElytraFlight() {
        super("ElytraFlight", "Fly freely with an elytra");
    }

    public boolean controlMode() {
        return mode.getValue().equals("Control");
    }

    public boolean creativeMode() {
        return mode.getValue().equals("Creative");
    }

    /** Called from the ClientPlayerEntity tickMovement TAIL mixin. */
    public void onControlTick(ClientPlayerEntity player) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.currentScreen != null || mc.getNetworkHandler() == null) {
            return;
        }
        if (!player.isAlive() || player.hasVehicle() || !hasElytra(player)) {
            return;
        }

        if (autoStart.getValue() && !player.isFallFlying() && !player.isOnGround() && !player.isTouchingWater()) {
            if (player.getVelocity().y < -0.1) {
                startGliding(player, mc.getNetworkHandler());
            }
        }

        if (!player.isFallFlying() || player.isOnGround()) {
            return; // vanilla handles landing/stopping
        }

        boolean up = mc.options.jumpKey.isPressed();
        double speedValue = speed.getValue();

        if (controlMode()) {
            control(player, up, speedValue);
        } else if (creativeMode()) {
            creative(player, speedValue);
        } else {
            free(player, speedValue);
        }
    }

    /** Creative-style flight: WASD relative to the look yaw, space/sneak for vertical. */
    private static void creative(ClientPlayerEntity player, double speed) {
        MinecraftClient mc = MinecraftClient.getInstance();
        double forward = (mc.options.forwardKey.isPressed() ? 1 : 0) - (mc.options.backKey.isPressed() ? 1 : 0);
        double strafe = (mc.options.rightKey.isPressed() ? 1 : 0) - (mc.options.leftKey.isPressed() ? 1 : 0);
        double vertical = (mc.options.jumpKey.isPressed() ? 1 : 0) - (mc.options.sneakKey.isPressed() ? 1 : 0);

        float yawRad = (float) Math.toRadians(player.getYaw());
        double mx = (-MathHelper.sin(yawRad) * forward + MathHelper.cos(yawRad) * strafe) * speed;
        double mz = (MathHelper.cos(yawRad) * forward + MathHelper.sin(yawRad) * strafe) * speed;
        double my = vertical * speed;
        player.setVelocity(mx, my, mz);
    }

    private static void control(ClientPlayerEntity player, boolean up, double speed) {
        Vec3d velocity = player.getVelocity();
        float yawRad = (float) Math.toRadians(player.getYaw());

        double vx = velocity.x + -MathHelper.sin(yawRad) * 0.08;
        double vz = velocity.z + MathHelper.cos(yawRad) * 0.08;
        double vy = up ? 0.6 * speed : -0.25;
        player.setVelocity(vx, vy, vz);
    }

    private static void free(ClientPlayerEntity player, double speed) {
        float pitchRad = (float) Math.toRadians(player.getPitch());
        float yawRad = (float) Math.toRadians(player.getYaw());
        double mx = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad) * speed;
        double my = -MathHelper.sin(pitchRad) * speed;
        double mz = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad) * speed;
        player.setVelocity(mx, my, mz);
    }

    private static void startGliding(ClientPlayerEntity player, ClientPlayNetworkHandler networkHandler) {
        // Mirrors the vanilla client start: validate via checkFallFlying() and
        // tell the server with a START_FALL_FLYING client command.
        if (player.checkFallFlying()) {
            networkHandler.sendPacket(new ClientCommandC2SPacket(player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
        }
    }

    private static boolean hasElytra(ClientPlayerEntity player) {
        return player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);
    }
}
