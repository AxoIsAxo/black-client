package com.kineticclient.gui;

import com.kineticclient.config.Config;
import com.kineticclient.gui.GuiUtil.Rect;
import com.kineticclient.hack.Hack;
import com.kineticclient.hack.impl.Keypresser;
import com.kineticclient.hack.setting.BoolSetting;
import com.kineticclient.hack.setting.ModeSetting;
import com.kineticclient.hack.setting.NumberSetting;
import com.kineticclient.hack.setting.Setting;
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
 * Custom per-hack settings screen: back button, master toggle and one custom
 * row per setting (toggle box, cycle button or slider). All custom-drawn.
 */
public class HackSettingsScreen extends Screen {

    private static final int PANEL_WIDTH = 232;
    private static final int TITLE_HEIGHT = 24;
    private static final int TOGGLE_HEIGHT = 18;
    private static final int SLIDER_HEIGHT = 26;
    private static final int TEXT_ROW_HEIGHT = 30;
    private static final int GAP = 3;

    private record Clickable(Rect bounds, Runnable action) {
    }

    private record ToggleRow(Rect bounds, boolean value, String label) {
    }

    private record ModeRow(Rect bounds, ModeSetting setting) {
    }

    private record TextRow(Rect bounds, StringSetting setting) {
    }

    private final Hack hack;
    private final List<Clickable> clickables = new ArrayList<>();
    private final List<CustomSlider> sliders = new ArrayList<>();
    private final List<ToggleRow> toggles = new ArrayList<>();
    private final List<ModeRow> modes = new ArrayList<>();
    private final List<TextRow> textFields = new ArrayList<>();
    private StringSetting focusedText;
    private Rect backBounds;
    private Rect enabledBounds;
    private Rect keybindBounds;
    private Rect holdKeyBounds;
    private boolean listeningKeybind;
    private boolean listeningHoldKey;
    private int panelX;
    private int panelY;
    private int panelHeight;

    public HackSettingsScreen(Hack hack) {
        super(Text.literal(hack.getName()));
        this.hack = hack;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Anti-capture: never render the menu on frames being captured
        // (deferred in-game screenshot) or while screen-capture software runs.
        if (MenuHider.hidden()) {
            return;
        }
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

        // Keypresser: the key it holds
        if (holdKeyBounds != null) {
            drawHoldKey(context, holdKeyBounds, mouseX, mouseY);
        }

        for (ToggleRow row : toggles) {
            drawToggle(context, row.bounds(), row.label(), row.value(), mouseX, mouseY);
        }
        for (ModeRow row : modes) {
            drawButton(context, row.bounds(), row.setting().getName() + ": " + row.setting().getValue(), mouseX, mouseY);
        }
        for (TextRow row : textFields) {
            drawTextField(context, row.bounds(), row.setting());
        }
        for (CustomSlider slider : sliders) {
            slider.render(context, client.textRenderer);
        }
    }

    private void drawTextField(DrawContext context, Rect bounds, StringSetting setting) {
        boolean focused = focusedText == setting;
        GuiUtil.text(context, client.textRenderer, setting.getName(), bounds.x() + 2, bounds.y() + 2, GuiUtil.MUTED);
        int boxY = bounds.y() + 13;
        int boxH = 13;
        GuiUtil.rect(context, bounds.x(), boxY, bounds.w(), boxH, 0xFF10101A);
        GuiUtil.border(context, bounds.x(), boxY, bounds.w(), boxH, focused ? GuiUtil.ACCENT : GuiUtil.BORDER);
        String display = client.textRenderer.trimToWidth(setting.getValue(), bounds.w() - 8);
        GuiUtil.text(context, client.textRenderer, display, bounds.x() + 3, boxY + 2, GuiUtil.TEXT);
        if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
            int cursorX = bounds.x() + 3 + client.textRenderer.getWidth(display);
            GuiUtil.rect(context, cursorX, boxY + 2, 1, 8, GuiUtil.ACCENT);
        }
    }

    private void drawHoldKey(DrawContext context, Rect bounds, int mouseX, int mouseY) {
        boolean hovered = GuiUtil.hovered(mouseX, mouseY, bounds);
        if (hovered || listeningHoldKey) {
            GuiUtil.rect(context, bounds.x(), bounds.y(), bounds.w(), bounds.h(), GuiUtil.HOVER);
            GuiUtil.glitchBars(context, bounds.x() + 1, bounds.y() + 1, bounds.w() - 2, bounds.h() - 2, System.currentTimeMillis(), 73);
        }
        GuiUtil.text(context, client.textRenderer, "Hold key", bounds.x() + 4, bounds.y() + 5, GuiUtil.TEXT);
        int heldKey = hack instanceof Keypresser keypresser ? keypresser.getHeldKey() : -1;
        String value = listeningHoldKey ? "Press a key..." : keyName(heldKey);
        int valueColor = listeningHoldKey ? GuiUtil.ACCENT : (heldKey == -1 ? GuiUtil.MUTED : GuiUtil.TEXT);
        int valueX = bounds.x() + bounds.w() - client.textRenderer.getWidth(value) - 6;
        GuiUtil.text(context, client.textRenderer, value, valueX, bounds.y() + 5, valueColor);
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
        textFields.clear();

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

        // Keypresser: a second row to set the key it should hold.
        if (hack instanceof Keypresser) {
            holdKeyBounds = new Rect(x, y, contentWidth, TOGGLE_HEIGHT);
            clickables.add(new Clickable(holdKeyBounds, () -> listeningHoldKey = true));
            y += TOGGLE_HEIGHT + GAP;
        }

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
            } else if (setting instanceof StringSetting string) {
                Rect bounds = new Rect(x, y, contentWidth, TEXT_ROW_HEIGHT);
                textFields.add(new TextRow(bounds, string));
                y += TEXT_ROW_HEIGHT + GAP;
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
        // Right-click the hold-key row to clear it.
        if (button == 2 && holdKeyBounds != null && holdKeyBounds.contains((int) mouseX, (int) mouseY)) {
            if (hack instanceof Keypresser keypresser) {
                keypresser.setHeldKey(-1);
                Config.INSTANCE.save();
            }
            listeningHoldKey = false;
            return true;
        }
        if (button == 0) {
            // Text fields: clicking one focuses it, clicking elsewhere unfocuses.
            boolean clickedField = false;
            for (TextRow row : textFields) {
                if (row.bounds().contains((int) mouseX, (int) mouseY)) {
                    focusedText = row.setting();
                    clickedField = true;
                    break;
                }
            }
            if (!clickedField) {
                focusedText = null;
            }
            if (clickedField) {
                return true;
            }
            // Clicking anywhere else cancels keybind listening.
            if (listeningKeybind && !keybindBounds.contains((int) mouseX, (int) mouseY)) {
                listeningKeybind = false;
            }
            if (listeningHoldKey && holdKeyBounds != null && !holdKeyBounds.contains((int) mouseX, (int) mouseY)) {
                listeningHoldKey = false;
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
        if (focusedText != null) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                String value = focusedText.getValue();
                if (!value.isEmpty()) {
                    focusedText.setValue(value.substring(0, value.length() - 1));
                    Config.INSTANCE.save();
                }
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                focusedText = null;
                Config.INSTANCE.save();
            }
            return true;
        }
        if (listeningHoldKey) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listeningHoldKey = false; // cancel without setting
            } else if (hack instanceof Keypresser keypresser) {
                keypresser.setHeldKey(keyCode);
                Config.INSTANCE.save();
                listeningHoldKey = false;
            }
            return true;
        }
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
    public boolean charTyped(char chr, int modifiers) {
        if (focusedText != null) {
            if (chr >= ' ' && chr != 127) {
                focusedText.setValue(focusedText.getValue() + chr);
                Config.INSTANCE.save();
            }
            return true;
        }
        return super.charTyped(chr, modifiers);
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
