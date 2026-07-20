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
    private final ConcurrentHashMap<UUID, PlayerData> dirtySnapshots = new ConcurrentHashMap<>();

    private ScheduledTask flushTask;
    private volatile boolean stopped;

    public VelocityPlayerDataSaveQueue(FriendNetVelocityPlugin plugin, PlayerService playerService, DatabaseService databaseService) {
        this.plugin = plugin;
        this.playerService = playerService;
        this.databaseService = databaseService;
    }

    public void start(long intervalSeconds) {
        if (intervalSeconds <= 0) {
            return;
        }

        stopped = false;
        flushTask = plugin.getServer().getScheduler()
                .buildTask(plugin, this::flushDirtyPlayers)
                .repeat(intervalSeconds, TimeUnit.SECONDS)
                .schedule();
    }

    public void stopAndFlush() {
        stopped = true;
        if (flushTask != null) {
            flushTask.cancel();
            flushTask = null;
        }

        flushDirtyPlayers();
    }

    public void markDirty(UUID playerId) {
        if (stopped || playerId == null) {
            return;
        }

        dirtyPlayers.add(playerId);
    }

    public void markDirty(PlayerData playerData) {
        if (stopped || playerData == null) {
            return;
        }

        dirtySnapshots.put(playerData.getPlayerId(), playerData);
        dirtyPlayers.add(playerData.getPlayerId());
    }

    public void clearDirty(UUID playerId) {
        if (playerId != null) {
            dirtyPlayers.remove(playerId);
            dirtySnapshots.remove(playerId);
        }
    }

    public void saveNow(PlayerData playerData) {
        if (stopped || playerData == null) {
            return;
        }

        databaseService.save(playerData);
        clearDirty(playerData.getPlayerId());
    }

    public synchronized void flushDirtyPlayers() {
        if (dirtyPlayers.isEmpty()) {
            return;
        }

        for (UUID playerId : Set.copyOf(dirtyPlayers)) {
            PlayerData playerData = dirtySnapshots.getOrDefault(playerId, playerService.getPlayerData(playerId));
            if (playerData == null) {
                clearDirty(playerId);
                continue;
            }

            databaseService.save(playerData);
            clearDirty(playerId);
        }

        Logger.debug("Flushed dirty player data queue");
    }
}
