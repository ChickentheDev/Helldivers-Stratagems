package dev.chickenthedev.stratagems.client;

import dev.chickenthedev.stratagems.Stratagem;
import dev.chickenthedev.stratagems.StratagemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
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
public class LoadoutScreen extends Screen {

    private static final String[] CATEGORIES = {"Offensive", "Supply", "Defensive", "Mission"};

    private final Set<String> selected = new LinkedHashSet<>();
    private LoadoutList list;
    private Button confirmButton;
    private String error;

    public LoadoutScreen(List<String> current) {
        super(Component.translatable("screen.stratagems.loadout.title"));
        selected.addAll(current);
    }

    @Override
    protected void init() {
        int listWidth = Math.min(360, width - 40);
        list = new LoadoutList(minecraft, listWidth, height - 96, 40, 20);
        list.setX((width - listWidth) / 2);
        list.fill();
        addRenderableWidget(list);

        confirmButton = Button.builder(Component.translatable("screen.stratagems.loadout.confirm"),
                        b -> onConfirm())
                .bounds(width / 2 - 124, height - 28, 120, 20).build();
        addRenderableWidget(confirmButton);
        addRenderableWidget(Button.builder(Component.translatable("screen.stratagems.loadout.cancel"),
                        b -> onClose())
                .bounds(width / 2 + 4, height - 28, 120, 20).build());
        refreshButtons();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        graphics.centeredText(font, title, width / 2, 12, 0xFFFFD500);
        String count = selected.size() + " / 4 selected";
        graphics.centeredText(font, count, width / 2, 26,
                selected.size() == 4 ? 0xFF55FF55 : 0xFFFFFFFF);
        if (error != null) {
            graphics.centeredText(font, error, width / 2, height - 44, 0xFFFF5555);
        }
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
            error = null;
        } else if (selected.size() >= 4) {
            error = Component.translatable("message.stratagems.loadout_full").getString();
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS, 0.8F));
        } else {
            selected.add(id);
            error = null;
        }
        refreshButtons();
    }

    private void refreshButtons() {
        if (confirmButton != null) confirmButton.active = selected.size() == 4;
    }

    private void onConfirm() {
        if (selected.size() != 4) return;
        ClientStratagemInput.sendLoadout(new ArrayList<>(selected));
        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2F));
        onClose();
    }

    private class LoadoutList extends ObjectSelectionList<LoadoutList.Entry> {

        LoadoutList(Minecraft mc, int width, int height, int top, int itemHeight) {
            super(mc, width, height, top, itemHeight);
        }

        @Override
        public int getRowWidth() {
            return width - 8;
        }

        /** Populate the list (addEntry is protected on the list itself). */
        void fill() {
            for (String category : CATEGORIES) {
                addEntry(new CategoryEntry(category));
                for (Stratagem s : StratagemRegistry.all()) {
                    if (s.category.equals(category)) addEntry(new StratagemEntry(s));
                }
            }
        }

        abstract class Entry extends ObjectSelectionList.Entry<Entry> {
        }

        class CategoryEntry extends Entry {
            private final String category;

            CategoryEntry(String category) {
                this.category = category;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int index, int y,
                                       boolean hovering, float partialTick) {
                graphics.text(font, category, getX() + 4, y + 5, 0xFFFFD500);
            }

            @Override
            public Component getNarration() {
                return Component.literal(category);
            }
        }

        class StratagemEntry extends Entry {
            private final Stratagem stratagem;

            StratagemEntry(Stratagem stratagem) {
                this.stratagem = stratagem;
            }

            @Override
            public void extractContent(GuiGraphicsExtractor graphics, int index, int y,
                                       boolean hovering, float partialTick) {
                boolean sel = isSelected(stratagem.id);
                int x = getX();
                int w = getWidth();
                if (hovering) graphics.fill(x, y, x + w, y + getHeight(), 0x33FFFFFF);
                String check = sel ? "[x] " : "[ ] ";
                int color = sel ? 0xFFFFD500 : 0xFFFFFFFF;
                graphics.text(font, check + stratagem.name, x + 4, y + 5, color);
                String code = stratagem.codeString();
                graphics.text(font, code, x + w - font.width(code) - 4, y + 5,
                        sel ? 0xFFFFD500 : 0xFFAAAAAA);
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
                if (event.button() == 0) {
                    toggle(stratagem.id);
                    return true;
                }
                return false;
            }

            @Override
            public Component getNarration() {
                return Component.literal(stratagem.name + " " + stratagem.codeString());
            }
        }
    }
}
