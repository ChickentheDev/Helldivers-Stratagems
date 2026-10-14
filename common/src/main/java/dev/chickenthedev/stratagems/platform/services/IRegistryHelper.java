package dev.chickenthedev.stratagems.platform.services;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.function.Function;
import java.util.function.Supplier;

// Platform abstraction for content registration: Fabric registers directly,
// NeoForge goes through DeferredRegister. Common code builds the content and
// the platform decides how it lands in the registries.
public interface IRegistryHelper {

    Supplier<Item> registerItem(String id, Function<Item.Properties, Item> factory);

    <T extends Entity> Supplier<EntityType<T>> registerEntityType(String id, Supplier<EntityType<T>> typeSupplier);
}
