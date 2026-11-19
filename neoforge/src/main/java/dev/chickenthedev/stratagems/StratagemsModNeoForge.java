package dev.chickenthedev.stratagems;

import dev.chickenthedev.stratagems.client.ClientStratagemInput;
import dev.chickenthedev.stratagems.network.ArmStratagemPayload;
import dev.chickenthedev.stratagems.network.SetLoadoutPayload;
import dev.chickenthedev.stratagems.network.ThrowConfirmedPayload;
import dev.chickenthedev.stratagems.network.ThrowStratagemPayload;
import dev.chickenthedev.stratagems.platform.NeoForgeRegistryHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(Constants.MOD_ID)
public class StratagemsModNeoForge {

    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constants.MOD_ID);

    @SuppressWarnings("unused")
    private static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN_TAB = CREATIVE_TABS.register("main",
            () -> CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
                    .icon(() -> new ItemStack(ModItems.UPLINK.get()))
                    .title(Component.translatable("itemGroup.stratagems"))
                    .build());

    public StratagemsModNeoForge(IEventBus modBus) {
        // Use NeoForge to bootstrap the Common mod.
        StratagemsMod.init(new NeoForgeRegistryHelper(modBus));

        CREATIVE_TABS.register(modBus);

        // Fill the "Helldivers Stratagems" creative tab.
        modBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (MAIN_TAB.getKey().equals(event.getTabKey())) {
                event.accept(ModItems.UPLINK.get());
                event.accept(ModItems.LOADOUT.get());
            }
        });

        modBus.addListener((RegisterPayloadHandlersEvent event) -> {
            PayloadRegistrar registrar = event.registrar(Constants.MOD_ID);
            registrar.playToServer(ArmStratagemPayload.TYPE, ArmStratagemPayload.CODEC,
                    (payload, context) -> context.enqueueWork(() ->
                            StratagemManager.tryArm((ServerPlayer) context.player(), payload.stratagemId())));
            registrar.playToServer(SetLoadoutPayload.TYPE, SetLoadoutPayload.CODEC,
                    (payload, context) -> context.enqueueWork(() ->
                            StratagemManager.setLoadout((ServerPlayer) context.player(), payload.stratagemIds())));
            registrar.playToServer(ThrowStratagemPayload.TYPE, ThrowStratagemPayload.CODEC,
                    (payload, context) -> context.enqueueWork(() ->
                            StratagemManager.tryThrow((ServerPlayer) context.player())));
            // Runs only on the client (a dedicated server never receives its
            // own clientbound packets); the lambda body loads the client-only
            // input class solely when a confirmation actually arrives.
            registrar.playToClient(ThrowConfirmedPayload.TYPE, ThrowConfirmedPayload.CODEC,
                    (payload, context) -> context.enqueueWork(() ->
                            ClientStratagemInput.onThrowConfirmed(
                                    payload.stratagemId(), payload.cooldownTicks())));
        });

        // Deliver throw confirmations to the throwing player so the HUD can
        // show the per-stratagem cooldown that exactly mirrors the server.
        StratagemManager.throwNotifier =
                (player, payload) -> PacketDistributor.sendToPlayer(player, payload);

        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> {
            StratagemManager.serverTick(event.getServer());
            DestroyerManager.serverTick(event.getServer());
        });

        // Single-player shares one JVM across worlds: drop per-world static
        // state on server stop so it can't leak into the next world.
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent event) -> {
            StratagemManager.clearServerState();
            DestroyerManager.clearServerState();
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
