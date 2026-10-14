package dev.chickenthedev.stratagems;

import dev.chickenthedev.stratagems.network.ArmStratagemPayload;
import dev.chickenthedev.stratagems.platform.FabricRegistryHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public class StratagemsModFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        // Use Fabric to bootstrap the Common mod.
        StratagemsMod.init(new FabricRegistryHelper());

        // Put the uplink in the Tools & Utilities creative tab.
        ResourceKey<CreativeModeTab> toolsTab = ResourceKey.create(Registries.CREATIVE_MODE_TAB,
                Identifier.fromNamespaceAndPath("minecraft", "tools_and_utilities"));
        CreativeModeTabEvents.modifyOutputEvent(toolsTab).register((FabricCreativeModeTabOutput output) ->
                output.prepend(ModItems.UPLINK.get()));

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
