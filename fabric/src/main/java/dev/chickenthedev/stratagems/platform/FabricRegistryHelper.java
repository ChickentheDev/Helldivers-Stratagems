package dev.chickenthedev.stratagems.platform;

import dev.chickenthedev.stratagems.Constants;
import dev.chickenthedev.stratagems.platform.services.IRegistryHelper;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.function.Function;
import java.util.function.Supplier;

public class FabricRegistryHelper implements IRegistryHelper {

    @Override
    public Supplier<Item> registerItem(String id, Function<Item.Properties, Item> factory) {
        Identifier rl = Identifier.fromNamespaceAndPath(Constants.MOD_ID, id);
        Item item = factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, rl)));
        Registry.register(BuiltInRegistries.ITEM, rl, item);
        return () -> item;
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntityType(String id, Supplier<EntityType<T>> typeSupplier) {
        EntityType<T> type = typeSupplier.get();
        Registry.register(BuiltInRegistries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, id), type);
        return () -> type;
    }
}
