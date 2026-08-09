package com.kineticclient.mixin;

import com.kineticclient.hack.HackManager;
import com.kineticclient.hack.impl.HealthBars;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Draws a health bar + counter above each target entity, on top of the HUD.
 * Entity positions are interpolated with the render tick delta, transformed
 * from world space into camera space (camera rotation) and then to NDC with
 * the basic projection matrix, and finally to scaled window coordinates.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void kinetic$renderHealthBars(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.gameRenderer == null) {
            return;
        }
        HealthBars healthBars = HackManager.INSTANCE.get(HealthBars.class);
        if (healthBars == null || !healthBars.isEnabled()) {
            return;
        }

        Camera camera = mc.gameRenderer.getCamera();
        if (!camera.isReady()) {
            return;
        }

        float tickDelta = tickCounter.getTickDelta(true);
        Vec3d cameraPos = camera.getPos();
        double range = healthBars.getRange();
        Box box = new Box(
                mc.player.getX() - range, mc.player.getY() - range, mc.player.getZ() - range,
                mc.player.getX() + range, mc.player.getY() + range, mc.player.getZ() + range);

        for (LivingEntity entity : mc.world.getEntitiesByType(
                TypeFilter.instanceOf(LivingEntity.class),
                box,
                target -> target != mc.player && target.isAlive())) {
            if (entity instanceof PlayerEntity && !healthBars.showPlayers()) {
                continue;
            }
            if (!(entity instanceof PlayerEntity) && !healthBars.showMobs()) {
                continue;
            }
            if (!healthBars.throughWalls() && !isVisible(mc, cameraPos, entity)) {
                continue;
            }

            Vec3d interpolated = new Vec3d(
                    MathHelper.lerp(tickDelta, entity.prevX, entity.getX()),
                    MathHelper.lerp(tickDelta, entity.prevY, entity.getY()),
                    MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ()));
            Vec3d head = new Vec3d(interpolated.x, interpolated.y + entity.getHeight() + 0.5, interpolated.z);

            double[] screen = project(mc, camera, head);
            if (screen == null) {
                continue; // behind the camera
            }

            drawBar(context, mc, screen[0], screen[1], entity);
        }
    }

    private static void drawBar(DrawContext context, MinecraftClient mc, double screenX, double screenY, LivingEntity entity) {
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float fraction = maxHealth <= 0.0F ? 0.0F : health / maxHealth;

        int barWidth = 30;
        int barHeight = 3;
        int x = (int) Math.round(screenX);
        int y = (int) Math.round(screenY);

        // Background track
        context.fill(x - barWidth / 2 - 1, y - 1, x + barWidth / 2 + 1, y + barHeight + 1, 0x90000000);
        // Health fill
        int color = fraction > 0.5F ? 0xFF55FF55 : (fraction > 0.25F ? 0xFFFFFF55 : 0xFFFF5555);
        int fillWidth = (int) (barWidth * fraction);
        context.fill(x - barWidth / 2, y, x - barWidth / 2 + fillWidth, y + barHeight, color);

        // Health counter above the bar
        String text = String.valueOf((int) Math.ceil(health));
        int textX = x - mc.textRenderer.getWidth(text) / 2;
        context.drawText(mc.textRenderer, text, textX, y - 10, 0xFFFFFFFF, true);
    }

    /** World position -> scaled screen coordinates, or null if behind the camera. */
    private static double[] project(MinecraftClient mc, Camera camera, Vec3d pos) {
        Vec3d cameraPos = camera.getPos();
        Vector4f vector = new Vector4f(
                (float) (pos.x - cameraPos.x),
                (float) (pos.y - cameraPos.y),
                (float) (pos.z - cameraPos.z),
                1.0F);
        Matrix4f view = new Matrix4f().rotation(camera.getRotation());
        Matrix4f projection = new Matrix4f(mc.gameRenderer.getBasicProjectionMatrix(mc.options.getFov().getValue()));
        vector.mul(view).mul(projection);
        if (vector.w <= 0.0F) {
            return null;
        }
        double x = (vector.x / vector.w * 0.5 + 0.5) * mc.getWindow().getScaledWidth();
        double y = (-vector.y / vector.w * 0.5 + 0.5) * mc.getWindow().getScaledHeight();
        return new double[]{x, y};
    }

    /** True when nothing solid blocks the line from the camera to the entity's eyes. */
    private static boolean isVisible(MinecraftClient mc, Vec3d cameraPos, LivingEntity entity) {
        Vec3d target = entity.getEyePos();
        BlockHitResult hit = mc.world.raycast(new RaycastContext(
                cameraPos, target, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        return hit == null || hit.getPos().squaredDistanceTo(cameraPos) >= cameraPos.squaredDistanceTo(target);
    }
}
