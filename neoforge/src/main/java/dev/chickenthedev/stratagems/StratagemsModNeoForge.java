package dev.chickenthedev.stratagems;

import dev.chickenthedev.stratagems.network.ArmStratagemPayload;
import dev.chickenthedev.stratagems.platform.NeoForgeRegistryHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod(Constants.MOD_ID)
public class StratagemsModNeoForge {

    public StratagemsModNeoForge(IEventBus modBus) {
        // Use NeoForge to bootstrap the Common mod.
        StratagemsMod.init(new NeoForgeRegistryHelper(modBus));

        modBus.addListener((RegisterPayloadHandlersEvent event) -> {
            PayloadRegistrar registrar = event.registrar(Constants.MOD_ID);
            registrar.playToServer(ArmStratagemPayload.TYPE, ArmStratagemPayload.CODEC,
                    (payload, context) -> context.enqueueWork(() ->
                            StratagemManager.setArmed(context.player().getUUID(), payload.stratagemId())));
        });

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            StratagemManager.serverTick(event.getServer());
            DestroyerManager.serverTick(event.getServer());
        });

        // Put the uplink in the Tools & Utilities creative tab.
        Identifier toolsTabId = Identifier.fromNamespaceAndPath("minecraft", "tools_and_utilities");
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            ResourceKey<CreativeModeTab> tabKey = event.getTabKey();
            if (tabKey.isFor(Registries.CREATIVE_MODE_TAB) && tabKey.identifier().equals(toolsTabId)) {
                event.accept(ModItems.UPLINK.get());
            }
        });

        // Client classes are absent on a dedicated server; only init client code where they exist.
        try {
            Class.forName("net.minecraft.client.Minecraft");
            StratagemsModNeoForgeClient.init(modBus);
        } catch (ClassNotFoundException e) {
            Constants.LOG.debug("No client classes present, skipping client init");
        }
    }
}
