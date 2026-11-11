package dev.chickenthedev.stratagems.entity;

import dev.chickenthedev.stratagems.Constants;
import dev.chickenthedev.stratagems.ModEntities;
import dev.chickenthedev.stratagems.StratagemManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

// A munition fired from the owner's Super Destroyer down to the stratagem
// target. Pods (sentries, weapons, vehicles...) fall as supply pods; strikes
// fall as glowing shells. On arrival the stratagem's effect plays out.
public class StrikeShellEntity extends ThrowableItemProjectile {

    private String stratagemId = "";
    private boolean pod = false;
    private Vec3 target = Vec3.ZERO;
    private double dirX = 0;
    private double dirZ = 1;
    private UUID ownerId = null;
    private int flightTicks = 20;
    private int elapsed = 0;
    private boolean applied = false;

    public StrikeShellEntity(EntityType<StrikeShellEntity> type, Level level) {
        super(type, level);
    }

    /** Configure the shell before spawning. */
    public void launch(Vec3 from, Vec3 target, String stratagemId, boolean pod,
                       double dirX, double dirZ, UUID ownerId) {
        this.stratagemId = stratagemId;
        this.pod = pod;
        this.target = target;
        this.dirX = dirX;
        this.dirZ = dirZ;
        this.ownerId = ownerId;
        setPos(from.x, from.y - 3.0, from.z);
        double dist = from.distanceTo(target);
        this.flightTicks = (int) Math.max(8, Math.min(40, dist / 14.0));
        Vec3 vel = target.subtract(from).scale(1.0 / this.flightTicks);
        setDeltaMovement(vel);
        setNoGravity(true);
    }

    @Override
    protected Item getDefaultItem() {
        return pod ? Items.CHEST : Items.FIRE_CHARGE;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            // Tracer so you can watch the round come down from the ship.
            Vec3 p = position();
            level().addParticle(pod ? ParticleTypes.CLOUD : ParticleTypes.END_ROD,
                    p.x, p.y, p.z, 0, 0, 0);
            return;
        }
        elapsed++;
        if (!applied && (elapsed >= flightTicks || position().distanceTo(target) < 2.0)) {
            apply();
        }
        if (elapsed > flightTicks + 200) {
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level().isClientSide() && !applied) {
            apply();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level().isClientSide() && !applied) {
            apply();
        }
    }

    private void apply() {
        applied = true;
        if (level() instanceof ServerLevel serverLevel) {
            Entity owner = null;
            if (ownerId != null) {
                ServerPlayer sp = serverLevel.getServer().getPlayerList().getPlayer(ownerId);
                if (sp != null) owner = sp;
            }
            level().playSound(null, target.x, target.y, target.z, SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.BLOCKS, 0.6F, 1.2F);
            StratagemManager.applyStrike(serverLevel, target, stratagemId, dirX, dirZ, owner);
        }
        discard();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder); // defines DATA_ITEM_STACK; without this, Builder.build() throws IllegalStateException on the null slot
    }

    @Override
    public void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Stratagem", stratagemId);
        output.putBoolean("Pod", pod);
        output.putDouble("TargetX", target.x);
        output.putDouble("TargetY", target.y);
        output.putDouble("TargetZ", target.z);
        output.putDouble("DirX", dirX);
        output.putDouble("DirZ", dirZ);
        if (ownerId != null) output.putString("Owner", ownerId.toString());
        output.putInt("FlightTicks", flightTicks);
        output.putInt("Elapsed", elapsed);
    }

    @Override
    public void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        stratagemId = input.getStringOr("Stratagem", "");
        pod = input.getBooleanOr("Pod", false);
        target = new Vec3(input.getDoubleOr("TargetX", 0), input.getDoubleOr("TargetY", 0),
                input.getDoubleOr("TargetZ", 0));
        dirX = input.getDoubleOr("DirX", 0);
        dirZ = input.getDoubleOr("DirZ", 1);
        String raw = input.getStringOr("Owner", "");
        if (!raw.isEmpty()) {
            try {
                ownerId = UUID.fromString(raw);
            } catch (IllegalArgumentException e) {
                Constants.LOG.warn("Strike shell had a bad owner UUID, dropping it");
                ownerId = null;
            }
        }
        flightTicks = input.getIntOr("FlightTicks", 20);
        elapsed = input.getIntOr("Elapsed", 0);
    }

    public static EntityType<StrikeShellEntity> type() {
        return ModEntities.STRIKE_SHELL.get();
    }
}
