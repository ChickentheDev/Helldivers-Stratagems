package dev.chickenthedev.stratagems.entity;

import dev.chickenthedev.stratagems.ModItems;
import dev.chickenthedev.stratagems.Stratagem;
import dev.chickenthedev.stratagems.StratagemManager;
import dev.chickenthedev.stratagems.StratagemRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

// Thrown stratagem beacon. Sticks where it lands, beeps with red smoke while
// the call-in counts down, then triggers its stratagem.
public class StratagemBeaconEntity extends ThrowableItemProjectile {

    private String stratagemId = "";
    private boolean landed = false;
    private int ticksUntilStrike = 60;
    private double dirX = 0;
    private double dirZ = 1;

    public StratagemBeaconEntity(EntityType<StratagemBeaconEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.BEACON.get();
    }

    public void setStratagem(String stratagemId, double dirX, double dirZ) {
        this.stratagemId = stratagemId;
        this.dirX = dirX;
        this.dirZ = dirZ;
        Stratagem s = StratagemRegistry.get(stratagemId);
        this.ticksUntilStrike = s != null ? s.callInTicks() : 60;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        stick();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        stick();
    }

    private void stick() {
        if (landed || level().isClientSide()) return;
        landed = true;
        setDeltaMovement(Vec3.ZERO);
        setNoGravity(true);
        level().playSound(null, blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 0.8F, 1.6F);
    }

    @Override
    public void tick() {
        if (landed) {
            if (!level().isClientSide()) {
                ServerLevel serverLevel = (ServerLevel) level();
                Vec3 pos = position();
                if (tickCount % 20 == 0) {
                    serverLevel.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y + 0.4, pos.z, 6, 0.1, 0.3, 0.1, 0.02);
                    serverLevel.sendParticles(ParticleTypes.FLAME, pos.x, pos.y + 0.2, pos.z, 2, 0.05, 0.1, 0.05, 0.0);
                    level().playSound(null, blockPosition(), SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.BLOCKS, 0.6F, 2.0F);
                }
                if (--ticksUntilStrike <= 0) {
                    StratagemManager.triggerStrike(serverLevel, pos, stratagemId, dirX, dirZ, getOwner());
                    discard();
                }
            }
            return;
        }
        super.tick();
        if (!level().isClientSide() && tickCount > 200) {
            stick();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Stratagem", stratagemId);
        output.putBoolean("Landed", landed);
        output.putInt("TicksUntilStrike", ticksUntilStrike);
        output.putDouble("DirX", dirX);
        output.putDouble("DirZ", dirZ);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        stratagemId = input.getStringOr("Stratagem", "");
        landed = input.getBooleanOr("Landed", false);
        ticksUntilStrike = input.getIntOr("TicksUntilStrike", 60);
        dirX = input.getDoubleOr("DirX", 0);
        dirZ = input.getDoubleOr("DirZ", 1);
    }
}
