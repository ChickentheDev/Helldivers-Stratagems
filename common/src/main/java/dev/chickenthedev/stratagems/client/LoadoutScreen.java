package dev.chickenthedev.stratagems.client;

import dev.chickenthedev.stratagems.Stratagem;
import dev.chickenthedev.stratagems.StratagemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

// Pick which 4 stratagems the uplink has equipped: every stratagem grouped by
// category with its arrow code, click to toggle, Confirm sends the loadout.
// Renders its own scrollable list (fixed row height, wheel scroll, click by
// row) instead of ObjectSelectionList, whose 26.3 entry layout drew every
// row on top of the first one.
public class LoadoutScreen extends Screen {

    private static final String[] CATEGORIES = {"Offensive", "Supply", "Defensive", "Mission"};
    private static final int ROW_H = 20;
    private static final int LIST_TOP = 40;
    private static final int LIST_BOTTOM_PAD = 48; // space for the buttons below
    private static final int SCROLLBAR_W = 6;

    private final Set<String> selected = new LinkedHashSet<>();
    private final List<Row> rows = new ArrayList<>();
    private Button confirmButton;
    private int scrollOffset = 0;
    private int hoveredRow = -1;

    private sealed interface Row permits CategoryRow, StratagemRow {}
    private record CategoryRow(String category) implements Row {}
    private record StratagemRow(Stratagem stratagem) implements Row {}

    public LoadoutScreen(List<String> current) {
        super(Component.translatable("screen.stratagems.loadout.title"));
        selected.addAll(current);
    }

    @Override
    protected void init() {
        rows.clear();
        for (String category : CATEGORIES) {
            rows.add(new CategoryRow(category));
            for (Stratagem s : StratagemRegistry.all()) {
                if (s.category.equals(category)) rows.add(new StratagemRow(s));
            }
        }
        scrollOffset = 0;

        confirmButton = Button.builder(Component.translatable("screen.stratagems.loadout.confirm"),
                        b -> onConfirm())
                .bounds(width / 2 - 124, height - 28, 120, 20).build();
        addRenderableWidget(confirmButton);
        addRenderableWidget(Button.builder(Component.translatable("screen.stratagems.loadout.cancel"),
                        b -> onClose())
                .bounds(width / 2 + 4, height - 28, 120, 20).build());
        refreshButtons();
    }

    private int listWidth() {
        return Math.min(360, width - 40);
    }

    private int listX() {
        return (width - listWidth()) / 2;
    }

    private int listBottom() {
        return height - LIST_BOTTOM_PAD;
    }

    private int visibleHeight() {
        return listBottom() - LIST_TOP;
    }

    private int contentHeight() {
        return rows.size() * ROW_H;
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - visibleHeight());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 12, 0xFFFFD500);
        String count = selected.size() + " / 4 selected";
        graphics.centeredText(font, count, width / 2, 26,
                selected.size() == 4 ? 0xFF55FF55 : 0xFFFFFFFF);

        scrollOffset = Math.min(scrollOffset, maxScroll());
        int x = listX();
        int w = listWidth();
        int top = LIST_TOP;
        int bottom = listBottom();

        // List background.
        graphics.fill(x, top, x + w, bottom, 0xC0101010);

        // Hovered row (mouse position arrives with the render state).
        hoveredRow = -1;
        if (mouseX >= x && mouseX < x + w && mouseY >= top && mouseY < bottom) {
            int idx = (mouseY - top + scrollOffset) / ROW_H;
            if (idx >= 0 && idx < rows.size() && rows.get(idx) instanceof StratagemRow) hoveredRow = idx;
        }

        for (int i = 0; i < rows.size(); i++) {
            int y = top + i * ROW_H - scrollOffset;
            if (y + ROW_H < top || y > bottom) continue;
            Row row = rows.get(i);
            if (i == hoveredRow) {
                graphics.fill(x, y, x + w, y + ROW_H, 0x33FFFFFF);
            }
            if (row instanceof CategoryRow cr) {
                graphics.text(font, cr.category(), x + 4, y + 5, 0xFFFFD500);
            } else if (row instanceof StratagemRow sr) {
                Stratagem s = sr.stratagem();
                boolean sel = isSelected(s.id);
                int color = sel ? 0xFFFFD500 : 0xFFFFFFFF;
                graphics.text(font, (sel ? "[x] " : "[ ] ") + s.name, x + 4, y + 5, color);
                String code = s.codeString();
                graphics.text(font, code, x + w - font.width(code) - 4, y + 5,
                        sel ? 0xFFFFD500 : 0xFFAAAAAA);
            }
        }

        // Scrollbar.
        if (contentHeight() > visibleHeight()) {
            int barX = x + w - SCROLLBAR_W;
            graphics.fill(barX, top, barX + SCROLLBAR_W, bottom, 0xFF000000);
            int thumbH = Math.max(20, visibleHeight() * visibleHeight() / contentHeight());
            int thumbY = top + (maxScroll() == 0 ? 0
                    : scrollOffset * (visibleHeight() - thumbH) / maxScroll());
            graphics.fill(barX, thumbY, barX + SCROLLBAR_W, thumbY + thumbH, 0xFF808080);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0) {
            int mx = (int) event.x();
            int my = (int) event.y();
            int x = listX();
            int w = listWidth();
            if (mx >= x && mx < x + w && my >= LIST_TOP && my < listBottom()) {
                int idx = (my - LIST_TOP + scrollOffset) / ROW_H;
                if (idx >= 0 && idx < rows.size() && rows.get(idx) instanceof StratagemRow sr) {
                    toggle(sr.stratagem().id);
                    return true;
                }
            }
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x = listX();
        int w = listWidth();
        if (mouseX >= x && mouseX < x + w && mouseY >= LIST_TOP && mouseY < listBottom()) {
            scrollOffset = Math.max(0, Math.min(maxScroll(),
                    scrollOffset - (int) (scrollY * ROW_H)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isSelected(String id) {
        return selected.contains(id);
    }

    private void toggle(String id) {
        if (selected.contains(id)) {
            selected.remove(id);
        } else if (selected.size() >= 4) {
            // Full loadout: clicking a new stratagem swaps it in for the
            // oldest pick, so selection always works instead of refusing.
            String oldest = selected.iterator().next();
            selected.remove(oldest);
            selected.add(id);
        } else {
            selected.add(id);
        }
        Minecraft.getInstance().getSoundManager()
                .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        refreshButtons();
    }

    private void refreshButtons() {
        if (confirmButton != null) confirmButton.active = selected.size() == 4;
    }

    private void onConfirm() {
        if (selected.size() != 4) return;
        ClientStratagemInput.sendLoadout(new ArrayList<>(selected));
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2F));
        onClose();
    }
}
