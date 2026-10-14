package dev.chickenthedev.stratagems.client;

import dev.chickenthedev.stratagems.Constants;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

// Compact automated turret: armored base, rotating head, and a barrel that
// pitches to track the sentry's current target. Parts are posed directly by
// the renderer (same pattern as the destroyer).
public class SentryModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "sentry"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart barrel;

    public SentryModel(ModelPart root) {
        this.root = root;
        this.head = root.getChild("head");
        this.barrel = head.getChild("barrel");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7, 0, -7, 14, 4, 14),
                PartPose.ZERO);
        root.addOrReplaceChild("column", CubeListBuilder.create()
                .texOffs(40, 18).addBox(-3, 4, -3, 6, 8, 6),
                PartPose.ZERO);

        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-5, -4, -5, 10, 8, 10),
                PartPose.offset(0, 14, 0));
        head.addOrReplaceChild("barrel", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-1.5F, -1.5F, 4, 3, 3, 14),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    public ModelPart root() {
        return root;
    }

    public ModelPart head() {
        return head;
    }

    public ModelPart barrel() {
        return barrel;
    }
}
