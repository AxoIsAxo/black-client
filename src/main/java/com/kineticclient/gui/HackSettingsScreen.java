package com.kineticclient.gui;

import com.kineticclient.audio.SoundSynthesizer;
import com.kineticclient.config.Config;
import com.kineticclient.gui.GuiUtil.Rect;
import com.kineticclient.hack.Hack;
import com.kineticclient.hack.setting.BoolSetting;
import com.kineticclient.hack.setting.ModeSetting;
import com.kineticclient.hack.setting.NumberSetting;
import com.kineticclient.hack.setting.StringSetting;
import com.kineticclient.util.MenuHider;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * KineticsLabs Neobrutalist Hack Settings Screen.
 */
public class HackSettingsScreen extends Screen {

    private final Hack hack;
    private final List<Clickable> clickables = new ArrayList<>();
    private final List<CustomSlider> sliders = new ArrayList<>();
    private CustomSlider activeSlider = null;

    private StringSetting focusedText;
    private boolean listeningKeybind;

    private record Clickable(Rect bounds, Runnable action) {
    }

    public HackSettingsScreen(Hack hack) {
        super(Text.literal(hack.getName()));
        this.hack = hack;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (MenuHider.hidden()) {
            return;
        }

        buildLayout(mouseX, mouseY);

        // 1. Background
        GuiUtil.drawParticleBackground(context, mouseX, mouseY, width, height);

        // 2. Main Center Card
        int cardW = Math.min(320, width - 40);
        int cardH = Math.min(height - 60, 40 + (hack.getSettings().size() * 28) + 60);
        int cardX = (width - cardW) / 2;
        int cardY = (height - cardH) / 2;

        int catColor = hack.getCategory() != null ? hack.getCategory().getColor() : GuiUtil.ACCENT_CYAN;

        // Card Box
        GuiUtil.brutalBoxThick(context, cardX, cardY, cardW, cardH, GuiUtil.SURFACE_GLASS, catColor, catColor, 4);

        // Header Row
        int headerH = 28;
        GuiUtil.rect(context, cardX, cardY, cardW, headerH, 0xFF000000);
        GuiUtil.rect(context, cardX, cardY + headerH - 1, cardW, 1, catColor);

        // Back Button [< BACK]
        int backW = 46;
        int backH = 18;
        int backX = cardX + 6;
        int backY = cardY + 5;
        Rect backRect = new Rect(backX, backY, backW, backH);
        boolean backHover = GuiUtil.hovered(mouseX, mouseY, backRect);
        GuiUtil.brutalBox(context, backX, backY, backW, backH, backHover ? 0xFF1E293B : 0xFF0F172A, backHover ? GuiUtil.ACCENT_CYAN : GuiUtil.BORDER_SLATE, 0xFF000000, 1);
        GuiUtil.textCentered(context, client.textRenderer, "< BACK", backX + backW / 2, backY + 5, backHover ? GuiUtil.ACCENT_CYAN : GuiUtil.TEXT_WHITE);
        clickables.add(new Clickable(backRect, () -> {
            SoundSynthesizer.INSTANCE.playClick("mechanical");
            this.close();
        }));

        // Title
        String title = hack.getName().toUpperCase();
        GuiUtil.textGlitchCentered(context, client.textRenderer, title, cardX + cardW / 2, cardY + 8);

        // Master Toggle Button
        int toggleW = 28;
        int toggleH = 16;
        int toggleX = cardX + cardW - toggleW - 6;
        int toggleY = cardY + 6;
        Rect toggleRect = new Rect(toggleX, toggleY, toggleW, toggleH);
        boolean toggleHover = GuiUtil.hovered(mouseX, mouseY, toggleRect);
        GuiUtil.toggleButton(context, client.textRenderer, toggleX, toggleY, toggleW, toggleH, hack.isEnabled(), toggleHover);
        clickables.add(new Clickable(toggleRect, () -> {
            hack.toggle();
            Config.INSTANCE.save();
            if (hack.isEnabled()) {
                SoundSynthesizer.INSTANCE.playToggleOn();
                ToastManager.INSTANCE.show("⚡ " + hack.getName() + " // ACTIVATED", ToastManager.ToastType.CYAN);
            } else {
                SoundSynthesizer.INSTANCE.playToggleOff();
                ToastManager.INSTANCE.show(hack.getName() + " // DEACTIVATED", ToastManager.ToastType.PURPLE);
            }
        }));

        // Content Rows
        int curY = cardY + headerH + 8;
        int rowW = cardW - 16;
        int rowX = cardX + 8;

        // Keybind Row
        int keyRowH = 20;
        Rect keyRect = new Rect(rowX, curY, rowW, keyRowH);
        boolean keyHover = GuiUtil.hovered(mouseX, mouseY, keyRect);
        GuiUtil.brutalBox(context, rowX, curY, rowW, keyRowH, keyHover ? GuiUtil.BG_CARD_HOVER : GuiUtil.BG_CARD, keyHover ? catColor : GuiUtil.BORDER_DARK, 0xFF000000, 1);
        GuiUtil.text(context, client.textRenderer, "KEYBIND", rowX + 6, curY + 6, GuiUtil.TEXT_SLATE);

        String keyStr = listeningKeybind ? "[PRESS KEY...]" : (hack.getKeyBind() == -1 ? "[NONE]" : "[" + InputUtil.fromKeyCode(hack.getKeyBind(), 0).getLocalizedText().getString() + "]");
        int keyStrW = client.textRenderer.getWidth(keyStr);
        int keyStrX = rowX + rowW - keyStrW - 6;
        GuiUtil.text(context, client.textRenderer, keyStr, keyStrX, curY + 6, listeningKeybind ? GuiUtil.ACCENT_CYAN : catColor);
        clickables.add(new Clickable(keyRect, () -> {
            listeningKeybind = true;
            SoundSynthesizer.INSTANCE.playClick("tick");
        }));

        curY += keyRowH + 6;

        // Settings Rows
        for (var setting : hack.getSettings()) {
            if (setting instanceof NumberSetting num) {
                int sH = 24;
                CustomSlider slider = new CustomSlider(num, catColor);
                slider.setBounds(new Rect(rowX + 2, curY, rowW - 4, sH));
                sliders.add(slider);
                slider.render(context, client.textRenderer);
                curY += sH + 6;
            } else if (setting instanceof ModeSetting mode) {
                int mH = 20;
                Rect modeRect = new Rect(rowX, curY, rowW, mH);
                boolean mHover = GuiUtil.hovered(mouseX, mouseY, modeRect);
                GuiUtil.brutalBox(context, rowX, curY, rowW, mH, mHover ? 0xFF1E293B : GuiUtil.BG_CARD, mHover ? catColor : GuiUtil.BORDER_DARK, 0xFF000000, 1);
                GuiUtil.text(context, client.textRenderer, mode.getName().toUpperCase(), rowX + 6, curY + 6, GuiUtil.TEXT_SLATE);
                String val = mode.getValue();
                int valW = client.textRenderer.getWidth(val);
                GuiUtil.text(context, client.textRenderer, val, rowX + rowW - valW - 6, curY + 6, catColor);
                clickables.add(new Clickable(modeRect, () -> {
                    mode.cycle();
                    Config.INSTANCE.save();
                    SoundSynthesizer.INSTANCE.playTick();
                }));
                curY += mH + 6;
            } else if (setting instanceof BoolSetting bool) {
                int bH = 20;
                Rect boolRect = new Rect(rowX, curY, rowW, bH);
                boolean bHover = GuiUtil.hovered(mouseX, mouseY, boolRect);
                GuiUtil.brutalBox(context, rowX, curY, rowW, bH, bHover ? GuiUtil.BG_CARD_HOVER : GuiUtil.BG_CARD, bHover ? catColor : GuiUtil.BORDER_DARK, 0xFF000000, 1);
                GuiUtil.text(context, client.textRenderer, bool.getName().toUpperCase(), rowX + 6, curY + 6, GuiUtil.TEXT_SLATE);
                int bToggleW = 28;
                int bToggleH = 14;
                int bToggleX = rowX + rowW - bToggleW - 6;
                int bToggleY = curY + 3;
                GuiUtil.toggleButton(context, client.textRenderer, bToggleX, bToggleY, bToggleW, bToggleH, bool.getValue(), bHover);
                clickables.add(new Clickable(boolRect, () -> {
                    bool.setValue(!bool.getValue());
                    Config.INSTANCE.save();
                    if (bool.getValue()) SoundSynthesizer.INSTANCE.playToggleOn();
                    else SoundSynthesizer.INSTANCE.playToggleOff();
                }));
                curY += bH + 6;
            } else if (setting instanceof StringSetting str) {
                int tH = 24;
                Rect textRect = new Rect(rowX, curY, rowW, tH);
                boolean focused = focusedText == str;
                GuiUtil.brutalBox(context, rowX, curY, rowW, tH, 0xFF000000, focused ? GuiUtil.ACCENT_CYAN : GuiUtil.BORDER_SLATE, 0xFF000000, 1);
                GuiUtil.text(context, client.textRenderer, str.getName().toUpperCase() + ":", rowX + 6, curY + 7, GuiUtil.TEXT_MUTED);
                int labelW = client.textRenderer.getWidth(str.getName() + ": ");
                String val = str.getValue();
                GuiUtil.text(context, client.textRenderer, val, rowX + 6 + labelW, curY + 7, GuiUtil.TEXT_WHITE);
                if (focused && ((System.currentTimeMillis() / 450) % 2 == 0)) {
                    int cx = rowX + 6 + labelW + client.textRenderer.getWidth(val);
                    GuiUtil.rect(context, cx, curY + 5, 1, 12, GuiUtil.ACCENT_CYAN);
                }
                clickables.add(new Clickable(textRect, () -> focusedText = str));
                curY += tH + 6;
            }
        }

        // Description Box at bottom of card
        if (hack.getDescription() != null && !hack.getDescription().isEmpty()) {
            int descY = cardY + cardH - 18;
            GuiUtil.rect(context, cardX + 4, descY - 1, cardW - 8, 1, GuiUtil.BORDER_DARK);
            GuiUtil.textCentered(context, client.textRenderer, hack.getDescription(), cardX + cardW / 2, descY + 4, GuiUtil.TEXT_MUTED);
        }

        // Toasts
        ToastManager.INSTANCE.render(context, client.textRenderer, width, height);
    }

    private void buildLayout(int mouseX, int mouseY) {
        clickables.clear();
        sliders.clear();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        focusedText = null;

        for (CustomSlider slider : sliders) {
            if (slider.getBounds() != null && slider.getBounds().contains((int) mouseX, (int) mouseY)) {
                slider.onClick(mouseX, button);
                activeSlider = slider;
                return true;
            }
        }

        for (Clickable clickable : clickables) {
            if (clickable.bounds().contains((int) mouseX, (int) mouseY)) {
                clickable.action().run();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (activeSlider != null) {
            activeSlider.onDrag(mouseX);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (activeSlider != null) {
            activeSlider.onRelease();
            activeSlider = null;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningKeybind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                hack.setKeyBind(-1);
            } else {
                hack.setKeyBind(keyCode);
            }
            Config.INSTANCE.save();
            listeningKeybind = false;
            SoundSynthesizer.INSTANCE.playToggleOn();
            return true;
        }

        if (focusedText != null) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                String val = focusedText.getValue();
                if (!val.isEmpty()) {
                    focusedText.setValue(val.substring(0, val.length() - 1));
                    Config.INSTANCE.save();
                    SoundSynthesizer.INSTANCE.playTick();
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                focusedText = null;
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (focusedText != null && chr >= 32 && chr < 127) {
            focusedText.setValue(focusedText.getValue() + chr);
            Config.INSTANCE.save();
            SoundSynthesizer.INSTANCE.playTick();
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        Config.INSTANCE.save();
        if (client != null) {
            client.setScreen(new HackMenuScreen());
        }
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
