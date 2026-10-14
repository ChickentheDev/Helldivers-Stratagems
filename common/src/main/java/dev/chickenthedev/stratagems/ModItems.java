package dev.chickenthedev.stratagems;

import dev.chickenthedev.stratagems.item.StratagemUplinkItem;
import dev.chickenthedev.stratagems.platform.services.IRegistryHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.function.Supplier;

public class ModItems {

    public static Supplier<Item> UPLINK;
    public static Supplier<Item> BEACON;

    public static void register(IRegistryHelper registries) {
        UPLINK = registries.registerItem("stratagem_uplink",
                props -> new StratagemUplinkItem(props.stacksTo(1).rarity(Rarity.EPIC)));
        // Visual item for the thrown beacon projectile; not handed out directly.
        BEACON = registries.registerItem("stratagem_beacon",
                props -> new Item(props.stacksTo(16)));
    }
}
