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

// Geometry for the rising energy column and its expanding ground ring.
public class OrbitalImpactModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "orbital_impact"), "main");

    private final ModelPart root;
    private final ModelPart beam;
    private final ModelPart railBeam;
    private final ModelPart[] ring = new ModelPart[64];

    public OrbitalImpactModel(ModelPart root) {
        this.root = root;
        beam = root.getChild("beam");
        railBeam = root.getChild("rail_beam");
        for (int i = 0; i < ring.length; i++) ring[i] = root.getChild("ring_" + i);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("beam", CubeListBuilder.create().texOffs(22, 0)
                        .addBox(-10, 0, -10, 20, 768, 20), PartPose.ZERO);
        root.addOrReplaceChild("rail_beam", CubeListBuilder.create().texOffs(12, 0)
                        .addBox(-20, 0, -20, 40, 1536, 40), PartPose.ZERO);

        for (int i = 0; i < 16; i++) {
            root.addOrReplaceChild("ring_" + i, CubeListBuilder.create().texOffs(18, 1600)
                            .addBox(-14, -1, -1, 28, 2, 2), PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 64, 2048);
    }

    public void pose(float age, boolean railcannon) {
        beam.visible = !railcannon && age < 18;
        railBeam.visible = railcannon && age < 24;
        float radius = Math.min(age / (railcannon ? 22.0F : 16.0F), 1.0F)
                * (railcannon ? 28.0F : 14.0F) * 16.0F;
        float fade = age < 24 ? 1.0F : Math.max(0.0F, 1.0F - (age - 24) / 16.0F);
        for (int i = 0; i < ring.length; i++) {
            float angle = (float) (i * Math.PI * 2 / ring.length);
            ModelPart segment = ring[i];
            segment.setPos((float) Math.cos(angle) * radius, 0, (float) Math.sin(angle) * radius);
            segment.yRot = -angle;
            segment.xScale = fade;
            segment.zScale = fade;
        }
    }

    public ModelPart root() {
        return root;
    }
}
