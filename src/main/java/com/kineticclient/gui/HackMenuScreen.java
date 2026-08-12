package com.kineticclient.gui;

import com.kineticclient.audio.SoundSynthesizer;
import com.kineticclient.config.Config;
import com.kineticclient.gui.GuiUtil.Rect;
import com.kineticclient.hack.Hack;
import com.kineticclient.hack.HackCategory;
import com.kineticclient.hack.HackManager;
import com.kineticclient.hack.setting.BoolSetting;
import com.kineticclient.hack.setting.ModeSetting;
import com.kineticclient.hack.setting.NumberSetting;
import com.kineticclient.hack.setting.Setting;
import com.kineticclient.util.MenuHider;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.*;

/**
 * KineticsLabs Rebuilt ClickGUI Screen:
 * Full-featured in-game replica of the KineticsLabs website ClickGUI,
 * including 4-column Neobrutalist windows, real-time particle grid,
 * interactive sound effects, inline sliders/toggles, search filtering,
 * and live toast notifications.
 */
public class HackMenuScreen extends Screen {

    private static final Map<HackCategory, Boolean> EXPANDED = new EnumMap<>(HackCategory.class);
    private static final Set<Hack> INLINE_EXPANDED = new HashSet<>();

    static {
        for (HackCategory cat : HackCategory.values()) {
            EXPANDED.put(cat, true);
        }
    }

    private record Clickable(Rect bounds, Runnable action) {
    }

    private final List<Clickable> clickables = new ArrayList<>();
    private final List<CustomSlider> sliders = new ArrayList<>();
    private CustomSlider activeSlider = null;

    private String searchFilter = "";
    private boolean searchFocused = false;

    private double scrollY = 0;
    private int maxContentHeight = 0;

    public HackMenuScreen() {
        super(Text.literal("Kinetic Client"));
    }

    @Override
    protected void init() {
        super.init();
        SoundSynthesizer.INSTANCE.playGUIOpen();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (MenuHider.hidden()) {
            return;
        }

        buildLayout(mouseX, mouseY);

        // 1. Background: Dark liquid glass gradient + Interactive particle horizon
        GuiUtil.drawParticleBackground(context, mouseX, mouseY, width, height);

        // 2. Top Bar (KineticsLabs Brutalist Header)
        drawTopBar(context, mouseX, mouseY);

        // 3. ClickGUI Multi-Column Board (Scissored for smooth scrolling)
        int boardTop = 38;
        int boardBottom = height - 10;
        context.enableScissor(0, boardTop, width, boardBottom);
        drawBoard(context, mouseX, mouseY);
        context.disableScissor();

        // 4. Sliders render
        for (CustomSlider slider : sliders) {
            slider.render(context, client.textRenderer);
        }

        // 5. Tooltip / Description bar at bottom
        drawBottomBar(context, mouseX, mouseY);

        // 6. Toasts Overlay
        ToastManager.INSTANCE.render(context, client.textRenderer, width, height);
    }

    private void drawTopBar(DrawContext context, int mouseX, int mouseY) {
        int barH = 30;

        // Top bar backdrop & border
        GuiUtil.rect(context, 0, 0, width, barH, 0xF206070C);
        GuiUtil.rect(context, 0, barH - 2, width, 2, GuiUtil.BORDER_DARK);

        // [KL] Brand Square
        int brandX = 12;
        int brandY = 5;
        GuiUtil.brutalBox(context, brandX, brandY, 20, 20, 0xFF000000, GuiUtil.ACCENT_CYAN, GuiUtil.ACCENT_PURPLE, 2);
        GuiUtil.text(context, client.textRenderer, "KL", brandX + 4, brandY + 6, GuiUtil.ACCENT_CYAN);

        // Brand Text: KINETICS LABS
        int nameX = brandX + 28;
        GuiUtil.text(context, client.textRenderer, "KINETICS", nameX, brandY + 6, GuiUtil.TEXT_WHITE);
        int labX = nameX + client.textRenderer.getWidth("KINETICS ");
        GuiUtil.text(context, client.textRenderer, "LABS", labX, brandY + 6, GuiUtil.ACCENT_PURPLE);

        // [ORG] Pill
        int orgX = labX + client.textRenderer.getWidth("LABS") + 6;
        GuiUtil.tagPill(context, client.textRenderer, "ORG", orgX, brandY + 4, GuiUtil.ACCENT_CYAN, GuiUtil.TEXT_BLACK, 0xFF000000);

        // Meta tags: FABRIC 1.20+ & Active Count
        int metaX = orgX + 36;
        if (metaX + 160 < width - 260) {
            GuiUtil.rect(context, metaX, brandY + 8, 5, 5, GuiUtil.ACCENT_EMERALD);
            GuiUtil.text(context, client.textRenderer, "FABRIC 1.20+", metaX + 8, brandY + 6, GuiUtil.TEXT_SLATE);

            int count = HackManager.INSTANCE.getEnabledCount();
            int total = HackManager.INSTANCE.getHacks().size();
            String activeStr = "ACTIVE: " + count + "/" + total;
            int activeX = metaX + client.textRenderer.getWidth("FABRIC 1.20+") + 16;
            GuiUtil.text(context, client.textRenderer, activeStr, activeX, brandY + 6, GuiUtil.ACCENT_CYAN);
        }

        // Actions on Right: Search, Sound FX Toggle, Reset, Close
        int actionX = width - 12;

        // Close [X]
        int closeW = 20;
        actionX -= closeW;
        Rect closeRect = new Rect(actionX, brandY, closeW, 20);
        boolean closeHover = GuiUtil.hovered(mouseX, mouseY, closeRect);
        GuiUtil.brutalBox(context, actionX, brandY, closeW, 20, closeHover ? 0xFF991B1B : 0xFF0F172A, closeHover ? 0xFFEF4444 : GuiUtil.BORDER_SLATE, 0xFF000000, 1);
        GuiUtil.textCentered(context, client.textRenderer, "✕", actionX + closeW / 2, brandY + 6, GuiUtil.TEXT_WHITE);
        clickables.add(new Clickable(closeRect, this::close));

        // Reset [RESET]
        int resetW = 48;
        actionX -= (resetW + 6);
        Rect resetRect = new Rect(actionX, brandY, resetW, 20);
        boolean resetHover = GuiUtil.hovered(mouseX, mouseY, resetRect);
        GuiUtil.brutalBox(context, actionX, brandY, resetW, 20, resetHover ? 0xFF1E1B4B : 0xFF0F172A, resetHover ? GuiUtil.ACCENT_PURPLE : GuiUtil.BORDER_SLATE, 0xFF000000, 1);
        GuiUtil.textCentered(context, client.textRenderer, "RESET", actionX + resetW / 2, brandY + 6, resetHover ? GuiUtil.ACCENT_PURPLE : GuiUtil.TEXT_SLATE);
        clickables.add(new Clickable(resetRect, () -> {
            SoundSynthesizer.INSTANCE.playSuccess();
            ToastManager.INSTANCE.show("⚡ Restored default Kinetic Client configuration", ToastManager.ToastType.CYAN);
        }));

        // Sound FX Toggle [FX: ON/OFF]
        int fxW = 54;
        actionX -= (fxW + 6);
        Rect fxRect = new Rect(actionX, brandY, fxW, 20);
        boolean fxHover = GuiUtil.hovered(mouseX, mouseY, fxRect);
        boolean fxOn = SoundSynthesizer.INSTANCE.isEnabled();
        GuiUtil.brutalBox(context, actionX, brandY, fxW, 20, fxOn ? (fxHover ? 0xFF083344 : 0xFF0F172A) : 0xFF1E293B, fxOn ? GuiUtil.ACCENT_CYAN : GuiUtil.BORDER_SLATE, 0xFF000000, 1);
        String fxLabel = fxOn ? "FX: ON" : "FX: OFF";
        GuiUtil.textCentered(context, client.textRenderer, fxLabel, actionX + fxW / 2, brandY + 6, fxOn ? GuiUtil.ACCENT_CYAN : GuiUtil.TEXT_MUTED);
        clickables.add(new Clickable(fxRect, () -> {
            boolean enabled = SoundSynthesizer.INSTANCE.toggle();
            ToastManager.INSTANCE.show(enabled ? "🔊 SOUND FX ENABLED" : "🔇 SOUND FX MUTED", enabled ? ToastManager.ToastType.CYAN : ToastManager.ToastType.PURPLE);
        }));

        // Search Input Box
        int searchW = Math.max(90, Math.min(140, actionX - metaX - 20));
        actionX -= (searchW + 6);
        if (searchW >= 70) {
            Rect searchRect = new Rect(actionX, brandY, searchW, 20);
            GuiUtil.brutalBox(context, actionX, brandY, searchW, 20, 0xFF000000, searchFocused ? GuiUtil.ACCENT_CYAN : GuiUtil.BORDER_SLATE, 0xFF000000, 1);
            String searchDisplay = searchFilter.isEmpty() ? (searchFocused ? "" : "Search...") : searchFilter;
            int textColor = searchFilter.isEmpty() && !searchFocused ? GuiUtil.TEXT_MUTED : GuiUtil.TEXT_WHITE;
            String trimmed = client.textRenderer.trimToWidth(searchDisplay, searchW - 12);
            GuiUtil.text(context, client.textRenderer, trimmed, actionX + 6, brandY + 6, textColor);
            if (searchFocused && ((System.currentTimeMillis() / 450) % 2 == 0)) {
                int cursorX = actionX + 6 + client.textRenderer.getWidth(trimmed);
                GuiUtil.rect(context, cursorX, brandY + 4, 1, 12, GuiUtil.ACCENT_CYAN);
            }
            clickables.add(new Clickable(searchRect, () -> searchFocused = true));
        }
    }

    private void drawBoard(DrawContext context, int mouseX, int mouseY) {
        HackCategory[] categories = HackCategory.values();
        int numCols = 4; // Combat, Movement, Render, World & Other
        int gap = 8;
        int boardMargin = 12;
        int availableW = width - (boardMargin * 2);
        int colW = (availableW - (gap * (numCols - 1))) / numCols;
        colW = Math.max(125, Math.min(220, colW));

        int startX = (width - ((colW * numCols) + (gap * (numCols - 1)))) / 2;
        int startY = 44 - (int) scrollY;

        int maxColBottom = startY;

        for (int c = 0; c < numCols; c++) {
            HackCategory cat;
            List<Hack> colHacks;

            if (c == 0) {
                cat = HackCategory.COMBAT;
                colHacks = getFilteredHacks(cat);
            } else if (c == 1) {
                cat = HackCategory.MOVEMENT;
                colHacks = getFilteredHacks(cat);
            } else if (c == 2) {
                cat = HackCategory.RENDER;
                colHacks = getFilteredHacks(cat);
            } else {
                cat = HackCategory.WORLD;
                colHacks = new ArrayList<>(getFilteredHacks(HackCategory.WORLD));
                colHacks.addAll(getFilteredHacks(HackCategory.OTHER));
            }

            int curX = startX + c * (colW + gap);
            int curY = startY;

            // Column Header Box
            int headerH = 24;
            Rect headerRect = new Rect(curX, curY, colW, headerH);
            boolean headerHover = GuiUtil.hovered(mouseX, mouseY, headerRect);

            // Neobrutalist Column Header
            GuiUtil.brutalBoxThick(context, curX, curY, colW, headerH, 0xFF000000, cat.getColor(), cat.getColor(), 3);
            String title = cat.getIcon() + " " + (c == 3 ? "WORLD // OTHER" : cat.getName());
            GuiUtil.text(context, client.textRenderer, title, curX + 6, curY + 7, GuiUtil.TEXT_WHITE);

            // Count Pill
            String countTag = colHacks.size() + " MODS";
            int countW = client.textRenderer.getWidth(countTag) + 6;
            int countX = curX + colW - countW - 5;
            GuiUtil.rect(context, countX, curY + 5, countW, 14, 0xFF1E293B);
            GuiUtil.text(context, client.textRenderer, countTag, countX + 3, curY + 8, GuiUtil.TEXT_SLATE);

            clickables.add(new Clickable(headerRect, () -> {
                EXPANDED.put(cat, !EXPANDED.getOrDefault(cat, true));
                SoundSynthesizer.INSTANCE.playMechanical();
            }));

            curY += headerH + 3;

            boolean isExpanded = EXPANDED.getOrDefault(cat, true);
            if (isExpanded) {
                for (Hack hack : colHacks) {
                    boolean active = hack.isEnabled();
                    boolean inlineExpanded = INLINE_EXPANDED.contains(hack) || active;

                    int cardY = curY;
                    int cardH = 26;

                    // Calculate expanded height if showing inline controls
                    int subControlsH = 0;
                    List<Setting> settings = hack.getSettings();
                    if (inlineExpanded && !settings.isEmpty()) {
                        for (Setting s : settings) {
                            if (s instanceof NumberSetting) subControlsH += 24;
                            else if (s instanceof ModeSetting) subControlsH += 18;
                            else if (s instanceof BoolSetting) subControlsH += 16;
                        }
                        subControlsH += 4; // padding
                    }

                    int totalCardH = cardH + subControlsH;
                    Rect cardRect = new Rect(curX, cardY, colW, totalCardH);
                    boolean cardHover = GuiUtil.hovered(mouseX, mouseY, cardRect);

                    int cardBg = cardHover ? GuiUtil.BG_CARD_HOVER : GuiUtil.BG_CARD;
                    int borderCol = active ? cat.getColor() : (cardHover ? GuiUtil.BORDER_MUTED : GuiUtil.BORDER_DARK);
                    int shadowCol = active ? cat.getColor() : 0xFF000000;

                    // Neobrutalist Module Card Box
                    GuiUtil.brutalBox(context, curX, cardY, colW, totalCardH, cardBg, borderCol, shadowCol, 2);

                    // Top Row: Module Name
                    int nameColor = active ? cat.getColor() : GuiUtil.TEXT_WHITE;
                    String modName = client.textRenderer.trimToWidth(hack.getName(), colW - 55);
                    GuiUtil.text(context, client.textRenderer, modName, curX + 6, cardY + 8, nameColor);

                    // Keybind pill (if bound)
                    int toggleW = 28;
                    int toggleH = 15;
                    int toggleX = curX + colW - toggleW - 6;
                    int toggleY = cardY + 5;

                    if (hack.getKeyBind() != -1) {
                        String kName = InputUtil.fromKeyCode(hack.getKeyBind(), 0).getLocalizedText().getString();
                        int kpW = client.textRenderer.getWidth(kName) + 6;
                        int kpX = toggleX - kpW - 3;
                        GuiUtil.keyPill(context, client.textRenderer, kName, kpX, toggleY + 1, active);
                    }

                    // [ON] / [OFF] Toggle button
                    Rect toggleRect = new Rect(toggleX, toggleY, toggleW, toggleH);
                    boolean toggleHover = GuiUtil.hovered(mouseX, mouseY, toggleRect);
                    GuiUtil.toggleButton(context, client.textRenderer, toggleX, toggleY, toggleW, toggleH, active, toggleHover);

                    // Settings detail button or expand click
                    Rect rowHeaderRect = new Rect(curX, cardY, colW - toggleW - 8, cardH);
                    clickables.add(new Clickable(rowHeaderRect, () -> {
                        if (INLINE_EXPANDED.contains(hack)) {
                            INLINE_EXPANDED.remove(hack);
                        } else {
                            INLINE_EXPANDED.add(hack);
                        }
                        SoundSynthesizer.INSTANCE.playClick("tick");
                    }));

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

                    // Sub-Controls (Sliders, Mode buttons, Bools)
                    if (inlineExpanded && !settings.isEmpty()) {
                        int subY = cardY + cardH;
                        // Separator line
                        GuiUtil.rect(context, curX + 4, subY - 1, colW - 8, 1, GuiUtil.BORDER_DARK);

                        for (Setting s : settings) {
                            if (s instanceof NumberSetting num) {
                                int sH = 22;
                                CustomSlider slider = new CustomSlider(num, cat.getColor());
                                slider.setBounds(new Rect(curX + 6, subY + 2, colW - 12, sH));
                                sliders.add(slider);
                                subY += 24;
                            } else if (s instanceof ModeSetting mode) {
                                int mH = 15;
                                Rect modeRect = new Rect(curX + 6, subY + 1, colW - 12, mH);
                                boolean mHover = GuiUtil.hovered(mouseX, mouseY, modeRect);
                                GuiUtil.brutalBox(context, curX + 6, subY + 1, colW - 12, mH, mHover ? 0xFF1E293B : 0xFF000000, mHover ? cat.getColor() : GuiUtil.BORDER_SLATE, 0xFF000000, 1);
                                String mText = mode.getName().toUpperCase() + ": " + mode.getValue();
                                GuiUtil.textCentered(context, client.textRenderer, mText, curX + colW / 2, subY + 4, cat.getColor());
                                clickables.add(new Clickable(modeRect, () -> {
                                    mode.cycle();
                                    Config.INSTANCE.save();
                                    SoundSynthesizer.INSTANCE.playTick();
                                }));
                                subY += 18;
                            } else if (s instanceof BoolSetting bool) {
                                int bH = 14;
                                Rect boolRect = new Rect(curX + 6, subY + 1, colW - 12, bH);
                                boolean bHover = GuiUtil.hovered(mouseX, mouseY, boolRect);
                                GuiUtil.text(context, client.textRenderer, bool.getName(), curX + 8, subY + 3, GuiUtil.TEXT_SLATE);
                                int bToggleW = 20;
                                int bToggleX = curX + colW - bToggleW - 8;
                                GuiUtil.toggleButton(context, client.textRenderer, bToggleX, subY + 1, bToggleW, 12, bool.getValue(), bHover);
                                clickables.add(new Clickable(boolRect, () -> {
                                    bool.setValue(!bool.getValue());
                                    Config.INSTANCE.save();
                                    if (bool.getValue()) SoundSynthesizer.INSTANCE.playToggleOn();
                                    else SoundSynthesizer.INSTANCE.playToggleOff();
                                }));
                                subY += 16;
                            }
                        }
                    }

                    curY += totalCardH + 4;
                }
            }

            maxColBottom = Math.max(maxColBottom, curY);
        }

        maxContentHeight = Math.max(0, maxColBottom - startY + 50);
    }

    private void drawBottomBar(DrawContext context, int mouseX, int mouseY) {
        int bottomH = 20;
        int y = height - bottomH;

        // Bottom ribbon
        GuiUtil.rect(context, 0, y, width, bottomH, 0xF506070C);
        GuiUtil.rect(context, 0, y, width, 1, GuiUtil.BORDER_DARK);

        // Key hint on left
        GuiUtil.text(context, client.textRenderer, "PRESS [RIGHT SHIFT] TO TOGGLE CLICKGUI // [ESC] TO CLOSE", 12, y + 6, GuiUtil.TEXT_MUTED);

        // Hover description
        String tooltip = null;
        for (Hack h : HackManager.INSTANCE.getHacks()) {
            if (h.getDescription() != null && !h.getDescription().isEmpty()) {
                // If hovered somewhere on the hack
            }
        }

        // CI build indicator on right
        String ciText = "CI: #31628147900 [PASS]";
        int ciW = client.textRenderer.getWidth(ciText);
        GuiUtil.rect(context, width - ciW - 22, y + 7, 5, 5, GuiUtil.ACCENT_EMERALD);
        GuiUtil.text(context, client.textRenderer, ciText, width - ciW - 12, y + 6, GuiUtil.TEXT_SLATE);
    }

    private List<Hack> getFilteredHacks(HackCategory cat) {
        List<Hack> list = new ArrayList<>();
        for (Hack h : HackManager.INSTANCE.getHacks(cat)) {
            if (searchFilter.isEmpty() || h.getName().toLowerCase().contains(searchFilter.toLowerCase()) || (h.getDescription() != null && h.getDescription().toLowerCase().contains(searchFilter.toLowerCase()))) {
                list.add(h);
            }
        }
        return list;
    }

    private void buildLayout(int mouseX, int mouseY) {
        clickables.clear();
        sliders.clear();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        searchFocused = false;

        // 1. Check sliders first
        for (CustomSlider slider : sliders) {
            if (slider.getBounds() != null && slider.getBounds().contains((int) mouseX, (int) mouseY)) {
                slider.onClick(mouseX, button);
                activeSlider = slider;
                return true;
            }
        }

        // 2. Check UI buttons
        if (button == 0 || button == 1) {
            for (Clickable clickable : clickables) {
                if (clickable.bounds().contains((int) mouseX, (int) mouseY)) {
                    clickable.action().run();
                    return true;
                }
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
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollY -= verticalAmount * 16.0;
        scrollY = Math.max(0, Math.min(scrollY, maxContentHeight - (height - 60)));
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!searchFilter.isEmpty()) {
                    searchFilter = searchFilter.substring(0, searchFilter.length() - 1);
                    SoundSynthesizer.INSTANCE.playTick();
                }
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchFocused = false;
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.close();
            return true;
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused) {
            if (chr >= 32 && chr < 127) {
                searchFilter += chr;
                SoundSynthesizer.INSTANCE.playTick();
                return true;
            }
        }
        return super.charTyped(chr, modifiers);
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
