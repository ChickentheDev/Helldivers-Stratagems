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

// Client-only. Super Destroyer silhouette matching the Helldivers 2 ship:
// massive tall rear engine block, long flat forward spine, vertical fins at
// the bow, bridge superstructure on top, yellow Super-Earth striping.
// Model units are 1/16th of a block; the renderer scales it up.
public class DestroyerModel {

    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            Identifier.fromNamespaceAndPath(Constants.MOD_ID, "super_destroyer"), "main");

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Massive rear engine block (taller than the hull)
        root.addOrReplaceChild("rear_block", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-70.0F, -10.0F, -9.0F, 26.0F, 22.0F, 18.0F), PartPose.ZERO);
        // Mid hull
        root.addOrReplaceChild("mid_hull", CubeListBuilder.create().texOffs(88, 0)
                .addBox(-44.0F, -6.0F, -7.0F, 52.0F, 12.0F, 14.0F), PartPose.ZERO);
        // Long flat forward spine
        root.addOrReplaceChild("spine", CubeListBuilder.create().texOffs(0, 40)
                .addBox(8.0F, -4.0F, -5.0F, 64.0F, 7.0F, 10.0F), PartPose.ZERO);
        // Raised top deck strip
        root.addOrReplaceChild("top_deck", CubeListBuilder.create().texOffs(148, 40)
                .addBox(-38.0F, 8.0F, -6.0F, 64.0F, 3.0F, 12.0F), PartPose.ZERO);
        // Bridge tower on the deck
        root.addOrReplaceChild("bridge", CubeListBuilder.create().texOffs(88, 26)
                .addBox(-34.0F, 11.0F, -4.0F, 14.0F, 8.0F, 8.0F), PartPose.ZERO);
        // Vertical bow fins (top and bottom)
        root.addOrReplaceChild("fin_top", CubeListBuilder.create().texOffs(132, 26)
                .addBox(60.0F, 3.0F, -1.0F, 12.0F, 10.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("fin_bottom", CubeListBuilder.create().texOffs(160, 26)
                .addBox(60.0F, -14.0F, -1.0F, 12.0F, 10.0F, 2.0F), PartPose.ZERO);
        // Keel rail under the spine
        root.addOrReplaceChild("keel", CubeListBuilder.create().texOffs(300, 40)
                .addBox(8.0F, -6.0F, -1.5F, 64.0F, 2.0F, 3.0F), PartPose.ZERO);
        // Ventral hangar pods
        root.addOrReplaceChild("pod_left", CubeListBuilder.create().texOffs(0, 57)
                .addBox(-20.0F, -11.0F, 8.0F, 12.0F, 5.0F, 4.0F), PartPose.ZERO);
        root.addOrReplaceChild("pod_right", CubeListBuilder.create().texOffs(32, 57)
                .addBox(-20.0F, -11.0F, -12.0F, 12.0F, 5.0F, 4.0F), PartPose.ZERO);

        // Engine glows (bright cyan in the texture) on the rear face
        for (int i = 0; i < 3; i++) {
            float z = -5.0F + i * 4.0F;
            root.addOrReplaceChild("engine_glow_" + i, CubeListBuilder.create().texOffs(320, 0)
                    .addBox(-72.0F, -6.0F, z, 2.0F, 8.0F, 4.0F), PartPose.ZERO);
        }
        // Bow nav lights on the fin tips (red left, green right)
        root.addOrReplaceChild("nav_light_left", CubeListBuilder.create().texOffs(332, 0)
                .addBox(66.0F, 11.0F, 0.0F, 4.0F, 2.0F, 2.0F), PartPose.ZERO);
        root.addOrReplaceChild("nav_light_right", CubeListBuilder.create().texOffs(344, 0)
                .addBox(66.0F, 11.0F, -2.0F, 4.0F, 2.0F, 2.0F), PartPose.ZERO);
        // Radar bar on a pivot so it can spin (on a small pylon above the deck)
        root.addOrReplaceChild("radar_pylon", CubeListBuilder.create().texOffs(366, 0)
                .addBox(19.5F, 11.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("radar", CubeListBuilder.create().texOffs(356, 0)
                .addBox(-2.0F, 0.0F, -0.5F, 4.0F, 3.0F, 1.0F), PartPose.offset(20.0F, 15.0F, 0.0F));
        // Antenna mast on the bridge
        root.addOrReplaceChild("antenna", CubeListBuilder.create().texOffs(370, 0)
                .addBox(-28.0F, 19.0F, -0.5F, 1.0F, 10.0F, 1.0F), PartPose.ZERO);

        return LayerDefinition.create(mesh, 512, 256);
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
