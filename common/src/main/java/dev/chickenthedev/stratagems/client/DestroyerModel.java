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

import java.util.ArrayList;
import java.util.List;

// Client-only. Boxy Super Destroyer silhouette, built from cuboids.
// Model units are 1/16th of a block; the renderer scales it up.
public class DestroyerModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "super_destroyer"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Main hull
        root.addOrReplaceChild("hull", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-40.0F, -4.0F, -5.0F, 80.0F, 8.0F, 10.0F), PartPose.ZERO);
        // Tapered nose
        root.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 0)
                .addBox(40.0F, -3.0F, -3.5F, 20.0F, 6.0F, 7.0F), PartPose.ZERO);
        // Rear engine block
        root.addOrReplaceChild("engine_block", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-64.0F, -6.0F, -6.0F, 24.0F, 12.0F, 12.0F), PartPose.ZERO);
        // Bridge tower and top
        root.addOrReplaceChild("bridge", CubeListBuilder.create().texOffs(0, 0)
                .addBox(10.0F, 4.0F, -3.0F, 20.0F, 7.0F, 6.0F), PartPose.ZERO);
        root.addOrReplaceChild("bridge_top", CubeListBuilder.create().texOffs(0, 0)
                .addBox(14.0F, 11.0F, -2.0F, 12.0F, 4.0F, 4.0F), PartPose.ZERO);
        root.addOrReplaceChild("antenna", CubeListBuilder.create().texOffs(0, 0)
                .addBox(18.0F, 15.0F, -0.5F, 2.0F, 9.0F, 1.0F), PartPose.ZERO);
        // Side wings
        root.addOrReplaceChild("wing_left", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-20.0F, -2.0F, 5.0F, 40.0F, 3.0F, 22.0F), PartPose.ZERO);
        root.addOrReplaceChild("wing_right", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-20.0F, -2.0F, -27.0F, 40.0F, 3.0F, 22.0F), PartPose.ZERO);

        // Engine glows (bright cyan in the texture)
        for (int i = 0; i < 3; i++) {
            float z = -4.5F + i * 3.0F;
            root.addOrReplaceChild("engine_glow_" + i, CubeListBuilder.create().texOffs(96, 0)
                    .addBox(-66.0F, -3.5F, z, 2.0F, 7.0F, 3.0F), PartPose.ZERO);
        }
        // Wingtip nav lights (blink red/green)
        root.addOrReplaceChild("nav_light_left", CubeListBuilder.create().texOffs(96, 32)
                .addBox(-2.0F, -1.0F, 26.0F, 4.0F, 2.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("nav_light_right", CubeListBuilder.create().texOffs(112, 32)
                .addBox(-2.0F, -1.0F, -28.0F, 4.0F, 2.0F, 2.0F), PartPose.ZERO);
        // Radar bar on a pivot so it can spin
        root.addOrReplaceChild("radar", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-2.0F, 0.0F, -0.5F, 4.0F, 4.0F, 1.0F), PartPose.offset(20.0F, 15.0F, 0.0F));

        return LayerDefinition.create(mesh, 128, 128);
    }

    private final ModelPart root;
    private final ModelPart radar;
    private final ModelPart navLightLeft;
    private final ModelPart navLightRight;
    private final List<ModelPart> engineGlows = new ArrayList<>();

    public DestroyerModel(ModelPart root) {
        this.root = root;
        this.radar = root.getChild("radar");
        this.navLightLeft = root.getChild("nav_light_left");
        this.navLightRight = root.getChild("nav_light_right");
        for (int i = 0; i < 3; i++) {
            engineGlows.add(root.getChild("engine_glow_" + i));
        }
    }

    public ModelPart root() {
        return root;
    }

    public ModelPart radar() {
        return radar;
    }

    public ModelPart navLightLeft() {
        return navLightLeft;
    }

    public ModelPart navLightRight() {
        return navLightRight;
    }

    public List<ModelPart> engineGlows() {
        return engineGlows;
    }
}
