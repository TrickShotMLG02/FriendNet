package com.trickshotmlg.friendnet.adapter_spigot.Services;

import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataSaveQueue {
    private final JavaPlugin plugin;
    private final PlayerService playerService;
    private final DatabaseService databaseService;
    private final boolean enabled;
    private final Set<UUID> dirtyPlayers = ConcurrentHashMap.newKeySet();

    private BukkitTask flushTask;

    public PlayerDataSaveQueue(JavaPlugin plugin, PlayerService playerService, DatabaseService databaseService, boolean enabled) {
        this.plugin = plugin;
        this.playerService = playerService;
        this.databaseService = databaseService;
        this.enabled = enabled;
    }

    public void start(long intervalTicks) {
        if (!enabled || intervalTicks <= 0) {
            return;
        }

        flushTask = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                this::flushDirtyPlayers,
                intervalTicks,
                intervalTicks
        );
    }

    public void stopAndFlush() {
        if (flushTask != null) {
            flushTask.cancel();
            flushTask = null;
        }

        flushDirtyPlayers();
    }

    public void markDirty(UUID playerId) {
        if (!enabled || playerId == null) {
            return;
        }

        dirtyPlayers.add(playerId);
    }

    public void clearDirty(UUID playerId) {
        if (playerId != null) {
            dirtyPlayers.remove(playerId);
        }
    }

    public void saveNow(PlayerData playerData) {
        if (!enabled || playerData == null) {
            return;
        }

        databaseService.save(playerData);
        clearDirty(playerData.getPlayerId());
    }

    public void flushDirtyPlayers() {
        if (!enabled || dirtyPlayers.isEmpty()) {
            return;
        }

        for (UUID playerId : Set.copyOf(dirtyPlayers)) {
            PlayerData playerData = playerService.getPlayerData(playerId);
            if (playerData == null) {
                dirtyPlayers.remove(playerId);
                continue;
            }

            databaseService.save(playerData);
            dirtyPlayers.remove(playerId);
        }

        Logger.debug("Flushed dirty player data queue");
    }
}
