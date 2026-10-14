package dev.chickenthedev.stratagems.entity;

import dev.chickenthedev.stratagems.StratagemManager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

// Automated turret deployed by sentry stratagems. Acquires the nearest hostile
// mob in range and fires on a per-kind interval. Discards after its ammo runs out.
public class SentryEntity extends Entity {

    private static final EntityDataAccessor<Float> AIM_YAW =
            SynchedEntityData.defineId(SentryEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AIM_PITCH =
            SynchedEntityData.defineId(SentryEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> KIND =
            SynchedEntityData.defineId(SentryEntity.class, EntityDataSerializers.STRING);

    private static final int MAX_AGE_TICKS = 20 * 240; // 4 minutes of ammo

    private int fireCooldown = 20;

    public SentryEntity(EntityType<SentryEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public void setKind(String kind) {
        entityData.set(KIND, kind);
    }

    public String getKind() {
        return entityData.get(KIND);
    }

    public float getAimYaw() {
        return entityData.get(AIM_YAW);
    }

    public float getAimPitch() {
        return entityData.get(AIM_PITCH);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(AIM_YAW, 0.0F);
        builder.define(AIM_PITCH, 0.0F);
        builder.define(KIND, "mg");
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        setKind(input.getStringOr("Kind", "mg"));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        output.putString("Kind", getKind());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false; // sentries are invulnerable; they expire on a timer
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) return;
        ServerLevel server = (ServerLevel) level();

        if (tickCount > MAX_AGE_TICKS) {
            server.sendParticles(ParticleTypes.SMOKE, getX(), getY() + 1, getZ(), 20, 0.5, 0.5, 0.5, 0.05);
            discard();
            return;
        }

        SentrySpec spec = SentrySpec.forKind(getKind());
        Monster target = acquireTarget(spec.range());
        if (target == null) {
            fireCooldown = Math.min(fireCooldown, 10);
            return;
        }

        // Aim the head at the target (synced to clients for the barrel animation).
        Vec3 from = position().add(0, 1.0, 0);
        Vec3 to = target.position().add(0, target.getBbHeight() * 0.6, 0);
        Vec3 delta = to.subtract(from);
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        entityData.set(AIM_YAW, (float) (Math.atan2(delta.x, delta.z)));
        entityData.set(AIM_PITCH, (float) (-Math.atan2(delta.y, horizontal)));

        if (--fireCooldown > 0) return;
        fireCooldown = spec.interval();
        fire(server, spec, target, from, delta.normalize());
    }

    private Monster acquireTarget(int range) {
        Vec3 p = position();
        AABB box = new AABB(p.x - range, p.y - 4, p.z - range, p.x + range, p.y + 12, p.z + range);
        List<Monster> monsters = level().getEntities(EntityTypeTest.forClass(Monster.class), box, Monster::isAlive);
        Monster best = null;
        double bestDist = Double.MAX_VALUE;
        for (Monster m : monsters) {
            double d = m.distanceToSqr(this);
            if (d < bestDist) {
                bestDist = d;
                best = m;
            }
        }
        return best;
    }

    private void fire(ServerLevel server, SentrySpec spec, Monster target, Vec3 from, Vec3 dir) {
        Vec3 targetPos = target.position();
        switch (spec.attack()) {
            case "arrow" -> {
                Arrow arrow = new Arrow(server, from.x, from.y, from.z,
                        new ItemStack(Items.ARROW), null);
                arrow.shoot(dir.x, dir.y, dir.z, 3.0F, 1.0F);
                arrow.setBaseDamage(spec.damage());
                server.addFreshEntity(arrow);
                muzzle(server, from, dir);
            }
            case "explode" -> StratagemManager.detonate(server,
                    new Vec3(targetPos.x, targetPos.y + 0.5, targetPos.z), (float) spec.damage());
            case "rockets" -> {
                for (int i = 0; i < 3; i++) {
                    Vec3 t = targetPos;
                    StratagemManager.schedule(server, i * 6, () -> StratagemManager.detonate(server,
                            new Vec3(t.x, t.y + 0.5, t.z), 2.5F));
                }
            }
            case "tesla" -> {
                @SuppressWarnings("unchecked")
                EntityType<LightningBolt> boltType =
                        StratagemManager.<LightningBolt>entityType("lightning_bolt");
                LightningBolt bolt = new LightningBolt(boltType, server);
                bolt.setPos(targetPos.x, targetPos.y, targetPos.z);
                bolt.setVisualOnly(true);
                server.addFreshEntity(bolt);
                damageNear(server, targetPos, 8, 10.0F, null);
            }
            case "ems" -> {
                damageNear(server, targetPos, 10, 2.0F,
                        new MobEffectInstance(MobEffects.SLOWNESS, 200, 2));
                server.sendParticles(ParticleTypes.ELECTRIC_SPARK,
                        targetPos.x, targetPos.y + 1, targetPos.z, 30, 3, 2, 3, 0.1);
            }
            case "laser" -> {
                target.hurtServer(server, target.damageSources().magic(), (float) spec.damage());
                server.sendParticles(ParticleTypes.END_ROD, from.x, from.y, from.z, 6, 0.1, 0.1, 0.1, 0.05);
                server.sendParticles(ParticleTypes.FIREWORK,
                        targetPos.x, targetPos.y + 1, targetPos.z, 8, 0.3, 0.5, 0.3, 0.1);
            }
            case "flame" -> {
                damageNear(server, targetPos, 8, (float) spec.damage(), null);
                for (Monster m : nearMonsters(server, targetPos, 8)) m.igniteForSeconds(5);
                server.sendParticles(ParticleTypes.FLAME,
                        targetPos.x, targetPos.y + 1, targetPos.z, 30, 2, 1, 2, 0.1);
            }
            case "gas" -> {
                damageNear(server, targetPos, 10, 2.0F,
                        new MobEffectInstance(MobEffects.POISON, 160, 1));
                server.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                        targetPos.x, targetPos.y + 1, targetPos.z, 30, 3, 2, 3, 0.05);
            }
            case "fireball" -> {
                SmallFireball fireball = new SmallFireball(server, from.x, from.y, from.z,
                        new Vec3(dir.x * 1.2, dir.y * 1.2 + 0.15, dir.z * 1.2));
                server.addFreshEntity(fireball);
                muzzle(server, from, dir);
            }
        }
    }

    private List<Monster> nearMonsters(ServerLevel server, Vec3 pos, double radius) {
        AABB box = new AABB(pos.x - radius, pos.y - 3, pos.z - radius,
                pos.x + radius, pos.y + 5, pos.z + radius);
        return server.getEntities(EntityTypeTest.forClass(Monster.class), box, Monster::isAlive);
    }

    private void damageNear(ServerLevel server, Vec3 pos, double radius, float damage, MobEffectInstance effect) {
        for (Monster m : nearMonsters(server, pos, radius)) {
            m.hurtServer(server, m.damageSources().magic(), damage);
            if (effect != null) m.addEffect(new MobEffectInstance(effect));
        }
    }

    private void muzzle(ServerLevel server, Vec3 from, Vec3 dir) {
        server.sendParticles(ParticleTypes.FLAME,
                from.x + dir.x, from.y + dir.y, from.z + dir.z, 4, 0.1, 0.1, 0.1, 0.02);
    }

    private record SentrySpec(int range, int interval, String attack, double damage) {
        static SentrySpec forKind(String kind) {
            return switch (kind) {
                case "gatling" -> new SentrySpec(24, 8, "arrow", 4);
                case "autocannon" -> new SentrySpec(32, 40, "explode", 2);
                case "mortar" -> new SentrySpec(48, 60, "explode", 3);
                case "rocket" -> new SentrySpec(40, 50, "rockets", 0);
                case "tesla" -> new SentrySpec(12, 50, "tesla", 0);
                case "ems" -> new SentrySpec(48, 60, "ems", 0);
                case "laser" -> new SentrySpec(32, 20, "laser", 8);
                case "flame" -> new SentrySpec(10, 25, "flame", 5);
                case "gas" -> new SentrySpec(48, 40, "gas", 0);
                case "hmg" -> new SentrySpec(32, 10, "arrow", 8);
                case "grenadier" -> new SentrySpec(36, 35, "fireball", 0);
                case "at" -> new SentrySpec(40, 70, "explode", 6);
                default -> new SentrySpec(24, 15, "arrow", 6); // mg
            };
        }
    }
}
