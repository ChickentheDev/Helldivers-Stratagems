package dev.chickenthedev.stratagems;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;

// The player's equipped stratagem loadout: exactly 4 stratagem ids, stored on
// the uplink ItemStack itself (vanilla CUSTOM_DATA, no new component type),
// so the loadout travels with the item and syncs to the client for free.
public final class StratagemLoadout {

    public static final String KEY = "stratagems:loadout";
    public static final int SIZE = 4;

    public static final List<String> DEFAULT = List.of(
            "orbital_precision_strike",
            "eagle_500kg",
            "m_resupply",
            "s_mg");

    private StratagemLoadout() {
    }

    /** The uplink anywhere in the player's inventory (hotbar, main, offhand, or armor), or null. */
    public static ItemStack findUplink(Player player) {
        if (player == null) return null;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == ModItems.UPLINK.get()) return stack;
        }
        return null;
    }

    /** The loadout on this uplink, or the default when none is stored / an entry is invalid. */
    public static List<String> get(ItemStack uplink) {
        List<String> out = new ArrayList<>(DEFAULT);
        if (uplink == null || uplink.isEmpty()) return out;
        CustomData data = uplink.get(DataComponents.CUSTOM_DATA);
        if (data == null) return out;
        ListTag list = data.copyTag().getListOrEmpty(KEY);
        if (list.isEmpty()) return out;
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            String id = list.getStringOr(i, "");
            if (StratagemRegistry.get(id) != null && !ids.contains(id)) ids.add(id);
        }
        if (ids.isEmpty()) return out;
        // Fill any shortfall with defaults so the HUD always has 4 slots.
        for (String d : DEFAULT) {
            if (ids.size() >= SIZE) break;
            if (!ids.contains(d)) ids.add(d);
        }
        return ids.subList(0, Math.min(SIZE, ids.size()));
    }

    /** Write a validated loadout (1-4 known, distinct ids) onto the uplink stack. */
    public static void set(ItemStack uplink, List<String> ids) {
        if (uplink == null || uplink.isEmpty()) return;
        List<String> clean = new ArrayList<>();
        for (String id : ids) {
            if (StratagemRegistry.get(id) != null && !clean.contains(id)) clean.add(id);
            if (clean.size() >= SIZE) break;
        }
        if (clean.isEmpty()) return;
        CustomData data = uplink.get(DataComponents.CUSTOM_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTag();
        ListTag list = new ListTag();
        for (String id : clean) list.add(StringTag.valueOf(id));
        tag.put(KEY, list);
        uplink.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }
}
