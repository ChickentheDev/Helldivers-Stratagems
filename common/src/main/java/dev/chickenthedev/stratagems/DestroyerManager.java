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

    public static void serverTick(MinecraftServer server) {
        Set<UUID> online = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (!(player.level() instanceof ServerLevel level)) continue;
            if (!level.dimension().equals(Level.OVERWORLD)) continue;
            online.add(player.getUUID());

            DestroyerEntity destroyer = DESTROYERS.get(player.getUUID());
            if (destroyer == null || destroyer.isRemoved() || destroyer.level() != level) {
                destroyer = new DestroyerEntity(ModEntities.DESTROYER.get(), level);
                destroyer.setOwnerId(player.getUUID());
                destroyer.setPos(player.getX(), DestroyerEntity.ORBIT_HEIGHT, player.getZ());
                level.addFreshEntity(destroyer);
                DESTROYERS.put(player.getUUID(), destroyer);
                Constants.LOG.info("Super Destroyer on station for {}", player.getScoreboardName());
            }
        }
        // Drop tracking for players who logged out or left the overworld, and
        // discard their ship so ghost destroyers don't accumulate. A new ship
        // is created when they return.
        DESTROYERS.entrySet().removeIf(entry -> {
            if (online.contains(entry.getKey())) return false;
            entry.getValue().discard();
            return true;
        });
    }
}
