package com.kineticclient.mixin;

import com.kineticclient.audio.SoundSynthesizer;
import com.kineticclient.gui.GuiUtil;
import com.kineticclient.gui.HackMenuScreen;
import com.kineticclient.gui.ToastManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.SplashTextRenderer;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rebuilt Title Screen matching KineticsLabs Website Hero Section:
 * Features particle grid background, KineticsLabs top bar, giant hero typography,
 * tag pills, neobrutalist buttons, interactive ClickGUI launcher, and CI status.
 */
@Mixin(TitleScreen.class)
public abstract class TitleScreenMixin {

    @Unique
    private static final long START_TIME = System.currentTimeMillis();

    @Inject(method = "render", at = @At("HEAD"))
    private void kinetic$renderTitleScreen(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();

        // 1. Dark Gradient & Ambient Particles Background
        GuiUtil.drawParticleBackground(context, mouseX, mouseY, width, height);

        // 2. KineticsLabs Top Navigation Bar
        drawTopBar(context, mc, mouseX, mouseY, width);

        // 3. Hero Section (Wordmark & Tag Pills)
        drawHero(context, mc, width, height);

        // 4. Toasts
        ToastManager.INSTANCE.render(context, mc.textRenderer, width, height);
    }

    @Unique
    private void drawTopBar(DrawContext context, MinecraftClient mc, int mouseX, int mouseY, int width) {
        int barH = 26;
        GuiUtil.rect(context, 0, 0, width, barH, 0xF206070C);
        GuiUtil.rect(context, 0, barH - 1, width, 1, GuiUtil.BORDER_DARK);

        // [KL] Brand Box
        int brandX = 12;
        int brandY = 4;
        GuiUtil.brutalBox(context, brandX, brandY, 18, 18, 0xFF000000, GuiUtil.ACCENT_CYAN, GuiUtil.ACCENT_PURPLE, 2);
        GuiUtil.text(context, mc.textRenderer, "KL", brandX + 3, brandY + 5, GuiUtil.ACCENT_CYAN);

        // KINETICS LABS
        int nameX = brandX + 24;
        GuiUtil.text(context, mc.textRenderer, "KINETICS", nameX, brandY + 5, GuiUtil.TEXT_WHITE);
        int labX = nameX + mc.textRenderer.getWidth("KINETICS ");
        GuiUtil.text(context, mc.textRenderer, "LABS", labX, brandY + 5, GuiUtil.ACCENT_PURPLE);

        // [ORG] Pill
        int orgX = labX + mc.textRenderer.getWidth("LABS") + 5;
        GuiUtil.tagPill(context, mc.textRenderer, "ORG", orgX, brandY + 3, GuiUtil.ACCENT_CYAN, GuiUtil.TEXT_BLACK, 0xFF000000);

        // Meta status
        int metaX = orgX + 36;
        if (metaX + 160 < width - 120) {
            GuiUtil.rect(context, metaX, brandY + 7, 5, 5, GuiUtil.ACCENT_EMERALD);
            GuiUtil.text(context, mc.textRenderer, "FABRIC 1.20+", metaX + 8, brandY + 5, GuiUtil.TEXT_SLATE);
            int ciX = metaX + mc.textRenderer.getWidth("FABRIC 1.20+") + 16;
            GuiUtil.text(context, mc.textRenderer, "CI: #31628147900 [PASS]", ciX, brandY + 5, GuiUtil.ACCENT_CYAN);
        }

        // FX Button on Right
        int fxW = 50;
        int fxX = width - fxW - 12;
        boolean fxOn = SoundSynthesizer.INSTANCE.isEnabled();
        GuiUtil.brutalBox(context, fxX, brandY, fxW, 18, fxOn ? 0xFF083344 : 0xFF0F172A, fxOn ? GuiUtil.ACCENT_CYAN : GuiUtil.BORDER_SLATE, 0xFF000000, 1);
        GuiUtil.textCentered(context, mc.textRenderer, fxOn ? "FX: ON" : "FX: OFF", fxX + fxW / 2, brandY + 5, fxOn ? GuiUtil.ACCENT_CYAN : GuiUtil.TEXT_MUTED);
    }

    @Unique
    private void drawHero(DrawContext context, MinecraftClient mc, int width, int height) {
        int centerX = width / 2;
        int heroY = Math.max(34, height / 4 - 36);

        // Tag Pills Row
        int tagY = heroY;
        int t1W = mc.textRenderer.getWidth("MINECRAFT UTILITY CLIENT") + 8;
        int t2W = mc.textRenderer.getWidth("CLIENT-SIDE ONLY") + 8;
        int t3W = mc.textRenderer.getWidth("OPEN RIGHT-SHIFT") + 8;
        int totalTagsW = t1W + t2W + t3W + 12;
        int tagStartX = centerX - totalTagsW / 2;

        GuiUtil.tagPill(context, mc.textRenderer, "MINECRAFT UTILITY CLIENT", tagStartX, tagY, GuiUtil.ACCENT_PURPLE, GuiUtil.TEXT_BLACK, 0xFF000000);
        GuiUtil.tagPill(context, mc.textRenderer, "CLIENT-SIDE ONLY", tagStartX + t1W + 6, tagY, GuiUtil.ACCENT_CYAN, GuiUtil.TEXT_BLACK, 0xFF000000);
        GuiUtil.tagPill(context, mc.textRenderer, "OPEN RIGHT-SHIFT", tagStartX + t1W + t2W + 12, tagY, 0xFFFFFFFF, GuiUtil.TEXT_BLACK, 0xFF000000);

        // Giant Title: KINETIC CLIENT
        int titleY = heroY + 18;
        String line1 = "KINETIC CLIENT";
        GuiUtil.textGlitchCentered(context, mc.textRenderer, line1, centerX, titleY);

        // Subtitle
        int subY = titleY + 14;
        String desc = "High-performance Fabric utility client for anarchy servers";
        GuiUtil.textCentered(context, mc.textRenderer, desc, centerX, subY, GuiUtil.TEXT_SLATE);
    }

    @Inject(method = "renderPanoramaBackground", at = @At("HEAD"), cancellable = true)
    private void kinetic$hidePanorama(DrawContext context, float delta, CallbackInfo ci) {
        ci.cancel();
    }

    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/SplashTextRenderer;render(Lnet/minecraft/client/gui/DrawContext;ILnet/minecraft/client/font/TextRenderer;I)V"))
    private void kinetic$hideSplash(SplashTextRenderer renderer, DrawContext context, int screenWidth, TextRenderer textRenderer, int alpha) {
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void kinetic$repositionWidgets(CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        int width = mc.getWindow().getScaledWidth();
        int height = mc.getWindow().getScaledHeight();

        int blockY = Math.max(90, height / 2 - 25);
        int vanillaStart = height / 4 + 48;
        int shift = blockY - vanillaStart;

        TitleScreen screen = (TitleScreen) (Object) this;

        for (Element element : screen.children()) {
            if (element instanceof ClickableWidget widget) {
                // Keep copyright notice in place
                if (widget.getY() + widget.getHeight() >= height - 20) {
                    continue;
                }
                widget.setY(widget.getY() + shift);
            }
        }
    }
}
