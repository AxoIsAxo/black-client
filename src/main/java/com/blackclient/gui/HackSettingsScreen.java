package com.blackclient.gui;

import com.blackclient.config.Config;
import com.blackclient.gui.GuiUtil.Rect;
import com.blackclient.hack.Hack;
import com.blackclient.hack.setting.BoolSetting;
import com.blackclient.hack.setting.ModeSetting;
import com.blackclient.hack.setting.NumberSetting;
import com.blackclient.hack.setting.Setting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom per-hack settings screen: back button, master toggle and one custom
 * row per setting (toggle box, cycle button or slider). All custom-drawn.
 */
public class HackSettingsScreen extends Screen {

    private static final int PANEL_WIDTH = 232;
    private static final int TITLE_HEIGHT = 24;
    private static final int TOGGLE_HEIGHT = 18;
    private static final int SLIDER_HEIGHT = 26;
    private static final int GAP = 3;

    private record Clickable(Rect bounds, Runnable action) {
    }

    private record ToggleRow(Rect bounds, boolean value, String label) {
    }

    private record ModeRow(Rect bounds, ModeSetting setting) {
    }

    private final Hack hack;
    private final List<Clickable> clickables = new ArrayList<>();
    private final List<CustomSlider> sliders = new ArrayList<>();
    private final List<ToggleRow> toggles = new ArrayList<>();
    private final List<ModeRow> modes = new ArrayList<>();
    private Rect backBounds;
    private Rect enabledBounds;
    private Rect keybindBounds;
    private boolean listeningKeybind;
    private int panelX;
    private int panelY;
    private int panelHeight;

    public HackSettingsScreen(Hack hack) {
        super(Text.literal(hack.getName()));
        this.hack = hack;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        buildLayout();

        GuiUtil.rect(context, 0, 0, width, height, 0x55000000);
        GuiUtil.rect(context, panelX, panelY, PANEL_WIDTH, panelHeight, GuiUtil.BG);
        GuiUtil.border(context, panelX, panelY, PANEL_WIDTH, panelHeight, GuiUtil.BORDER);
        GuiUtil.textGlitchCentered(context, client.textRenderer, hack.getName(), panelX + PANEL_WIDTH / 2, panelY + 7);

        // Back button
        drawButton(context, backBounds, "< Back", mouseX, mouseY);

        // Enabled toggle
        drawToggle(context, enabledBounds, "Enabled", hack.isEnabled(), mouseX, mouseY);

        // Keybind
        drawKeybind(context, keybindBounds, mouseX, mouseY);

        for (ToggleRow row : toggles) {
            drawToggle(context, row.bounds(), row.label(), row.value(), mouseX, mouseY);
        }
        for (ModeRow row : modes) {
            drawButton(context, row.bounds(), row.setting().getName() + ": " + row.setting().getValue(), mouseX, mouseY);
        }
        for (CustomSlider slider : sliders) {
            slider.render(context, client.textRenderer);
        }
    }

    private void drawButton(DrawContext context, Rect bounds, String label, int mouseX, int mouseY) {
        boolean hovered = GuiUtil.hovered(mouseX, mouseY, bounds);
        if (hovered) {
            GuiUtil.rect(context, bounds.x(), bounds.y(), bounds.w(), bounds.h(), GuiUtil.HOVER);
            GuiUtil.glitchBars(context, bounds.x() + 1, bounds.y() + 1, bounds.w() - 2, bounds.h() - 2, System.currentTimeMillis(), 47);
        }
        GuiUtil.border(context, bounds.x(), bounds.y(), bounds.w(), bounds.h(), GuiUtil.BORDER);
        GuiUtil.textCentered(context, client.textRenderer, label, bounds.x() + bounds.w() / 2, bounds.y() + 4, GuiUtil.TEXT);
    }

    private void drawToggle(DrawContext context, Rect bounds, String label, boolean value, int mouseX, int mouseY) {
        boolean hovered = GuiUtil.hovered(mouseX, mouseY, bounds);
        if (hovered) {
            GuiUtil.rect(context, bounds.x(), bounds.y(), bounds.w(), bounds.h(), GuiUtil.HOVER);
            GuiUtil.glitchBars(context, bounds.x() + 1, bounds.y() + 1, bounds.w() - 2, bounds.h() - 2, System.currentTimeMillis(), 59);
        }
        GuiUtil.text(context, client.textRenderer, label, bounds.x() + 4, bounds.y() + 5, GuiUtil.TEXT);
        String state = value ? "ON" : "OFF";
        int stateColor = value ? GuiUtil.ON : GuiUtil.OFF;
        int stateX = bounds.x() + bounds.w() - client.textRenderer.getWidth(state) - 6;
        GuiUtil.text(context, client.textRenderer, state, stateX, bounds.y() + 5, stateColor);
    }

    private void drawKeybind(DrawContext context, Rect bounds, int mouseX, int mouseY) {
        boolean hovered = GuiUtil.hovered(mouseX, mouseY, bounds);
        if (hovered || listeningKeybind) {
            GuiUtil.rect(context, bounds.x(), bounds.y(), bounds.w(), bounds.h(), GuiUtil.HOVER);
            GuiUtil.glitchBars(context, bounds.x() + 1, bounds.y() + 1, bounds.w() - 2, bounds.h() - 2, System.currentTimeMillis(), 67);
        }
        GuiUtil.text(context, client.textRenderer, "Keybind", bounds.x() + 4, bounds.y() + 5, GuiUtil.TEXT);
        String value = listeningKeybind ? "Press a key..." : keyName(hack.getKeyBind());
        int valueColor = listeningKeybind ? GuiUtil.ACCENT : (hack.getKeyBind() == -1 ? GuiUtil.MUTED : GuiUtil.TEXT);
        int valueX = bounds.x() + bounds.w() - client.textRenderer.getWidth(value) - 6;
        GuiUtil.text(context, client.textRenderer, value, valueX, bounds.y() + 5, valueColor);
    }

    private static String keyName(int keyCode) {
        if (keyCode == -1) {
            return "None";
        }
        return InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
    }

    private void buildLayout() {
        clickables.clear();
        sliders.clear();
        toggles.clear();
        modes.clear();

        panelX = (width - PANEL_WIDTH) / 2;
        panelY = 30;

        int x = panelX + 6;
        int contentWidth = PANEL_WIDTH - 12;

        backBounds = new Rect(x, panelY + 2, 52, 15);
        clickables.add(new Clickable(backBounds, this::close));

        int y = panelY + TITLE_HEIGHT;

        enabledBounds = new Rect(x, y, contentWidth, TOGGLE_HEIGHT);
        clickables.add(new Clickable(enabledBounds, () -> {
            hack.toggle();
            Config.INSTANCE.save();
        }));
        y += TOGGLE_HEIGHT + GAP;

        keybindBounds = new Rect(x, y, contentWidth, TOGGLE_HEIGHT);
        clickables.add(new Clickable(keybindBounds, () -> listeningKeybind = true));
        y += TOGGLE_HEIGHT + GAP;

        for (Setting setting : hack.getSettings()) {
            if (setting instanceof BoolSetting bool) {
                Rect bounds = new Rect(x, y, contentWidth, TOGGLE_HEIGHT);
                toggles.add(new ToggleRow(bounds, bool.getValue(), setting.getName()));
                clickables.add(new Clickable(bounds, () -> {
                    bool.toggle();
                    Config.INSTANCE.save();
                }));
                y += TOGGLE_HEIGHT + GAP;
            } else if (setting instanceof ModeSetting mode) {
                Rect bounds = new Rect(x, y, contentWidth, TOGGLE_HEIGHT);
                modes.add(new ModeRow(bounds, mode));
                clickables.add(new Clickable(bounds, () -> {
                    mode.cycle();
                    Config.INSTANCE.save();
                }));
                y += TOGGLE_HEIGHT + GAP;
            } else if (setting instanceof NumberSetting number) {
                Rect bounds = new Rect(x, y, contentWidth, SLIDER_HEIGHT);
                CustomSlider slider = new CustomSlider(number);
                slider.setBounds(bounds);
                sliders.add(slider);
                y += SLIDER_HEIGHT + GAP;
            }
        }

        int contentHeight = y - panelY + 12;
        panelHeight = Math.min(contentHeight, height - panelY - 20);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        buildLayout();
        // Right-click the keybind row to clear the binding.
        if (button == 2 && keybindBounds.contains((int) mouseX, (int) mouseY)) {
            hack.setKeyBind(-1);
            Config.INSTANCE.save();
            listeningKeybind = false;
            return true;
        }
        if (button == 0) {
            // Clicking anywhere else cancels keybind listening.
            if (listeningKeybind && !keybindBounds.contains((int) mouseX, (int) mouseY)) {
                listeningKeybind = false;
            }
            for (Clickable clickable : clickables) {
                if (clickable.bounds().contains((int) mouseX, (int) mouseY)) {
                    clickable.action().run();
                    return true;
                }
            }
            for (CustomSlider slider : sliders) {
                if (slider.getBounds().contains((int) mouseX, (int) mouseY)) {
                    slider.onClick(mouseX, button);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listeningKeybind) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listeningKeybind = false; // cancel without binding
            } else {
                hack.setKeyBind(keyCode);
                Config.INSTANCE.save();
                listeningKeybind = false;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (CustomSlider slider : sliders) {
            slider.onDrag(mouseX);
        }
        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (CustomSlider slider : sliders) {
            slider.onRelease();
        }
        return true;
    }

    @Override
    public void close() {
        Config.INSTANCE.save();
        super.close();
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
