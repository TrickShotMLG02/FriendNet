package com.trickshotmlg.friendnet.adapter_velocity.services;

import com.trickshotmlg.friendnet.adapter_velocity.FriendNetVelocityPlugin;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import com.velocitypowered.api.scheduler.ScheduledTask;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class VelocityPlayerDataSaveQueue {

    private final FriendNetVelocityPlugin plugin;
    private final PlayerService playerService;
    private final DatabaseService databaseService;
    private final Set<UUID> dirtyPlayers = ConcurrentHashMap.newKeySet();

    private ScheduledTask flushTask;

    public VelocityPlayerDataSaveQueue(FriendNetVelocityPlugin plugin, PlayerService playerService, DatabaseService databaseService) {
        this.plugin = plugin;
        this.playerService = playerService;
        this.databaseService = databaseService;
    }

    public void start(long intervalSeconds) {
        if (intervalSeconds <= 0) {
            return;
        }

        flushTask = plugin.getServer().getScheduler()
                .buildTask(plugin, this::flushDirtyPlayers)
                .repeat(intervalSeconds, TimeUnit.SECONDS)
                .schedule();
    }

    public void stopAndFlush() {
        if (flushTask != null) {
            flushTask.cancel();
            flushTask = null;
        }

        flushDirtyPlayers();
    }

    public void markDirty(UUID playerId) {
        if (playerId != null) {
            dirtyPlayers.add(playerId);
        }
    }

    public void clearDirty(UUID playerId) {
        if (playerId != null) {
            dirtyPlayers.remove(playerId);
        }
    }

    public void saveNow(PlayerData playerData) {
        if (playerData == null) {
            return;
        }

        databaseService.save(playerData);
        clearDirty(playerData.getPlayerId());
    }

    public void flushDirtyPlayers() {
        if (dirtyPlayers.isEmpty()) {
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
