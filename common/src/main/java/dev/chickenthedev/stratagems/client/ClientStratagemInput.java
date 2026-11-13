package dev.chickenthedev.stratagems.client;

import dev.chickenthedev.stratagems.Constants;
import dev.chickenthedev.stratagems.Direction;
import dev.chickenthedev.stratagems.ModItems;
import dev.chickenthedev.stratagems.Stratagem;
import dev.chickenthedev.stratagems.StratagemLoadout;
import dev.chickenthedev.stratagems.StratagemRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Client-only. Handles stratagem code entry: press the stratagem keybind with
// the uplink anywhere in the inventory to open code mode, tap arrow keys to
// enter the code, and a full match arms the stratagem (synced to the server
// via packet). Only the 4 equipped stratagems can be armed.
public class ClientStratagemInput {

    private static final long CODE_MODE_TIMEOUT_MS = 6000;

    private static Consumer<String> armSender;
    private static Consumer<List<String>> loadoutSender;
    private static boolean codeMode = false;
    private static final List<Direction> buffer = new ArrayList<>();
    private static List<String> activeLoadout = List.of();
    private static long lastInputMs = 0;
    private static String armedDisplay = null;
    private static String armedId = null;
    private static Component hint = null;
    private static long hintUntilMs = 0;
    private static net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> lastDimension = null;

    public static void init(Consumer<String> sender) {
        armSender = sender;
    }

    public static void initLoadout(Consumer<List<String>> sender) {
        loadoutSender = sender;
    }

    /** Send a confirmed 4-stratagem loadout to the server. */
    public static void sendLoadout(List<String> ids) {
        if (loadoutSender != null) loadoutSender.accept(ids);
    }

    /** Transient hint shown on the HUD (e.g. after pressing G without the uplink). */
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

    /** The uplink anywhere in the inventory (hotbar, main, or offhand) — it no
     * longer needs to be held to open code mode. The throw still needs it held. */
    public static boolean hasUplink() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return false;
        return StratagemLoadout.findUplink(mc.player) != null;
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
        if (!hasUplink()) {
            hint = Component.translatable("message.stratagems.hold_uplink");
            hintUntilMs = System.currentTimeMillis() + 2500;
            return;
        }
        codeMode = true;
        buffer.clear();
        activeLoadout = StratagemLoadout.get(StratagemLoadout.findUplink(mc.player));
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

        Stratagem match = StratagemRegistry.findFullMatchIn(activeLoadout, buffer);
        if (match != null) {
            armedDisplay = match.name;
            armedId = match.id;
            if (armSender != null) armSender.accept(match.id);
            exitCodeMode();
            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.EXPERIENCE_ORB_PICKUP, 1.2F));
            Constants.LOG.debug("Armed stratagem: {}", match.id);
            return;
        }
        if (!StratagemRegistry.hasAnyPrefixIn(activeLoadout, buffer)) {
            buffer.clear();
            Minecraft.getInstance().getSoundManager()
                    .play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS, 0.8F));
        }
    }

    // Called every client tick from the platform entrypoint.
    public static void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            // Logged out / disconnected: the server drops armed state, so the
            // HUD must not keep showing a stale "ARMED:".
            armedDisplay = null;
            armedId = null;
            StratagemHud.clearCooldowns();
            lastDimension = null;
            return;
        }
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim = mc.player.level().dimension();
        if (lastDimension != null && !lastDimension.equals(dim)) {
            // Changed dimension: destroyers (and armed state) are overworld-only.
            armedDisplay = null;
            armedId = null;
            StratagemHud.clearCooldowns();
        }
        lastDimension = dim;
        if (codeMode) {
            if (!hasUplink() || mc.gui.screen() != null
                    || System.currentTimeMillis() - lastInputMs > CODE_MODE_TIMEOUT_MS) {
                exitCodeMode();
            }
        }
        // The throw consumed the armed stratagem once the uplink goes on cooldown.
        if (armedDisplay != null && mc.player != null
                && mc.player.getCooldowns().isOnCooldown(new ItemStack(ModItems.UPLINK.get()))) {
            // Start the HUD's per-stratagem cooldown timer (display-only; the
            // server enforces the real cooldown).
            if (armedId != null) {
                Stratagem s = StratagemRegistry.get(armedId);
                if (s != null) StratagemHud.setCooldown(armedId, s.cooldownTicks());
            }
            armedDisplay = null;
            armedId = null;
        }
    }

    private static void exitCodeMode() {
        codeMode = false;
        buffer.clear();
        activeLoadout = List.of();
    }
}
