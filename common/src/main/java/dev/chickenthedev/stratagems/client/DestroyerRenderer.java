package dev.chickenthedev.stratagems.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.chickenthedev.stratagems.Constants;
import dev.chickenthedev.stratagems.entity.DestroyerEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.resources.Identifier;

// Client-only. Renders the Super Destroyer with its animations:
// slow drift sway, spinning radar, blinking wingtip lights, engine flicker.
public class DestroyerRenderer extends EntityRenderer<DestroyerEntity, DestroyerRenderer.DestroyerRenderState> {

    public static class DestroyerRenderState extends EntityRenderState {
        public int age;
    }

    private static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(
            Constants.MOD_ID, "textures/entity/super_destroyer.png");

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

    private final DestroyerModel model;

    public DestroyerRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new DestroyerModel(context.bakeLayer(DestroyerModel.LAYER));
    }

    @Override
    public DestroyerRenderState createRenderState() {
        return new DestroyerRenderState();
    }

    @Override
    public void extractRenderState(DestroyerEntity entity, DestroyerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        state.age = entity.tickCount;
    }

    @Override
    public void submit(DestroyerRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraState) {
        poseStack.pushPose();

        // Slow, stately drift: a ship holding station, not parked.
        float swayYaw = (float) Math.sin(state.age * 0.04) * 0.05F;
        float swayPitch = (float) Math.sin(state.age * 0.03 + 1.0F) * 0.02F;
        poseStack.rotate(Axis.YP, swayYaw);
        poseStack.rotate(Axis.XP, swayPitch);
        poseStack.scale(12.0F, 12.0F, 12.0F);

        // Spinning radar bar.
        model.radar().yRot = state.age * 0.1F;
        // Alternating wingtip strobes.
        boolean blink = (state.age % 40) < 20;
        model.navLightLeft().visible = blink;
        model.navLightRight().visible = !blink;
        // Engine shimmer.
        for (int i = 0; i < model.engineGlows().size(); i++) {
            model.engineGlows().get(i).visible = (state.age + i * 2) % 8 < 7;
        }

        collector.submitModelPart(model.root(), poseStack,
                RenderTypes.entityCutout(TEXTURE),
                state.lightCoords, OverlayTexture.NO_OVERLAY, IDENTITY_UV);

        poseStack.popPose();
    }
}
