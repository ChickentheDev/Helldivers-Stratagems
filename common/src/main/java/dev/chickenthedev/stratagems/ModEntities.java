package dev.chickenthedev.stratagems;

import dev.chickenthedev.stratagems.entity.DestroyerEntity;
import dev.chickenthedev.stratagems.entity.SentryEntity;
import dev.chickenthedev.stratagems.entity.ShieldDomeEntity;
import dev.chickenthedev.stratagems.entity.StratagemBeaconEntity;
import dev.chickenthedev.stratagems.platform.services.IRegistryHelper;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.Supplier;

public class ModEntities {

    public static Supplier<EntityType<StratagemBeaconEntity>> BEACON;
    public static Supplier<EntityType<ShieldDomeEntity>> SHIELD_DOME;
    public static Supplier<EntityType<DestroyerEntity>> DESTROYER;
    public static Supplier<EntityType<SentryEntity>> SENTRY;

    public static void register(IRegistryHelper registries) {
        BEACON = registries.registerEntityType("stratagem_beacon", () ->
                EntityType.Builder.<StratagemBeaconEntity>of(StratagemBeaconEntity::new, MobCategory.MISC)
                        .sized(0.25F, 0.25F)
                        .clientTrackingRange(8)
                        .updateInterval(10)
                        .build(key("stratagem_beacon")));
        SHIELD_DOME = registries.registerEntityType("shield_dome", () ->
                EntityType.Builder.<ShieldDomeEntity>of(ShieldDomeEntity::new, MobCategory.MISC)
                        .sized(0.5F, 0.5F)
                        .clientTrackingRange(10)
                        .updateInterval(20)
                        .build(key("shield_dome")));
        // Big cull box on purpose: the rendered ship is far larger than a normal entity.
        DESTROYER = registries.registerEntityType("super_destroyer", () ->
                EntityType.Builder.<DestroyerEntity>of(DestroyerEntity::new, MobCategory.MISC)
                        .sized(64.0F, 24.0F)
                        .clientTrackingRange(32)
                        .updateInterval(3)
                        .build(key("super_destroyer")));
        SENTRY = registries.registerEntityType("sentry", () ->
                EntityType.Builder.<SentryEntity>of(SentryEntity::new, MobCategory.MISC)
                        .sized(0.9F, 1.2F)
                        .clientTrackingRange(10)
                        .updateInterval(3)
                        .build(key("sentry")));
    }

    private static ResourceKey<EntityType<?>> key(String id) {
        return ResourceKey.create(Registries.ENTITY_TYPE,
                Identifier.fromNamespaceAndPath(Constants.MOD_ID, id));
    }
}
