package dev.chickenthedev.stratagems;

import dev.chickenthedev.stratagems.network.ArmStratagemPayload;
import dev.chickenthedev.stratagems.platform.FabricRegistryHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class StratagemsModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Use Fabric to bootstrap the Common mod.
        StratagemsMod.init(new FabricRegistryHelper());

        // Dedicated "Helldivers Stratagems" creative tab.
        ResourceKey<CreativeModeTab> stratagemsTab = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, "main"));
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, stratagemsTab.identifier(),
                CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                        .icon(() -> new ItemStack(ModItems.UPLINK.get()))
                        .title(Component.translatable("itemGroup.stratagems"))
                        .build());
        CreativeModeTabEvents.modifyOutputEvent(stratagemsTab).register(output ->
                output.accept(ModItems.UPLINK.get()));

        PayloadTypeRegistry.serverboundPlay().register(ArmStratagemPayload.TYPE, ArmStratagemPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ArmStratagemPayload.TYPE,
                (payload, context) -> context.server().execute(() ->
                        StratagemManager.setArmed(context.player().getUUID(), payload.stratagemId())));

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            StratagemManager.serverTick(server);
            DestroyerManager.serverTick(server);
        });
    }
}
