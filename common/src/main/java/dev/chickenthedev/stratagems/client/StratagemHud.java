package dev.chickenthedev.stratagems.client;

import dev.chickenthedev.stratagems.Direction;
import dev.chickenthedev.stratagems.ModItems;
import dev.chickenthedev.stratagems.Stratagem;
import dev.chickenthedev.stratagems.StratagemLoadout;
import dev.chickenthedev.stratagems.StratagemRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Client-only. Draws the stratagem list (STRATAGEMS header + one row per
// loadout entry: category tile, name, arrow code or cooldown timer),
// the stratagem code-entry panel, and the armed indicator.
public class StratagemHud {

    private static final int YELLOW = 0xFFFFD500;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DIM = 0xFFAAAAAA;

    // Per-stratagem cooldown end (ms, System.currentTimeMillis). Filled by
    // ClientStratagemInput when a throw is detected; the server already
    // tracks the authoritative cooldowns, so this is display-only.
    private static final Map<String, Long> COOLDOWN_END_MS = new HashMap<>();

    public static void setCooldown(String id, int ticks) {
        COOLDOWN_END_MS.put(id, System.currentTimeMillis() + ticks * 50L);
    }

    public static void clearCooldowns() {
        COOLDOWN_END_MS.clear();
    }

    // List palette (Sam's StratagemHud design).
    private static final int HEADER_BG = 0xCC2A2D33;
    private static final int ROW_BG = 0x88202328;
    private static final int ROW_DIM = 0xFF7A7D82;
    private static final int LIST_YELLOW = 0xFFFFE600;

    // List layout (Sam's StratagemHud design).
    private static final int X = 6;
    private static final int ICON = 20;
    private static final int TEXT_X = X + ICON + 6;
    private static final int WIDTH = 140;
    private static final int ROW_H = 26;
    private static final int HEADER_H = 12;

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

    // Stratagem list, vertically centered on the left edge: header + one row
    // per equipped stratagem (category tile, name, arrow code or cooldown).
    private static void renderLoadoutList(GuiGraphicsExtractor graphics, Minecraft mc, ItemStack uplink) {
        List<String> ids = StratagemLoadout.get(uplink);
        if (ids.isEmpty()) return;

        Font font = mc.font;
        boolean codeMode = ClientStratagemInput.isCodeMode();
        List<Direction> buffer = ClientStratagemInput.getBuffer();

        int total = HEADER_H + ids.size() * ROW_H;
        int y = graphics.guiHeight() / 2 - total / 2;

        // Header
        graphics.fill(X, y, X + WIDTH, y + HEADER_H, HEADER_BG);
        graphics.text(font, "STRATAGEMS", X + 6, y + 2, WHITE);
        y += HEADER_H;

        for (String id : ids) {
            Stratagem s = StratagemRegistry.get(id);
            if (s == null) continue;

            long left = remainingMs(id);
            boolean cooling = left > 0;

            // A row stays "live" while the typed buffer is a prefix of its code.
            boolean matching = codeMode && !buffer.isEmpty() && startsWith(s.code, buffer);
            boolean dimmed = cooling || (codeMode && !buffer.isEmpty() && !matching);

            // Row background and category tile at the left edge.
            graphics.fill(TEXT_X - 3, y, X + WIDTH, y + ROW_H, ROW_BG);
            graphics.fill(X, y + 3, X + ICON, y + 3 + ICON, categoryColor(s.category, dimmed));
            String initial = s.name.isEmpty() ? "?" : s.name.substring(0, 1);
            graphics.text(font, initial, X + (ICON - font.width(initial)) / 2, y + 9, 0xFF101114);

            // Stratagem name
            graphics.text(font, trim(font, s.name, WIDTH - ICON - 10), TEXT_X, y + 4, dimmed ? ROW_DIM : WHITE);

            // Cooldown status or directional input sequence
            if (cooling) {
                graphics.text(font, "Cooldown " + fmt(left), TEXT_X, y + 14, dimmed ? ROW_DIM : WHITE);
            } else {
                drawCode(graphics, font, s.code, buffer, matching, TEXT_X, y + 14);
            }
            y += ROW_H;
        }
    }

    private static void drawCode(GuiGraphicsExtractor g, Font font, Direction[] code,
                                 List<Direction> buffer, boolean matching, int x, int y) {
        for (int i = 0; i < code.length; i++) {
            boolean hit = matching && i < buffer.size();
            String glyph = code[i].glyph;
            g.text(font, glyph, x, y, hit ? LIST_YELLOW : WHITE);
            x += font.width(glyph) + 3;
        }
    }

    private static boolean startsWith(Direction[] code, List<Direction> buffer) {
        if (buffer.size() > code.length) return false;
        for (int i = 0; i < buffer.size(); i++) {
            if (code[i] != buffer.get(i)) return false;
        }
        return true;
    }

    private static long remainingMs(String id) {
        Long end = COOLDOWN_END_MS.get(id);
        if (end == null) return 0;
        long left = end - System.currentTimeMillis();
        if (left <= 0) {
            COOLDOWN_END_MS.remove(id);
            return 0;
        }
        return left;
    }

    private static String fmt(long ms) {
        long sec = (ms + 999) / 1000;
        return String.format("T-%02d:%02d", sec / 60, sec % 60);
    }

    private static String trim(Font font, String s, int maxPx) {
        if (font.width(s) <= maxPx) return s;
        while (s.length() > 1 && font.width(s + "..") > maxPx) s = s.substring(0, s.length() - 1);
        return s + "..";
    }

    private static int categoryColor(String category, boolean dimmed) {
        int c = switch (category != null ? category : "Mission") {
            case "Offensive" -> 0xFFE5484D; // Red
            case "Supply"    -> 0xFF3E8EF7; // Blue
            case "Defensive" -> 0xFF46A758; // Green
            default          -> 0xFFFFE600; // Yellow (Mission/Standard)
        };
        return dimmed ? (c & 0x00FFFFFF) | 0x66000000 : c;
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
