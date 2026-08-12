package com.kineticclient.mixin;

import com.kineticclient.gui.GuiUtil;
import com.kineticclient.gui.ToastManager;
import com.kineticclient.hack.Hack;
import com.kineticclient.hack.HackManager;
import com.kineticclient.hack.impl.AutoClicker;
import com.kineticclient.hack.impl.HealthBars;
import com.kineticclient.hack.impl.KillAura;
import com.kineticclient.hack.impl.Speed;
import com.kineticclient.hack.setting.ModeSetting;
import com.kineticclient.hack.setting.NumberSetting;
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

import java.util.Comparator;
import java.util.List;

/**
 * InGame HUD Mixin:
 * Renders the KineticsLabs Watermark, Active Module ArrayList, HealthBars,
 * and live toast notifications.
 */
@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("TAIL"))
    private void kinetic$renderHud(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) {
            return;
        }

        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        // 1. Watermark (Top Left)
        drawWatermark(context, mc);

        // 2. ArrayList (Top Right)
        drawArrayList(context, mc, screenW);

        // 3. HealthBars
        renderHealthBars(context, mc, tickCounter);

        // 4. In-Game Toasts
        ToastManager.INSTANCE.render(context, mc.textRenderer, screenW, screenH);
    }

    private static void drawWatermark(DrawContext context, MinecraftClient mc) {
        int x = 8;
        int y = 8;

        // [KL] Square Badge
        GuiUtil.brutalBox(context, x, y, 16, 16, 0xFF000000, GuiUtil.ACCENT_CYAN, GuiUtil.ACCENT_PURPLE, 2);
        GuiUtil.text(context, mc.textRenderer, "KL", x + 3, y + 4, GuiUtil.ACCENT_CYAN);

        // Brand Name
        int textX = x + 22;
        GuiUtil.text(context, mc.textRenderer, "KINETICS", textX, y + 4, GuiUtil.TEXT_WHITE);
        int labX = textX + mc.textRenderer.getWidth("KINETICS ");
        GuiUtil.text(context, mc.textRenderer, "LABS", labX, y + 4, GuiUtil.ACCENT_PURPLE);

        // [FABRIC 1.20+]
        int pillX = labX + mc.textRenderer.getWidth("LABS") + 6;
        GuiUtil.tagPill(context, mc.textRenderer, "FABRIC 1.20+", pillX, y + 2, GuiUtil.ACCENT_CYAN, GuiUtil.TEXT_BLACK, 0xFF000000);

        // Stats Box: Active / FPS / CPS
        int statsY = y + 20;
        int activeCount = HackManager.INSTANCE.getEnabledCount();
        int fps = mc.getCurrentFps();
        AutoClicker ac = HackManager.INSTANCE.get(AutoClicker.class);
        String cpsStr = (ac != null && ac.isEnabled()) ? "14 CPS" : "0 CPS";

        String statsText = "ACTIVE: " + activeCount + "  |  " + fps + " FPS  |  " + cpsStr;
        int statsW = mc.textRenderer.getWidth(statsText) + 8;
        GuiUtil.brutalBox(context, x, statsY, statsW, 13, 0xD0060913, GuiUtil.BORDER_DARK, 0xFF000000, 1);
        GuiUtil.text(context, mc.textRenderer, statsText, x + 4, statsY + 3, GuiUtil.TEXT_SLATE);
    }

    private static void drawArrayList(DrawContext context, MinecraftClient mc, int screenW) {
        List<Hack> enabled = HackManager.INSTANCE.getHacks().stream()
                .filter(Hack::isEnabled)
                .sorted(Comparator.comparingInt(h -> -mc.textRenderer.getWidth(getDisplayName((Hack) h))))
                .toList();

        int y = 8;
        for (Hack hack : enabled) {
            String name = getDisplayName(hack);
            int textW = mc.textRenderer.getWidth(name);
            int cardW = textW + 10;
            int cardH = 13;
            int cardX = screenW - cardW - 8;

            int catColor = hack.getCategory() != null ? hack.getCategory().getColor() : GuiUtil.ACCENT_CYAN;

            // Neobrutalist ArrayList pill
            GuiUtil.brutalBox(context, cardX, y, cardW, cardH, 0xEE060913, 0xFF000000, 0xFF000000, 1);
            // Category colored left accent line
            GuiUtil.rect(context, cardX, y, 2, cardH, catColor);

            // Module Text
            GuiUtil.text(context, mc.textRenderer, hack.getName(), cardX + 5, y + 3, GuiUtil.TEXT_WHITE);

            // Subvalue if present
            String sub = getSubValue(hack);
            if (!sub.isEmpty()) {
                int nameW = mc.textRenderer.getWidth(hack.getName() + " ");
                GuiUtil.text(context, mc.textRenderer, sub, cardX + 5 + nameW, y + 3, GuiUtil.TEXT_MUTED);
            }

            y += (cardH + 3);
        }
    }

    private static String getDisplayName(Hack hack) {
        String sub = getSubValue(hack);
        return sub.isEmpty() ? hack.getName() : hack.getName() + " " + sub;
    }

    private static String getSubValue(Hack hack) {
        for (var setting : hack.getSettings()) {
            if (setting instanceof ModeSetting m) {
                return "[" + m.getValue() + "]";
            }
            if (setting instanceof NumberSetting n) {
                if (hack instanceof KillAura) return "[" + String.format("%.1f", n.getValue()) + "]";
                if (hack instanceof AutoClicker) return "[" + n.getValueInt() + " CPS]";
                if (hack instanceof Speed) return "[" + String.format("%.1f", n.getValue()) + "x]";
            }
        }
        return "";
    }

    private static void renderHealthBars(DrawContext context, MinecraftClient mc, RenderTickCounter tickCounter) {
        HealthBars healthBars = HackManager.INSTANCE.get(HealthBars.class);
        if (healthBars == null || !healthBars.isEnabled() || mc.gameRenderer == null) {
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
                continue;
            }

            drawBar(context, mc, screen[0], screen[1], entity);
        }
    }

    private static void drawBar(DrawContext context, MinecraftClient mc, double screenX, double screenY, LivingEntity entity) {
        float health = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float fraction = maxHealth <= 0.0F ? 0.0F : health / maxHealth;

        int barWidth = 32;
        int barHeight = 4;
        int x = (int) Math.round(screenX);
        int y = (int) Math.round(screenY);

        int bx = x - barWidth / 2;
        int by = y;

        // Neobrutalist Bar Box
        GuiUtil.brutalBox(context, bx, by, barWidth, barHeight, 0xFF000000, 0xFF1E293B, 0xFF000000, 1);

        // Health fill
        int color = fraction > 0.5F ? GuiUtil.ACCENT_EMERALD : (fraction > 0.25F ? 0xFFFBBF24 : 0xFFEF4444);
        int fillWidth = (int) (barWidth * fraction);
        if (fillWidth > 0) {
            GuiUtil.rect(context, bx, by, fillWidth, barHeight, color);
        }

        // Health counter above the bar
        String text = String.valueOf((int) Math.ceil(health));
        int textX = x - mc.textRenderer.getWidth(text) / 2;
        GuiUtil.text(context, mc.textRenderer, text, textX, y - 10, 0xFFFFFFFF);
    }

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

    private static boolean isVisible(MinecraftClient mc, Vec3d cameraPos, LivingEntity entity) {
        Vec3d target = entity.getEyePos();
        BlockHitResult hit = mc.world.raycast(new RaycastContext(
                cameraPos, target, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
        return hit == null || hit.getPos().squaredDistanceTo(cameraPos) >= cameraPos.squaredDistanceTo(target);
    }
}
