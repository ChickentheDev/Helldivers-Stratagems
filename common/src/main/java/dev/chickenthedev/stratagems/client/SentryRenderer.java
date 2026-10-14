package dev.chickenthedev.stratagems.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.chickenthedev.stratagems.Constants;
import dev.chickenthedev.stratagems.entity.SentryEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.resources.Identifier;

// Renders the sentry turret with its head yawed and barrel pitched toward
// whatever the sentry is currently engaging.
public class SentryRenderer extends EntityRenderer<SentryEntity, SentryRenderer.SentryRenderState> {

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Constants.MOD_ID, "textures/entity/sentry.png");

    private static final UvMapping IDENTITY_UV = new UvMapping() {
        @Override
        public float getU(float u) {
            return u;
        }

        @Override
        public float getV(float v) {
            return v;
        }
    };

    private final SentryModel model;

    public SentryRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SentryModel(context.bakeLayer(SentryModel.LAYER));
    }

    @Override
    public SentryRenderState createRenderState() {
        return new SentryRenderState();
    }

    @Override
    public void extractRenderState(SentryEntity entity, SentryRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.aimYaw = entity.getAimYaw();
        state.aimPitch = entity.getAimPitch();
    }

    @Override
    public void submit(SentryRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();
        // Face the model's +z barrel toward the target yaw.
        poseStack.rotate(Axis.YP, (float) Math.PI);
        model.head().yRot = state.aimYaw;
        model.barrel().xRot = state.aimPitch;
        collector.submitModelPart(model.root(), poseStack,
                RenderTypes.entityCutout(TEXTURE),
                state.lightCoords, OverlayTexture.NO_OVERLAY, IDENTITY_UV);
        poseStack.popPose();
    }

    public static class SentryRenderState extends EntityRenderState {
        public float aimYaw;
        public float aimPitch;
    }
}
