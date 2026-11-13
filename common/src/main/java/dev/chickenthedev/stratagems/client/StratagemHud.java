package dev.chickenthedev.stratagems.client;

import dev.chickenthedev.stratagems.Direction;
import dev.chickenthedev.stratagems.ModItems;
import dev.chickenthedev.stratagems.Stratagem;
import dev.chickenthedev.stratagems.StratagemLoadout;
import dev.chickenthedev.stratagems.StratagemRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// Client-only. Draws the top-left loadout list (the 4 equipped stratagems
// with their arrow codes, whenever the uplink is anywhere in the inventory),
// the stratagem code-entry panel, and the armed indicator.
public class StratagemHud {

    private static final int YELLOW = 0xFFFFD500;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DIM = 0xFFAAAAAA;

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.screen() != null) return;

        net.minecraft.network.chat.Component hint = ClientStratagemInput.getHint();
        if (hint != null) {
            centeredText(graphics, mc, hint.getString(), graphics.guiWidth() / 2,
                    graphics.guiHeight() - 60, YELLOW);
        }

        ItemStack uplink = StratagemLoadout.findUplink(mc.player);
        if (uplink != null) {
            renderLoadoutList(graphics, mc, uplink);
        }

        if (ClientStratagemInput.isCodeMode()) {
            renderCodePanel(graphics, mc);
        } else if (uplink != null) {
            String armed = ClientStratagemInput.getArmedDisplay();
            int cx = graphics.guiWidth() / 2;
            if (armed != null) {
                int y = graphics.guiHeight() - 70;
                centeredText(graphics, mc, "ARMED: " + armed, cx, y, YELLOW);
                centeredText(graphics, mc, "Right-click to throw the beacon", cx, y + 11, DIM);
            } else if (!onCooldown(mc)) {
                centeredText(graphics, mc, "Press G to enter a stratagem code", cx, graphics.guiHeight() - 60, DIM);
            }
        }
    }

    // Top-left: header + one row per equipped stratagem (code glyphs + name).
    // The armed one is yellow; all rows dim while the uplink is recharging
    // (the uplink carries one shared cooldown for every stratagem).
    private static void renderLoadoutList(GuiGraphicsExtractor graphics, Minecraft mc, ItemStack uplink) {
        List<String> loadout = StratagemLoadout.get(uplink);
        String armed = ClientStratagemInput.getArmedDisplay();
        boolean cooling = onCooldown(mc);
        int x = 8;
        int y = 8;
        graphics.text(mc.font, "STRATAGEMS", x, y, YELLOW);
        y += 11;
        for (String id : loadout) {
            Stratagem s = StratagemRegistry.get(id);
            if (s == null) continue;
            int color = cooling ? DIM : (s.name.equals(armed) ? YELLOW : WHITE);
            graphics.text(mc.font, s.codeString() + "  " + s.name, x, y, color);
            y += 11;
        }
        if (cooling) {
            graphics.text(mc.font, "Recharging...", x, y, DIM);
        }
    }

    private static void centeredText(GuiGraphicsExtractor graphics, Minecraft mc, String text, int cx, int y, int color) {
        graphics.text(mc.font, text, cx - mc.font.width(text) / 2, y, color);
    }

    private static boolean onCooldown(Minecraft mc) {
        return mc.player.getCooldowns().isOnCooldown(new ItemStack(ModItems.UPLINK.get()));
    }

    private static void renderCodePanel(GuiGraphicsExtractor graphics, Minecraft mc) {
        int cx = graphics.guiWidth() / 2;
        int y = 34;

        StringBuilder arrows = new StringBuilder();
        for (Direction d : ClientStratagemInput.getBuffer()) {
            if (arrows.length() > 0) arrows.append(' ');
            arrows.append(d.glyph);
        }
        String line = arrows.length() == 0 ? "▲ ▼ ◀ ▶" : arrows.toString();

        int textWidth = mc.font.width(line);
        int pad = 10;
        graphics.fill(cx - textWidth / 2 - pad, y - 16, cx + textWidth / 2 + pad, y + 24, 0xC0101010);
        centeredText(graphics, mc, "STRATAGEM CODE", cx, y - 10, YELLOW);
        centeredText(graphics, mc, line, cx, y + 4, arrows.length() == 0 ? DIM : WHITE);
        centeredText(graphics, mc, "G to cancel", cx, y + 32, DIM);
    }
}
