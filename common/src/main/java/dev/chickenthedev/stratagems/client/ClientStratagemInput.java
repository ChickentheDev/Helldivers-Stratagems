package dev.chickenthedev.stratagems.client;

import dev.chickenthedev.stratagems.Constants;
import dev.chickenthedev.stratagems.Direction;
import dev.chickenthedev.stratagems.ModItems;
import dev.chickenthedev.stratagems.Stratagem;
import dev.chickenthedev.stratagems.StratagemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Client-only. Handles stratagem code entry: press the stratagem keybind while
// holding the uplink to open code mode, tap arrow keys to enter the code, and a
// full match arms the stratagem (synced to the server via packet).
public class ClientStratagemInput {

    private static final long CODE_MODE_TIMEOUT_MS = 6000;

    private static Consumer<String> armSender;
    private static boolean codeMode = false;
    private static final List<Direction> buffer = new ArrayList<>();
    private static long lastInputMs = 0;
    private static String armedDisplay = null;
    private static Component hint = null;
    private static long hintUntilMs = 0;

    public static void init(Consumer<String> sender) {
        armSender = sender;
    }

    /** Transient hint shown on the HUD (e.g. after pressing G without the uplink held). */
    public static Component getHint() {
        if (hint != null && System.currentTimeMillis() < hintUntilMs) return hint;
        hint = null;
        return null;
    }

    public static boolean isCodeMode() {
        return codeMode;
    }

    public static List<Direction> getBuffer() {
        return buffer;
    }

    public static String getArmedDisplay() {
        return armedDisplay;
    }

    private static boolean holdingUplink() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        ItemStack main = mc.player.getMainHandItem();
        ItemStack off = mc.player.getOffhandItem();
        return main.getItem() == ModItems.UPLINK.get() || off.getItem() == ModItems.UPLINK.get();
    }

    private static void blip(float pitch) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSoundManager() != null) {
            mc.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_HAT, pitch));
        }
    }

    public static void onUplinkKey() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.screen() != null) return;
        if (codeMode) {
            exitCodeMode();
            return;
        }
        if (!holdingUplink()) {
            hint = Component.translatable("message.stratagems.hold_uplink");
            hintUntilMs = System.currentTimeMillis() + 2500;
            return;
        }
        codeMode = true;
        buffer.clear();
        lastInputMs = System.currentTimeMillis();
        blip(1.0F);
    }

    public static void onDirection(Direction direction) {
        if (!codeMode) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.gui.screen() != null) {
            exitCodeMode();
            return;
        }
        buffer.add(direction);
        lastInputMs = System.currentTimeMillis();
        blip(0.9F + 0.1F * buffer.size());

        Stratagem match = StratagemRegistry.findFullMatch(buffer);
        if (match != null) {
            armedDisplay = match.name;
            if (armSender != null) armSender.accept(match.id);
            exitCodeMode();
            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2F));
            Constants.LOG.debug("Armed stratagem: {}", match.id);
            return;
        }
        if (!StratagemRegistry.hasAnyPrefix(buffer)) {
            buffer.clear();
            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS, 0.8F));
        }
    }

    // Called every client tick from the platform entrypoint.
    public static void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (codeMode) {
            if (!holdingUplink() || mc.gui.screen() != null
                    || System.currentTimeMillis() - lastInputMs > CODE_MODE_TIMEOUT_MS) {
                exitCodeMode();
            }
        }
        // The throw consumed the armed stratagem once the uplink goes on cooldown.
        if (armedDisplay != null && mc.player != null
                && mc.player.getCooldowns().isOnCooldown(new ItemStack(ModItems.UPLINK.get()))) {
            armedDisplay = null;
        }
    }

    private static void exitCodeMode() {
        codeMode = false;
        buffer.clear();
    }
}
