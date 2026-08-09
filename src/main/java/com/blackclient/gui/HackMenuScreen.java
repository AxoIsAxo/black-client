package com.blackclient.gui;

import com.blackclient.config.Config;
import com.blackclient.gui.GuiUtil.Rect;
import com.blackclient.hack.Hack;
import com.blackclient.hack.HackCategory;
import com.blackclient.hack.HackManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Custom main menu: hacks grouped into collapsible category sections, drawn
 * entirely with custom rectangles/text (no vanilla widget styling). Clicking
 * a hack row toggles it, clicking the ">" at the row end opens its settings,
 * clicking a header collapses/expands the group. Supports scrolling and shows
 * the hovered hack's description as a tooltip.
 */
public class HackMenuScreen extends Screen {

    private static final int PANEL_WIDTH = 232;
    private static final int TITLE_HEIGHT = 24;
    private static final int HEADER_HEIGHT = 21;
    private static final int ROW_HEIGHT = 19;
    private static final int GAP = 5;
    private static final int SETTINGS_BUTTON_WIDTH = 22;

    /** Remember collapse state for the whole session. */
    private static final Map<HackCategory, Boolean> EXPANDED = new EnumMap<>(HackCategory.class);

    private record Clickable(Rect bounds, Runnable action) {
    }

    private record HeaderEntry(HackCategory category, Rect bounds) {
    }

    private record RowEntry(Hack hack, Rect bounds, Rect settingsBounds) {
    }

    private final List<Clickable> clickables = new ArrayList<>();
    private final List<HeaderEntry> headers = new ArrayList<>();
    private final List<RowEntry> rows = new ArrayList<>();
    private double scroll;
    private int panelX;
    private int panelY;
    private int panelHeight;

    public HackMenuScreen() {
        super(Text.literal("Black Client"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        buildLayout();

        GuiUtil.rect(context, 0, 0, width, height, 0x55000000);
        GuiUtil.rect(context, panelX, panelY, PANEL_WIDTH, panelHeight, GuiUtil.BG);
        GuiUtil.border(context, panelX, panelY, PANEL_WIDTH, panelHeight, GuiUtil.BORDER);
        GuiUtil.textGlitchCentered(context, client.textRenderer, "Black Client", panelX + PANEL_WIDTH / 2, panelY + 7);

        context.enableScissor(panelX + 5, panelY + TITLE_HEIGHT, panelX + PANEL_WIDTH - 5, panelY + panelHeight - 4);
        for (HeaderEntry header : headers) {
            drawHeader(context, header, mouseX, mouseY);
        }
        for (RowEntry row : rows) {
            drawRow(context, row, mouseX, mouseY);
        }
        context.disableScissor();

        drawTooltip(context, mouseX, mouseY);
    }

    private void drawHeader(DrawContext context, HeaderEntry header, int mouseX, int mouseY) {
        Rect bounds = header.bounds();
        int color = (header.category().getColor() & 0x00FFFFFF) | 0x44000000;
        GuiUtil.rect(context, bounds.x(), bounds.y(), bounds.w(), bounds.h(), color);
        if (GuiUtil.hovered(mouseX, mouseY, bounds)) {
            GuiUtil.rect(context, bounds.x(), bounds.y(), bounds.w(), bounds.h(), GuiUtil.HOVER);
        }

        List<Hack> hacks = hacksIn(header.category());
        long enabled = hacks.stream().filter(Hack::isEnabled).count();
        String title = header.category().getName() + "  (" + enabled + "/" + hacks.size() + ")";
        GuiUtil.text(context, client.textRenderer, title, bounds.x() + 6, bounds.y() + 5, GuiUtil.TEXT);

        boolean expanded = isExpanded(header.category());
        String indicator = expanded ? "-" : "+";
        GuiUtil.text(context, client.textRenderer, indicator,
                bounds.x() + bounds.w() - 14, bounds.y() + 5, GuiUtil.MUTED);
    }

    private void drawRow(DrawContext context, RowEntry row, int mouseX, int mouseY) {
        Rect bounds = row.bounds();
        if (GuiUtil.hovered(mouseX, mouseY, bounds)) {
            GuiUtil.rect(context, bounds.x(), bounds.y(), bounds.w(), bounds.h(), GuiUtil.HOVER);
            GuiUtil.glitchBars(context, bounds.x() + 1, bounds.y() + 1, bounds.w() - 2, bounds.h() - 2, System.currentTimeMillis(), 47);
        }
        GuiUtil.text(context, client.textRenderer, row.hack().getName(), bounds.x() + 6, bounds.y() + 5, GuiUtil.TEXT);

        String status = row.hack().isEnabled() ? "ON" : "OFF";
        int statusColor = row.hack().isEnabled() ? GuiUtil.ON : GuiUtil.OFF;
        int statusX = bounds.x() + bounds.w() - SETTINGS_BUTTON_WIDTH - client.textRenderer.getWidth(status) - 8;
        GuiUtil.text(context, client.textRenderer, status, statusX, bounds.y() + 5, statusColor);

        Rect settings = row.settingsBounds();
        if (GuiUtil.hovered(mouseX, mouseY, settings)) {
            GuiUtil.rect(context, settings.x(), settings.y(), settings.w(), settings.h(), GuiUtil.HOVER);
        }
        GuiUtil.textCentered(context, client.textRenderer, ">", settings.x() + settings.w() / 2, settings.y() + 5, GuiUtil.MUTED);
    }

    private void drawTooltip(DrawContext context, int mouseX, int mouseY) {
        for (RowEntry row : rows) {
            if (GuiUtil.hovered(mouseX, mouseY, row.bounds())) {
                String description = row.hack().getDescription();
                int textWidth = client.textRenderer.getWidth(description);
                int x = panelX + PANEL_WIDTH / 2 - textWidth / 2;
                int y = panelY + panelHeight - 14;
                GuiUtil.rect(context, x - 4, y - 2, textWidth + 8, 10, 0xE0000000);
                GuiUtil.text(context, client.textRenderer, description, x, y, GuiUtil.MUTED);
                return;
            }
        }
    }

    private void buildLayout() {
        clickables.clear();
        headers.clear();
        rows.clear();

        panelX = (width - PANEL_WIDTH) / 2;
        panelY = 30;

        // Natural content height is independent of the scroll offset; clamping
        // against a height that already shrank by scroll made every scroll
        // snap back toward the top.
        int naturalHeight = contentHeight();
        panelHeight = Math.min(naturalHeight, height - panelY - 20);
        scroll = naturalHeight <= panelHeight
                ? 0
                : Math.max(0, Math.min(scroll, naturalHeight - panelHeight));

        int x = panelX + 6;
        int contentWidth = PANEL_WIDTH - 12;
        int y = panelY + TITLE_HEIGHT - (int) scroll;

        for (HackCategory category : HackCategory.values()) {
            List<Hack> hacks = hacksIn(category);
            if (hacks.isEmpty()) {
                continue;
            }

            Rect header = new Rect(x, y, contentWidth, HEADER_HEIGHT);
            headers.add(new HeaderEntry(category, header));
            clickables.add(new Clickable(header, () -> toggleExpanded(category)));
            y += HEADER_HEIGHT;

            if (isExpanded(category)) {
                for (Hack hack : hacks) {
                    Rect row = new Rect(x, y, contentWidth, ROW_HEIGHT);
                    Rect settingsButton = new Rect(x + contentWidth - SETTINGS_BUTTON_WIDTH, y, SETTINGS_BUTTON_WIDTH, ROW_HEIGHT);
                    rows.add(new RowEntry(hack, row, settingsButton));
                    // The settings button overlaps the row, so it must be
                    // checked first or the row toggle swallows the click.
                    clickables.add(new Clickable(settingsButton, () -> client.setScreen(new HackSettingsScreen(hack))));
                    clickables.add(new Clickable(row, () -> toggleHack(hack)));
                    y += ROW_HEIGHT;
                }
            }
            y += GAP;
        }
    }

    private int contentHeight() {
        int h = TITLE_HEIGHT + 8;
        for (HackCategory category : HackCategory.values()) {
            List<Hack> hacks = hacksIn(category);
            if (hacks.isEmpty()) {
                continue;
            }
            h += HEADER_HEIGHT;
            if (isExpanded(category)) {
                h += hacks.size() * ROW_HEIGHT;
            }
            h += GAP;
        }
        return h;
    }

    private List<Hack> hacksIn(HackCategory category) {
        List<Hack> result = new ArrayList<>();
        for (Hack hack : HackManager.INSTANCE.getHacks()) {
            if (hack.getCategory() == category) {
                result.add(hack);
            }
        }
        return result;
    }

    private void toggleHack(Hack hack) {
        hack.toggle();
        Config.INSTANCE.save();
    }

    private void toggleExpanded(HackCategory category) {
        EXPANDED.put(category, !isExpanded(category));
    }

    private boolean isExpanded(HackCategory category) {
        return EXPANDED.getOrDefault(category, true);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        buildLayout();
        if (button == 0) {
            for (Clickable clickable : clickables) {
                if (clickable.bounds().contains((int) mouseX, (int) mouseY)) {
                    clickable.action().run();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scroll -= verticalAmount * 12.0;
        buildLayout(); // clamp
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
