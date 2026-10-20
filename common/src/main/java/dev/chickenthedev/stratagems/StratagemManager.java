package dev.chickenthedev.stratagems;

import dev.chickenthedev.stratagems.entity.SentryEntity;
import dev.chickenthedev.stratagems.entity.ShieldDomeEntity;
import dev.chickenthedev.stratagems.entity.StratagemBeaconEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.Mule;
import net.minecraft.world.entity.animal.golem.IronGolem;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.monster.Ravager;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

// Server-side state: which stratagem each player has armed, delayed strike
// scheduling, and the actual stratagem effects.
public class StratagemManager {

    private static final Map<UUID, String> ARMED = new HashMap<>();
    private static final List<DelayedAction> QUEUE = new ArrayList<>();

    private static class DelayedAction {
        final ServerLevel level;
        int ticksLeft;
        final Runnable action;

        DelayedAction(ServerLevel level, int ticksLeft, Runnable action) {
            this.level = level;
            this.ticksLeft = ticksLeft;
            this.action = action;
        }
    }

    public static void setArmed(UUID playerId, String stratagemId) {
        if (StratagemRegistry.get(stratagemId) != null) {
            ARMED.put(playerId, stratagemId);
        }
    }

    public static void clearArmed(UUID playerId) {
        ARMED.remove(playerId);
    }

    // Called from the uplink item's server-side use(). Spawns the beacon for the
    // armed stratagem and starts its cooldown.
    public static boolean tryThrow(ServerPlayer player) {
        UUID id = player.getUUID();
        // Read, don't remove: a failed throw (cooldown, bad id) must not eat the armed state.
        String stratagemId = ARMED.get(id);
        if (stratagemId == null) { return false; }
        Stratagem stratagem = StratagemRegistry.get(stratagemId);
        if (stratagem == null) { return false; }
        ItemStack uplinkStack = new ItemStack(ModItems.UPLINK.get());
        if (player.getCooldowns().isOnCooldown(uplinkStack)) { return false; }
        // Only consume the armed stratagem on the success path.
        ARMED.remove(id);

        ServerLevel level = player.level();
        StratagemBeaconEntity beacon = new StratagemBeaconEntity(ModEntities.BEACON.get(), level);
        beacon.setOwner(player);
        beacon.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());
        Vec3 look = player.getLookAngle();
        beacon.setStratagem(stratagemId, look.x, look.z);
        beacon.shoot(look.x, look.y, look.z, 1.6F, 1.0F);
        level.addFreshEntity(beacon);

        player.getCooldowns().addCooldown(uplinkStack, stratagem.cooldownTicks());
        level.playSound(null, player.blockPosition(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.8F, 1.2F);
        return true;
    }

    public static void schedule(ServerLevel level, int delayTicks, Runnable action) {
        QUEUE.add(new DelayedAction(level, delayTicks, action));
    }

    public static void serverTick(MinecraftServer server) {
        if (QUEUE.isEmpty()) return;
        Iterator<DelayedAction> it = QUEUE.iterator();
        while (it.hasNext()) {
            DelayedAction a = it.next();
            if (--a.ticksLeft <= 0) {
                it.remove();
                try {
                    a.action.run();
                } catch (Exception e) {
                    Constants.LOG.warn("Stratagem delayed action failed", e);
                }
            }
        }
    }

    // ---- Shared helpers (also used by SentryEntity) ----

    @SuppressWarnings("unchecked")
    public static <T extends Entity> EntityType<T> entityType(String id) {
        return (EntityType<T>) BuiltInRegistries.ENTITY_TYPE.getValue(
                Identifier.fromNamespaceAndPath("minecraft", id));
    }

    public static void detonate(ServerLevel level, Vec3 pos, float power) {
        Level.ExplosionInteraction interaction = StratagemConfig.INSTANCE.blockDamage
                ? Level.ExplosionInteraction.TNT
                : Level.ExplosionInteraction.NONE;
        level.explode(null, pos.x, pos.y + 0.5, pos.z, power, interaction);
    }

    // Fired when a beacon's call-in countdown finishes.
    public static void triggerStrike(ServerLevel level, Vec3 pos, String stratagemId,
                                     double dirX, double dirZ, Entity owner) {
        Stratagem stratagem = StratagemRegistry.get(stratagemId);
        if (stratagem == null) return;
        Constants.LOG.info("Stratagem inbound: {} at {}", stratagem.name, BlockPos.containing(pos));
        level.playSound(null, BlockPos.containing(pos), SoundEvents.LIGHTNING_BOLT_THUNDER,
                SoundSource.WEATHER, 1.0F, 0.7F);

        switch (stratagemId) {
            // ---- Orbital Strikes ----
            case "orbital_precision_strike" -> {
                detonate(level, pos, 10.0F);
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y + 1, pos.z, 3, 1, 1, 1, 0);
            }
            case "orbital_gatling_barrage" -> barrage(level, pos, 14, 2.5F, 10, 10, 6);
            case "orbital_gas_strike" -> lingeringCloud(level, pos, ParticleTypes.HAPPY_VILLAGER,
                    20, 5, 2.0F, new MobEffectInstance(MobEffects.POISON, 120, 1));
            case "orbital_120mm" -> barrage(level, pos, 24, 4.0F, 14, 8, 6);
            case "orbital_airburst" -> airburst(level, pos);
            case "orbital_smoke" -> lingeringCloud(level, pos, ParticleTypes.LARGE_SMOKE,
                    25, 7, 0, null);
            case "orbital_ems" -> lingeringCloud(level, pos, ParticleTypes.ELECTRIC_SPARK,
                    20, 6, 0, new MobEffectInstance(MobEffects.SLOWNESS, 200, 2));
            case "orbital_380mm" -> barrage(level, pos, 30, 6.0F, 18, 10, 8);
            case "orbital_walking" -> walkingBarrage(level, pos, dirX, dirZ);
            case "orbital_laser" -> orbitalLaser(level, pos);
            case "orbital_napalm" -> napalmBarrage(level, pos, 20, 14);
            case "orbital_railcannon" -> {
                detonate(level, pos, 16.0F);
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y + 1, pos.z, 6, 2, 2, 2, 0);
                level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 10, pos.z, 40, 1, 10, 1, 0.1);
            }

            // ---- Eagle Strikes ----
            case "eagle_strafing" -> strikeLine(level, pos, dirX, dirZ, true, 8, 2.5F, 6, 6, false);
            case "eagle_airstrike" -> strikeLine(level, pos, dirX, dirZ, false, 6, 5.0F, 7, 10, false);
            case "eagle_cluster" -> barrage(level, pos, 30, 2.0F, 16, 4, 4);
            case "eagle_smoke" -> lingeringCloud(level, pos, ParticleTypes.LARGE_SMOKE, 20, 6, 0, null);
            case "eagle_napalm" -> strikeLine(level, pos, dirX, dirZ, false, 6, 3.5F, 7, 10, true);
            case "eagle_rockets" -> strikeLine(level, pos, dirX, dirZ, true, 6, 4.0F, 5, 8, false);
            case "eagle_500kg" -> {
                detonate(level, pos, 18.0F);
                level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y + 1, pos.z, 8, 2, 2, 2, 0);
            }
            case "eagle_gas" -> lingeringCloud(level, pos, ParticleTypes.HAPPY_VILLAGER,
                    18, 5, 2.0F, new MobEffectInstance(MobEffects.POISON, 120, 1));

            // ---- Support Weapons (hellpod with the weapon) ----
            case "w_mg43" -> weaponPod(level, pos,
                    new ItemStack(Items.CROSSBOW), new ItemStack(Items.ARROW, 64));
            case "w_eat17" -> weaponPod(level, pos,
                    new ItemStack(Items.TNT, 6), new ItemStack(Items.FLINT_AND_STEEL));
            case "w_stalwart" -> weaponPod(level, pos,
                    new ItemStack(Items.BOW), new ItemStack(Items.ARROW, 96));
            case "w_las98" -> weaponPod(level, pos,
                    new ItemStack(Items.BLAZE_ROD, 4), new ItemStack(Items.GLOWSTONE_DUST, 16));
            case "w_apw1" -> weaponPod(level, pos,
                    new ItemStack(Items.CROSSBOW), new ItemStack(Items.SPECTRAL_ARROW, 32));
            case "w_gr8" -> weaponPod(level, pos,
                    new ItemStack(Items.TNT, 8), new ItemStack(Items.FIRE_CHARGE, 32));
            case "w_gl21" -> weaponPod(level, pos,
                    new ItemStack(Items.FIRE_CHARGE, 12), new ItemStack(Items.TNT, 4));
            case "w_flam40" -> weaponPod(level, pos,
                    new ItemStack(Items.FLINT_AND_STEEL), new ItemStack(Items.BLAZE_POWDER, 16),
                    new ItemStack(Items.LAVA_BUCKET));
            case "w_mg206" -> weaponPod(level, pos,
                    new ItemStack(Items.CROSSBOW), new ItemStack(Items.ARROW, 96),
                    new ItemStack(Items.SPECTRAL_ARROW, 16));
            case "w_ac8" -> weaponPod(level, pos,
                    new ItemStack(Items.DISPENSER), new ItemStack(Items.ARROW, 64));
            case "w_arc3" -> weaponPod(level, pos,
                    new ItemStack(Items.COPPER_INGOT, 4), new ItemStack(Items.AMETHYST_SHARD, 16));
            case "w_las99" -> weaponPod(level, pos,
                    new ItemStack(Items.ENDER_EYE, 16));
            case "w_rl77" -> weaponPod(level, pos,
                    new ItemStack(Items.CROSSBOW), new ItemStack(Items.FIREWORK_ROCKET, 16));
            case "w_mls4x" -> weaponPod(level, pos,
                    new ItemStack(Items.FIREWORK_ROCKET, 24));
            case "w_faf14" -> weaponPod(level, pos,
                    new ItemStack(Items.TRIDENT));
            case "w_rs422" -> weaponPod(level, pos,
                    new ItemStack(Items.CROSSBOW), new ItemStack(Items.SPYGLASS),
                    new ItemStack(Items.ARROW, 32));
            case "w_wasp" -> weaponPod(level, pos,
                    new ItemStack(Items.FIREWORK_ROCKET, 32), new ItemStack(Items.TNT, 8));
            case "w_cqc20" -> weaponPod(level, pos,
                    new ItemStack(Items.MACE));
            case "w_plas45" -> weaponPod(level, pos,
                    new ItemStack(Items.AMETHYST_SHARD, 16), new ItemStack(Items.ECHO_SHARD, 8));
            case "w_mgx42" -> weaponPod(level, pos,
                    new ItemStack(Items.CROSSBOW, 2), new ItemStack(Items.ARROW, 128));
            case "w_s11" -> weaponPod(level, pos,
                    new ItemStack(Items.TRIDENT), new ItemStack(Items.PRISMARINE_SHARD, 16));
            case "w_cqc9" -> weaponPod(level, pos,
                    new ItemStack(Items.SHEARS), new ItemStack(Items.BONE_MEAL, 32));
            case "w_gl52" -> weaponPod(level, pos,
                    new ItemStack(Items.SNOWBALL, 16), new ItemStack(Items.SLIME_BALL, 16));
            case "w_eat700" -> weaponPod(level, pos,
                    new ItemStack(Items.TNT, 8), new ItemStack(Items.LAVA_BUCKET, 2),
                    new ItemStack(Items.FLINT_AND_STEEL));
            case "w_tx41" -> weaponPod(level, pos,
                    new ItemStack(Items.FERMENTED_SPIDER_EYE, 16), new ItemStack(Items.GUNPOWDER, 8));
            case "w_eat411" -> weaponPod(level, pos,
                    new ItemStack(Items.TNT, 10));
            case "w_gl28" -> weaponPod(level, pos,
                    new ItemStack(Items.FIRE_CHARGE, 24), new ItemStack(Items.TNT, 6));
            case "w_c4" -> weaponPod(level, pos,
                    new ItemStack(Items.TNT, 16), new ItemStack(Items.REDSTONE_BLOCK, 4));
            case "w_ms11" -> weaponPod(level, pos,
                    new ItemStack(Items.FIREWORK_ROCKET, 32), new ItemStack(Items.TNT, 16));
            case "w_cremator" -> weaponPod(level, pos,
                    new ItemStack(Items.FLINT_AND_STEEL, 2), new ItemStack(Items.BLAZE_POWDER, 32));
            case "w_m1000" -> weaponPod(level, pos,
                    new ItemStack(Items.CROSSBOW), new ItemStack(Items.ARROW, 128),
                    new ItemStack(Items.SPECTRAL_ARROW, 32));
            case "w_flag" -> weaponPod(level, pos,
                    new ItemStack(Items.BANNER.pick(DyeColor.WHITE)));
            case "w_meltagun" -> weaponPod(level, pos,
                    new ItemStack(Items.LAVA_BUCKET, 2), new ItemStack(Items.MAGMA_BLOCK, 16));

            // ---- Backpacks ----
            case "p_b1" -> weaponPod(level, pos,
                    new ItemStack(Items.ARROW, 64), new ItemStack(Items.COOKED_BEEF, 16),
                    new ItemStack(Items.GOLDEN_APPLE, 4));
            case "p_lift850" -> weaponPod(level, pos,
                    new ItemStack(Items.WIND_CHARGE, 16));
            case "p_sh20" -> weaponPod(level, pos,
                    new ItemStack(Items.SHIELD));
            case "p_ar23", "p_las5", "p_flam75", "p_k9", "p_tx13" -> guardDog(level, pos, owner);
            case "p_sh32" -> shieldDome(level, pos);
            case "p_sh51" -> playerBuff(level, owner,
                    new MobEffectInstance(MobEffects.RESISTANCE, 2400, 1));
            case "p_b100" -> {
                level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y + 1, pos.z, 40, 1, 1, 1, 0.1);
                level.playSound(null, BlockPos.containing(pos), SoundEvents.TNT_PRIMED,
                        SoundSource.BLOCKS, 1.0F, 0.7F);
                schedule(level, 100, () -> {
                    detonate(level, pos, 20.0F);
                    level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y + 1, pos.z, 10, 3, 3, 3, 0);
                });
            }
            case "p_lift860" -> weaponPod(level, pos,
                    new ItemStack(Items.PHANTOM_MEMBRANE, 8), new ItemStack(Items.WIND_CHARGE, 8));
            case "p_lift182" -> weaponPod(level, pos,
                    new ItemStack(Items.ENDER_PEARL, 16));

            // ---- Vehicles ----
            case "v_exo45", "v_exo49", "v_exo51", "v_exo55" -> spawnVehicle(level, pos, "exo", owner);
            case "v_m102" -> spawnVehicle(level, pos, "frv", owner);
            case "v_m103" -> spawnVehicle(level, pos, "frv_supply", owner);
            case "v_m104" -> spawnVehicle(level, pos, "frv_fire", owner);
            case "v_td110", "v_td220" -> spawnVehicle(level, pos, "td", owner);

            // ---- Sentries ----
            case "s_mg" -> deploySentry(level, pos, "mg");
            case "s_gatling" -> deploySentry(level, pos, "gatling");
            case "s_autocannon" -> deploySentry(level, pos, "autocannon");
            case "s_mortar" -> deploySentry(level, pos, "mortar");
            case "s_rocket" -> deploySentry(level, pos, "rocket");
            case "s_tesla" -> deploySentry(level, pos, "tesla");
            case "s_ems" -> deploySentry(level, pos, "ems");
            case "s_laser" -> deploySentry(level, pos, "laser");
            case "s_flame" -> deploySentry(level, pos, "flame");
            case "s_gas" -> deploySentry(level, pos, "gas");

            // ---- Emplacements ----
            case "e_mines_ap" -> minefield(level, pos, 10, false, false);
            case "e_mines_fire" -> minefield(level, pos, 8, true, false);
            case "e_mines_at" -> minefield(level, pos, 6, false, false);
            case "e_shield" -> shieldDome(level, pos);
            case "e_hmg" -> deploySentry(level, pos, "hmg");
            case "e_grenadier" -> deploySentry(level, pos, "grenadier");
            case "e_mines_gas" -> minefield(level, pos, 8, false, true);
            case "e_at" -> deploySentry(level, pos, "at");

            // ---- Mission ----
            case "m_reinforce" -> reinforce(level, owner);
            case "m_resupply" -> dropSupplies(level, pos);
            case "m_sos" -> sosBeacon(level, pos);
            case "m_hellbomb" -> {
                level.sendParticles(ParticleTypes.SMOKE, pos.x, pos.y + 1, pos.z, 60, 2, 2, 2, 0.1);
                level.playSound(null, BlockPos.containing(pos), SoundEvents.TNT_PRIMED,
                        SoundSource.BLOCKS, 1.0F, 0.5F);
                schedule(level, 40, () -> {
                    detonate(level, pos, 30.0F);
                    level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y + 1, pos.z, 16, 4, 4, 4, 0);
                });
                schedule(level, 55, () -> detonate(level,
                        new Vec3(pos.x + 6, pos.y, pos.z - 4), 12.0F));
                schedule(level, 70, () -> detonate(level,
                        new Vec3(pos.x - 5, pos.y, pos.z + 6), 12.0F));
            }
            case "m_rearm" -> {
                if (owner instanceof ServerPlayer sp) {
                    sp.getCooldowns().addCooldown(new ItemStack(ModItems.UPLINK.get()), 1);
                    level.playSound(null, sp.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP,
                            SoundSource.PLAYERS, 1.0F, 1.4F);
                }
            }
            case "m_seaf" -> barrage(level, pos, 16, 5.0F, 14, 12, 8);
            case "m_flag" -> superEarthFlag(level, pos, owner);

            default -> Constants.LOG.warn("Unknown stratagem id: {}", stratagemId);
        }
    }
    // ---- Effect archetypes ----

    private static void barrage(ServerLevel level, Vec3 pos, int count, float power,
                                double radius, int tickStep, int jitter) {
        for (int i = 0; i < count; i++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2;
            double dist = 2 + level.getRandom().nextDouble() * radius;
            Vec3 target = new Vec3(pos.x + Math.cos(angle) * dist, pos.y, pos.z + Math.sin(angle) * dist);
            int delay = i * tickStep + level.getRandom().nextInt(jitter + 1);
            schedule(level, delay, () -> detonate(level, target, power));
        }
    }

    private static void strikeLine(ServerLevel level, Vec3 pos, double dirX, double dirZ,
                                   boolean alongThrow, int count, float power, double spacing,
                                   int tickStep, boolean incendiary) {
        double len = Math.sqrt(dirX * dirX + dirZ * dirZ);
        double tx = len > 0.01 ? dirX / len : 0;
        double tz = len > 0.01 ? dirZ / len : 1;
        double lx = alongThrow ? tx : -tz;
        double lz = alongThrow ? tz : tx;
        for (int i = 0; i < count; i++) {
            double offset = (i - (count - 1) / 2.0) * spacing;
            Vec3 target = new Vec3(pos.x + lx * offset, pos.y, pos.z + lz * offset);
            schedule(level, i * tickStep, () -> {
                level.playSound(null, BlockPos.containing(target), SoundEvents.GENERIC_EXPLODE.value(),
                        SoundSource.BLOCKS, 1.0F, 0.9F);
                detonate(level, target, power);
                if (incendiary) igniteNear(level, target, 4, 5);
            });
        }
    }

    private static void walkingBarrage(ServerLevel level, Vec3 pos, double dirX, double dirZ) {
        double len = Math.sqrt(dirX * dirX + dirZ * dirZ);
        double dx = len > 0.01 ? dirX / len : 0;
        double dz = len > 0.01 ? dirZ / len : 1;
        for (int i = 0; i < 8; i++) {
            double dist = (i - 3.5) * 9.0;
            Vec3 target = new Vec3(pos.x + dx * dist, pos.y, pos.z + dz * dist);
            schedule(level, i * 25, () -> {
                level.playSound(null, BlockPos.containing(target), SoundEvents.GENERIC_EXPLODE.value(),
                        SoundSource.BLOCKS, 1.2F, 0.7F);
                detonate(level, target, 6.0F);
            });
        }
    }

    private static void orbitalLaser(ServerLevel level, Vec3 pos) {
        for (int t = 0; t < 400; t += 10) {
            double angle = t * 0.08;
            Vec3 target = new Vec3(pos.x + Math.cos(angle) * 2.5, pos.y, pos.z + Math.sin(angle) * 2.5);
            int tick = t;
            schedule(level, t, () -> {
                AABB box = new AABB(target.x - 3, target.y - 2, target.z - 3,
                        target.x + 3, target.y + 40, target.z + 3);
                for (LivingEntity e : level.getEntities(EntityTypeTest.forClass(LivingEntity.class),
                        box, LivingEntity::isAlive)) {
                    e.hurtServer(level, e.damageSources().magic(), 10.0F);
                }
                level.sendParticles(ParticleTypes.END_ROD, target.x, target.y + 10, target.z,
                        30, 0.5, 10, 0.5, 0.05);
                level.sendParticles(ParticleTypes.FIREWORK, target.x, target.y + 1, target.z,
                        10, 1, 1, 1, 0.1);
                if (tick % 40 == 0) {
                    level.playSound(null, BlockPos.containing(target), SoundEvents.BEACON_AMBIENT,
                            SoundSource.WEATHER, 1.0F, 0.6F);
                }
            });
        }
    }

    private static void lingeringCloud(ServerLevel level, Vec3 pos, ParticleOptions particle,
                                       int seconds, double radius, float damage, MobEffectInstance effect) {
        for (int t = 0; t < seconds * 20; t += 20) {
            schedule(level, t, () -> {
                level.sendParticles(particle, pos.x, pos.y + 1, pos.z,
                        50, radius / 2, 1.5, radius / 2, 0.05);
                AABB box = new AABB(pos.x - radius, pos.y - 1, pos.z - radius,
                        pos.x + radius, pos.y + 4, pos.z + radius);
                for (LivingEntity e : level.getEntities(EntityTypeTest.forClass(LivingEntity.class),
                        box, LivingEntity::isAlive)) {
                    if (damage > 0) e.hurtServer(level, e.damageSources().magic(), damage);
                    if (effect != null) e.addEffect(new MobEffectInstance(effect));
                }
            });
        }
    }

    private static void napalmBarrage(ServerLevel level, Vec3 pos, int count, double radius) {
        for (int i = 0; i < count; i++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2;
            double dist = 2 + level.getRandom().nextDouble() * radius;
            Vec3 target = new Vec3(pos.x + Math.cos(angle) * dist, pos.y, pos.z + Math.sin(angle) * dist);
            int delay = i * 12 + level.getRandom().nextInt(8);
            schedule(level, delay, () -> {
                detonate(level, target, 3.5F);
                igniteNear(level, target, 4, 6);
                level.sendParticles(ParticleTypes.FLAME, target.x, target.y + 1, target.z,
                        25, 2, 1, 2, 0.1);
            });
        }
    }

    private static void airburst(ServerLevel level, Vec3 pos) {
        for (int i = 0; i < 12; i++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2;
            double dist = level.getRandom().nextDouble() * 12;
            Vec3 target = new Vec3(pos.x + Math.cos(angle) * dist, pos.y + 5 + level.getRandom().nextDouble() * 3,
                    pos.z + Math.sin(angle) * dist);
            schedule(level, i * 8, () -> {
                detonate(level, target, 3.0F);
                level.sendParticles(ParticleTypes.FIREWORK, target.x, target.y, target.z,
                        12, 1, 1, 1, 0.15);
            });
        }
    }

    private static void igniteNear(ServerLevel level, Vec3 pos, double radius, int seconds) {
        AABB box = new AABB(pos.x - radius, pos.y - 2, pos.z - radius,
                pos.x + radius, pos.y + 3, pos.z + radius);
        for (LivingEntity e : level.getEntities(EntityTypeTest.forClass(LivingEntity.class),
                box, LivingEntity::isAlive)) {
            e.igniteForSeconds(seconds);
        }
    }

    private static void deploySentry(ServerLevel level, Vec3 pos, String kind) {
        SentryEntity sentry = new SentryEntity(ModEntities.SENTRY.get(), level);
        sentry.setKind(kind);
        sentry.setPos(pos.x, pos.y + 0.5, pos.z);
        level.addFreshEntity(sentry);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.ANVIL_LAND,
                SoundSource.BLOCKS, 0.8F, 1.4F);
        level.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y + 1, pos.z, 20, 0.8, 0.8, 0.8, 0.05);
    }

    private static void minefield(ServerLevel level, Vec3 pos, int count, boolean incendiary, boolean gas) {
        for (int i = 0; i < count; i++) {
            double angle = level.getRandom().nextDouble() * Math.PI * 2;
            double dist = 2 + level.getRandom().nextDouble() * 10;
            BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING,
                    BlockPos.containing(pos.x + Math.cos(angle) * dist, pos.y, pos.z + Math.sin(angle) * dist));
            BlockPos tntPos = ground.above();
            if (!level.getBlockState(tntPos).isAir()) continue;
            level.setBlock(tntPos, Blocks.TNT.defaultBlockState(), 3);
            level.setBlock(tntPos.above(), Blocks.STONE_PRESSURE_PLATE.defaultBlockState(), 3);
            if (incendiary) {
                for (int f = 0; f < 3; f++) {
                    BlockPos fire = tntPos.offset(level.getRandom().nextInt(3) - 1, 1,
                            level.getRandom().nextInt(3) - 1);
                    if (level.getBlockState(fire).isAir()) {
                        level.setBlock(fire, Blocks.FIRE.defaultBlockState(), 3);
                    }
                }
            }
        }
        if (gas) {
            lingeringCloud(level, pos, ParticleTypes.HAPPY_VILLAGER, 15, 8, 1.0F,
                    new MobEffectInstance(MobEffects.POISON, 100, 1));
        }
        level.playSound(null, BlockPos.containing(pos), SoundEvents.ANVIL_LAND,
                SoundSource.BLOCKS, 0.6F, 1.6F);
    }

    private static void weaponPod(ServerLevel level, Vec3 pos, ItemStack... items) {
        placeChestWith(level, pos, List.of(items));
    }

    private static void placeChestWith(ServerLevel level, Vec3 pos, List<ItemStack> items) {
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(pos));
        BlockPos chestPos = ground.above();
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), 3);
        level.sendParticles(ParticleTypes.CLOUD, chestPos.getX() + 0.5, chestPos.getY() + 0.5,
                chestPos.getZ() + 0.5, 25, 0.8, 0.8, 0.8, 0.05);
        level.playSound(null, chestPos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.7F, 1.3F);

        BlockEntity be = level.getBlockEntity(chestPos);
        if (be instanceof ChestBlockEntity chest) {
            int slot = 0;
            for (ItemStack stack : items) {
                if (slot < 27) chest.setItem(slot++, stack);
            }
        }
    }

    private static void dropSupplies(ServerLevel level, Vec3 pos) {
        List<ItemStack> pool = new ArrayList<>();
        ItemStack[] candidates = {
                new ItemStack(Items.GOLDEN_APPLE, 3),
                new ItemStack(Items.ARROW, 32),
                new ItemStack(Items.COOKED_BEEF, 12),
                new ItemStack(Items.IRON_INGOT, 10),
                new ItemStack(Items.TNT, 8),
                new ItemStack(Items.ENDER_PEARL, 2),
                new ItemStack(Items.DIAMOND, 2),
                new ItemStack(Items.EXPERIENCE_BOTTLE, 16),
                new ItemStack(Items.TOTEM_OF_UNDYING, 1),
        };
        for (ItemStack stack : candidates) {
            if (level.getRandom().nextFloat() < 0.75f) pool.add(stack);
        }
        placeChestWith(level, pos, pool);
    }

    private static void guardDog(ServerLevel level, Vec3 pos, Entity owner) {
        Wolf wolf = new Wolf(entityType("wolf"), level);
        wolf.setPos(pos.x, pos.y + 0.5, pos.z);
        if (owner instanceof ServerPlayer sp) wolf.tame(sp);
        level.addFreshEntity(wolf);
        level.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y + 1, pos.z, 20, 0.8, 0.8, 0.8, 0.05);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.WOLF_SHAKE,
                SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    private static void playerBuff(ServerLevel level, Entity owner, MobEffectInstance effect) {
        if (owner instanceof ServerPlayer sp) {
            sp.addEffect(new MobEffectInstance(effect));
            level.playSound(null, sp.blockPosition(), SoundEvents.BEACON_ACTIVATE,
                    SoundSource.PLAYERS, 0.8F, 1.4F);
        }
    }

    private static void spawnVehicle(ServerLevel level, Vec3 pos, String kind, Entity owner) {
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(pos));
        double x = ground.getX() + 0.5, y = ground.getY() + 1.0, z = ground.getZ() + 0.5;
        switch (kind) {
            case "exo" -> {
                IronGolem golem = new IronGolem(entityType("iron_golem"), level);
                golem.setPos(x, y, z);
                level.addFreshEntity(golem);
            }
            case "frv" -> {
                Horse horse = new Horse(entityType("horse"), level);
                horse.setPos(x, y, z);
                horse.setTamed(true);
                if (horse.canUseSlot(EquipmentSlot.SADDLE)) {
                    horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
                }
                level.addFreshEntity(horse);
            }
            case "frv_supply" -> {
                Mule mule = new Mule(entityType("mule"), level);
                mule.setPos(x, y, z);
                mule.setTamed(true);
                if (mule.canUseSlot(EquipmentSlot.SADDLE)) {
                    mule.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
                }
                if (mule.canUseSlot(EquipmentSlot.CHEST)) {
                    mule.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.CHEST));
                }
                level.addFreshEntity(mule);
            }
            case "frv_fire" -> {
                Horse horse = new Horse(entityType("horse"), level);
                horse.setPos(x, y, z);
                horse.setTamed(true);
                if (horse.canUseSlot(EquipmentSlot.SADDLE)) {
                    horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
                }
                level.addFreshEntity(horse);
                weaponPod(level, pos, new ItemStack(Items.FLINT_AND_STEEL),
                        new ItemStack(Items.BLAZE_POWDER, 16));
            }
            case "td" -> {
                Ravager ravager = new Ravager(entityType("ravager"), level);
                ravager.setPos(x, y, z);
                level.addFreshEntity(ravager);
            }
        }
        level.playSound(null, BlockPos.containing(pos), SoundEvents.ANVIL_LAND,
                SoundSource.BLOCKS, 1.0F, 0.8F);
        level.sendParticles(ParticleTypes.CLOUD, x, y + 1, z, 30, 1, 1, 1, 0.08);
    }

    private static void shieldDome(ServerLevel level, Vec3 pos) {
        ShieldDomeEntity dome = new ShieldDomeEntity(ModEntities.SHIELD_DOME.get(), level);
        dome.setPos(pos.x, pos.y, pos.z);
        level.addFreshEntity(dome);
        level.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 1, pos.z, 30, 2, 2, 2, 0.05);
        level.playSound(null, BlockPos.containing(pos), SoundEvents.BEACON_ACTIVATE,
                SoundSource.BLOCKS, 1.0F, 1.2F);
    }

    private static void reinforce(ServerLevel level, Entity owner) {
        if (owner instanceof ServerPlayer sp) {
            sp.setHealth(sp.getMaxHealth());
            sp.addEffect(new MobEffectInstance(MobEffects.SATURATION, 200, 1));
            sp.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1200, 0));
            level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    sp.getX(), sp.getY() + 1, sp.getZ(), 40, 0.5, 1, 0.5, 0.1);
            level.playSound(null, sp.blockPosition(), SoundEvents.TOTEM_USE,
                    SoundSource.PLAYERS, 1.0F, 1.2F);
        }
    }

    private static void sosBeacon(ServerLevel level, Vec3 pos) {
        for (int i = 0; i < 5; i++) {
            Vec3 p = new Vec3(pos.x, pos.y + 2 + i * 3, pos.z);
            int delay = i * 12;
            schedule(level, delay, () -> {
                level.sendParticles(ParticleTypes.FIREWORK, p.x, p.y, p.z, 30, 1, 1, 1, 0.2);
                level.playSound(null, BlockPos.containing(p), SoundEvents.FIREWORK_ROCKET_LAUNCH,
                        SoundSource.BLOCKS, 1.0F, 1.0F);
            });
        }
        LightningBolt bolt = new LightningBolt(entityType("lightning_bolt"), level);
        bolt.setPos(pos.x, pos.y, pos.z);
        bolt.setVisualOnly(true);
        level.addFreshEntity(bolt);
    }

    private static void superEarthFlag(ServerLevel level, Vec3 pos, Entity owner) {
        BlockPos ground = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, BlockPos.containing(pos));
        BlockPos flagPos = ground.above();
        level.setBlock(flagPos, Blocks.BANNER.pick(DyeColor.WHITE).defaultBlockState(), 3);
        for (int i = 0; i < 3; i++) {
            int delay = i * 15;
            schedule(level, delay, () -> level.sendParticles(ParticleTypes.FIREWORK,
                    flagPos.getX() + 0.5, flagPos.getY() + 3, flagPos.getZ() + 0.5,
                    30, 1.5, 1.5, 1.5, 0.2));
        }
        level.playSound(null, flagPos, SoundEvents.FIREWORK_ROCKET_LAUNCH,
                SoundSource.BLOCKS, 1.0F, 0.8F);
        if (owner instanceof ServerPlayer sp) {
            sp.addEffect(new MobEffectInstance(MobEffects.SPEED, 1200, 1));
        }
    }
}
