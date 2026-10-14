package dev.chickenthedev.stratagems.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

// Shield Generator Relay: a stationary dome that grants heavy damage resistance
// to players inside it for a limited time.
public class ShieldDomeEntity extends Entity {

    private static final float RADIUS = 8.0F;
    private static final int DURATION_TICKS = 400; // 20 seconds

    private int life = DURATION_TICKS;

    public ShieldDomeEntity(EntityType<ShieldDomeEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        ServerLevel serverLevel = (ServerLevel) level();

        if (--life <= 0) {
            serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 1, getZ(), 40, RADIUS / 2, 1.5, RADIUS / 2, 0.05);
            discard();
            return;
        }

        Vec3 pos = position();
        AABB zone = new AABB(pos.x - RADIUS, pos.y - 2, pos.z - RADIUS, pos.x + RADIUS, pos.y + 6, pos.z + RADIUS);
        for (Player player : serverLevel.getEntitiesOfClass(Player.class, zone)) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 40, 3, false, false));
        }

        if (tickCount % 4 == 0) {
            for (int i = 0; i < 24; i++) {
                double angle = (Math.PI * 2 * i) / 24;
                double x = pos.x + Math.cos(angle) * RADIUS;
                double z = pos.z + Math.sin(angle) * RADIUS;
                serverLevel.sendParticles(ParticleTypes.END_ROD, x, pos.y + 0.2, z, 1, 0, 0.6, 0, 0.01);
            }
            serverLevel.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 4.5, pos.z, 3, RADIUS / 2, 0.2, RADIUS / 2, 0.01);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        life = input.getIntOr("Life", DURATION_TICKS);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putInt("Life", life);
    }
}
