package dev.chickenthedev.stratagems.client;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;

// Client-only. Renders nothing; used for entities that are purely logical
// server-side (their visuals are particles).
public class NoOpRenderer<T extends Entity> extends EntityRenderer<T, EntityRenderState> {

    public NoOpRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }
}
