package dev.chickenthedev.stratagems;

import dev.chickenthedev.stratagems.client.ClientStratagemInput;
import dev.chickenthedev.stratagems.client.DestroyerModel;
import dev.chickenthedev.stratagems.client.SentryModel;
import dev.chickenthedev.stratagems.client.SentryRenderer;
import dev.chickenthedev.stratagems.client.DestroyerRenderer;
import dev.chickenthedev.stratagems.client.NoOpRenderer;
import dev.chickenthedev.stratagems.client.StratagemHud;
import dev.chickenthedev.stratagems.network.ArmStratagemPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class StratagemsModFabricClient implements ClientModInitializer {

    private static KeyMapping uplinkKey;
    private static KeyMapping upKey;
    private static KeyMapping downKey;
    private static KeyMapping leftKey;
    private static KeyMapping rightKey;

    private static final Category CATEGORY =
            new Category(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "stratagems"));

    @Override
    public void onInitializeClient() {
        uplinkKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.stratagems.uplink", InputConstants.Type.KEYBOARD, GLFW.GLFW_KEY_G, CATEGORY));
        upKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.stratagems.up", InputConstants.Type.KEYBOARD, GLFW.GLFW_KEY_UP, CATEGORY));
        downKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.stratagems.down", InputConstants.Type.KEYBOARD, GLFW.GLFW_KEY_DOWN, CATEGORY));
        leftKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.stratagems.left", InputConstants.Type.KEYBOARD, GLFW.GLFW_KEY_LEFT, CATEGORY));
        rightKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.stratagems.right", InputConstants.Type.KEYBOARD, GLFW.GLFW_KEY_RIGHT, CATEGORY));

        ClientStratagemInput.init(id -> ClientPlayNetworking.send(new ArmStratagemPayload(id)));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (uplinkKey.consumeClick()) {
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

        HudElementRegistry.addLast(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "hud"), StratagemHud::render);

        EntityRendererRegistry.register(ModEntities.BEACON.get(), ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.SHIELD_DOME.get(), NoOpRenderer::new);

        ModelLayerRegistry.registerModelLayer(DestroyerModel.LAYER, DestroyerModel::createBodyLayer);
        ModelLayerRegistry.registerModelLayer(SentryModel.LAYER, SentryModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntities.DESTROYER.get(), DestroyerRenderer::new);
        EntityRendererRegistry.register(ModEntities.SENTRY.get(), SentryRenderer::new);
    }
}
