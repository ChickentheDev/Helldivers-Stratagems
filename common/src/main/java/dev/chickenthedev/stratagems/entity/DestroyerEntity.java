package dev.chickenthedev.stratagems.entity;

import dev.chickenthedev.stratagems.Constants;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

// A player's Super Destroyer, holding station high in the sky. Follows its
// owner around the overworld so it's always visible when you look up.
public class DestroyerEntity extends Entity {

    public static final double ORBIT_HEIGHT = 215.0;

    private UUID ownerId;

    public DestroyerEntity(EntityType<DestroyerEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        noPhysics = true;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && ownerId != null && level() instanceof ServerLevel serverLevel) {
            ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(ownerId);
            if (player != null && player.level() == serverLevel && player.isAlive()) {
                // Gentle bob plus a slow drift toward a point above the owner.
                double targetY = ORBIT_HEIGHT + Math.sin(tickCount * 0.03) * 2.0;
                Vec3 pos = position();
                setPos(lerp(pos.x, player.getX(), 0.03),
                        lerp(pos.y, targetY, 0.05),
                        lerp(pos.z, player.getZ(), 0.03));
            }
        }
    }

    private static double lerp(double from, double to, double factor) {
        return from + (to - from) * factor;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        if (ownerId != null) {
            output.putString("Owner", ownerId.toString());
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        String raw = input.getStringOr("Owner", "");
        if (!raw.isEmpty()) {
            try {
                ownerId = UUID.fromString(raw);
            } catch (IllegalArgumentException e) {
                Constants.LOG.warn("Destroyer had a bad owner UUID, dropping it");
                ownerId = null;
            }
        }
    }
}
