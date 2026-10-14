package dev.chickenthedev.stratagems;

import dev.chickenthedev.stratagems.client.ClientStratagemInput;
import dev.chickenthedev.stratagems.client.DestroyerModel;
import dev.chickenthedev.stratagems.client.SentryModel;
import dev.chickenthedev.stratagems.client.SentryRenderer;
import dev.chickenthedev.stratagems.client.DestroyerRenderer;
import dev.chickenthedev.stratagems.client.NoOpRenderer;
import dev.chickenthedev.stratagems.client.StratagemHud;
import dev.chickenthedev.stratagems.network.ArmStratagemPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.lwjgl.glfw.GLFW;

public class StratagemsModNeoForgeClient {

    private static KeyMapping uplinkKey;
    private static KeyMapping upKey;
    private static KeyMapping downKey;
    private static KeyMapping leftKey;
    private static KeyMapping rightKey;

    private static final Category CATEGORY =
            new Category(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "stratagems"));

    public static void init(IEventBus modBus) {
        ClientStratagemInput.init(id -> ClientPacketDistributor.sendToServer(new ArmStratagemPayload(id)));

        modBus.addListener((RegisterKeyMappingsEvent event) -> {
            uplinkKey = new KeyMapping("key.stratagems.uplink", InputConstants.Type.KEYBOARD, GLFW.GLFW_KEY_G, CATEGORY);
            upKey = new KeyMapping("key.stratagems.up", InputConstants.Type.KEYBOARD, GLFW.GLFW_KEY_UP, CATEGORY);
            downKey = new KeyMapping("key.stratagems.down", InputConstants.Type.KEYBOARD, GLFW.GLFW_KEY_DOWN, CATEGORY);
            leftKey = new KeyMapping("key.stratagems.left", InputConstants.Type.KEYBOARD, GLFW.GLFW_KEY_LEFT, CATEGORY);
            rightKey = new KeyMapping("key.stratagems.right", InputConstants.Type.KEYBOARD, GLFW.GLFW_KEY_RIGHT, CATEGORY);
            event.register(uplinkKey);
            event.register(upKey);
            event.register(downKey);
            event.register(leftKey);
            event.register(rightKey);
        });

        NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post event) -> {
            if (uplinkKey != null && uplinkKey.consumeClick()) {
                ClientStratagemInput.onUplinkKey();
            }
            if (ClientStratagemInput.isCodeMode()) {
                if (upKey.consumeClick()) ClientStratagemInput.onDirection(Direction.UP);
                if (downKey.consumeClick()) ClientStratagemInput.onDirection(Direction.DOWN);
                if (leftKey.consumeClick()) ClientStratagemInput.onDirection(Direction.LEFT);
                if (rightKey.consumeClick()) ClientStratagemInput.onDirection(Direction.RIGHT);
            }
            ClientStratagemInput.onTick();
        });

        NeoForge.EVENT_BUS.addListener((RenderGuiEvent.Pre event) ->
                StratagemHud.render(event.getGuiGraphics(), event.getPartialTick()));

        modBus.addListener((EntityRenderersEvent.RegisterRenderers event) -> {
            event.registerEntityRenderer(ModEntities.BEACON.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(ModEntities.SHIELD_DOME.get(), NoOpRenderer::new);
            event.registerEntityRenderer(ModEntities.DESTROYER.get(), DestroyerRenderer::new);
            event.registerEntityRenderer(ModEntities.SENTRY.get(), SentryRenderer::new);
        });

        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) ->
                event.registerLayerDefinition(DestroyerModel.LAYER, DestroyerModel::createBodyLayer));

        modBus.addListener((EntityRenderersEvent.RegisterLayerDefinitions event) ->
                event.registerLayerDefinition(SentryModel.LAYER, SentryModel::createBodyLayer));
    }
}
