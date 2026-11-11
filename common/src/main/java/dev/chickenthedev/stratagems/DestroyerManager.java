package dev.chickenthedev.stratagems;

import dev.chickenthedev.stratagems.entity.DestroyerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

// Server-side: makes sure every player in the overworld has their own
// Super Destroyer holding station above them.
public class DestroyerManager {

    private static final Map<UUID, DestroyerEntity> DESTROYERS = new HashMap<>();

    /** The given player's destroyer, or null if they have none on station. */
    public static DestroyerEntity forOwner(UUID playerId) {
        DestroyerEntity d = DESTROYERS.get(playerId);
        return (d != null && !d.isRemoved()) ? d : null;
    }

    /** Finds a destroyer already loaded in the level for this owner (e.g. reloaded from disk). */
    private static DestroyerEntity findLoadedDestroyer(ServerLevel level, ServerPlayer player) {
        // Ships hold station above their owner, so a bounded scan at orbit
        // height around the player is enough to find a reloaded one.
        net.minecraft.world.phys.AABB box = net.minecraft.world.phys.AABB.ofSize(
                new net.minecraft.world.phys.Vec3(player.getX(), DestroyerEntity.ORBIT_HEIGHT, player.getZ()),
                256, 128, 256);
        UUID ownerId = player.getUUID();
        for (DestroyerEntity d : level.getEntitiesOfClass(DestroyerEntity.class, box)) {
            if (!d.isRemoved() && ownerId.equals(d.getOwnerId())) return d;
        }
        return null;
    }

    // Called on server stop: in single-player the JVM survives quit-to-title,
    // so the per-world ship map must not leak into the next world.
    public static void clearServerState() {
        DESTROYERS.clear();
    }

    public static void serverTick(MinecraftServer server) {
        Set<UUID> online = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.level() instanceof ServerLevel level)) continue;
            if (!level.dimension().equals(Level.OVERWORLD)) continue;
            online.add(player.getUUID());

            DestroyerEntity destroyer = DESTROYERS.get(player.getUUID());
            if (destroyer == null || destroyer.isRemoved() || destroyer.level() != level) {
                // Adopt-or-spawn: the destroyer persists to disk, so after a
                // server restart the saved ship reloads while our static map is
                // empty. Reuse it instead of spawning a duplicate each restart.
                destroyer = findLoadedDestroyer(level, player);
                if (destroyer == null) {
                    destroyer = new DestroyerEntity(ModEntities.DESTROYER.get(), level);
                    destroyer.setOwnerId(player.getUUID());
                    destroyer.setPos(player.getX(), DestroyerEntity.ORBIT_HEIGHT, player.getZ());
                    level.addFreshEntity(destroyer);
                    Constants.LOG.info("Super Destroyer on station for {}", player.getScoreboardName());
                } else {
                    Constants.LOG.info("Reacquired saved Super Destroyer for {}", player.getScoreboardName());
                }
                DESTROYERS.put(player.getUUID(), destroyer);
            }
        }
        // Drop tracking for players who logged out or left the overworld, and
        // discard their ship so ghost destroyers don't accumulate. A new ship
        // is created when they return. Armed state goes with them.
        DESTROYERS.entrySet().removeIf(entry -> {
            if (online.contains(entry.getKey())) return false;
            entry.getValue().discard();
            StratagemManager.clearArmed(entry.getKey());
            return true;
        });
    }
}
