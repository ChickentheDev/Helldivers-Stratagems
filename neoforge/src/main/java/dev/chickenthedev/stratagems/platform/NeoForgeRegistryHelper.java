package dev.chickenthedev.stratagems.platform;

import dev.chickenthedev.stratagems.Constants;
import dev.chickenthedev.stratagems.platform.services.IRegistryHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.Supplier;

public class NeoForgeRegistryHelper implements IRegistryHelper {

    private final DeferredRegister.Items items;
    private final DeferredRegister<EntityType<?>> entityTypes;

    public NeoForgeRegistryHelper(IEventBus modBus) {
        
        this.items = DeferredRegister.createItems(Constants.MOD_ID);
        this.entityTypes = DeferredRegister.create(Registries.ENTITY_TYPE, Constants.MOD_ID);
        this.items.register(modBus);
        this.entityTypes.register(modBus);
    }

    @Override
    public Supplier<Item> registerItem(String id, Function<Item.Properties, Item> factory) {
        return items.register(id, (Identifier rl) ->
                factory.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, rl))));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Entity> Supplier<EntityType<T>> registerEntityType(String id, Supplier<EntityType<T>> typeSupplier) {
        Supplier<EntityType<?>> registered = entityTypes.register(id, () -> (EntityType<?>) typeSupplier.get());
        return () -> (EntityType<T>) registered.get();
    }
}
