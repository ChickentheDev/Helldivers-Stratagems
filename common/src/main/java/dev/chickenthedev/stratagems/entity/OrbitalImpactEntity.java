package dev.chickenthedev.stratagems.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// Short-lived render anchor for the client-side orbital beam and shock ring.
public class OrbitalImpactEntity extends Entity {

    private static final EntityDataAccessor<Boolean> RAILCANNON =
            SynchedEntityData.defineId(OrbitalImpactEntity.class, EntityDataSerializers.BOOLEAN);

    public OrbitalImpactEntity(EntityType<OrbitalImpactEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
    }

    public void setRailcannon(boolean railcannon) {
        entityData.set(RAILCANNON, railcannon);
    }

    public boolean isRailcannon() {
        return entityData.get(RAILCANNON);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && tickCount >= 40) discard();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(RAILCANNON, false);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }
}
