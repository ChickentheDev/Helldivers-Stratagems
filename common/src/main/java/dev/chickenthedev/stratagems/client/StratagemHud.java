package dev.chickenthedev.stratagems.client;

import dev.chickenthedev.stratagems.Direction;
import dev.chickenthedev.stratagems.ModItems;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

// Client-only. Draws the stratagem code-entry panel and the armed indicator.
public class StratagemHud {

    private static final int YELLOW = 0xFFFFD500;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int DIM = 0xFFAAAAAA;

    public static void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.screen() != null) return;

        if (ClientStratagemInput.isCodeMode()) {
            renderCodePanel(graphics, mc);
        } else if (holdingUplink(mc)) {
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

    private static void centeredText(GuiGraphicsExtractor graphics, Minecraft mc, String text, int cx, int y, int color) {
        graphics.text(mc.font, text, cx - mc.font.width(text) / 2, y, color);
    }

    private static boolean holdingUplink(Minecraft mc) {
        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();
        return main.getItem() == ModItems.UPLINK.get() || off.getItem() == ModItems.UPLINK.get();
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
