package dev.chickenthedev.stratagems.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.chickenthedev.stratagems.Constants;
import dev.chickenthedev.stratagems.entity.OrbitalImpactEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.resources.Identifier;

// Renders the orbital beam and shock ring as full-bright translucent geometry.
public class OrbitalImpactRenderer extends EntityRenderer<OrbitalImpactEntity, OrbitalImpactRenderer.State> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Constants.MOD_ID, "textures/entity/orbital_impact.png");
    private static final UvMapping IDENTITY_UV = new UvMapping() {
        @Override public float getU(float u) { return u; }
        @Override public float getV(float v) { return v; }
    };

    private final OrbitalImpactModel model;

    public OrbitalImpactRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new OrbitalImpactModel(context.bakeLayer(OrbitalImpactModel.LAYER));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(OrbitalImpactEntity entity, State state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.age = entity.tickCount + partialTick;
        state.railcannon = entity.isRailcannon();
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector,
                       CameraRenderState cameraState) {
        model.pose(state.age, state.railcannon);
        poseStack.pushPose();
        collector.submitModelPart(model.root(), poseStack,
                RenderTypes.entityTranslucent(TEXTURE), 0xF000F0, OverlayTexture.NO_OVERLAY, IDENTITY_UV);
        poseStack.popPose();
    }

    @Override
    public boolean shouldRender(OrbitalImpactEntity entity, Frustum culler,
                                double camX, double camY, double camZ, float partialTicks) {
        // The rendered beam is much taller than the tiny logical anchor entity.
        return true;
    }

    public static class State extends EntityRenderState {
        public float age;
        public boolean railcannon;
    }
}
