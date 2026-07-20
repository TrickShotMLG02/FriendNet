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
        Logger.debug("Started Velocity player data save queue with interval " + intervalSeconds + "s");
    }

    public void stopAndFlush() {
        Logger.debug("Stopping Velocity player data save queue");
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
        Logger.debug("Marked Velocity player data dirty: " + playerId);
    }

    public void markDirty(PlayerData playerData) {
        if (stopped || playerData == null) {
            return;
        }

        dirtySnapshots.put(playerData.getPlayerId(), playerData);
        dirtyPlayers.add(playerData.getPlayerId());
        Logger.debug("Marked Velocity player data dirty snapshot: " + playerData.getPlayerId());
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
        Logger.debug("Saved Velocity player data immediately: " + playerData.getPlayerId());
    }

    public synchronized void flushDirtyPlayers() {
        if (dirtyPlayers.isEmpty()) {
            Logger.debug("Skipped Velocity player data flush; queue is empty");
            return;
        }

        int saved = 0;
        for (UUID playerId : Set.copyOf(dirtyPlayers)) {
            PlayerData playerData = dirtySnapshots.getOrDefault(playerId, playerService.getPlayerData(playerId));
            if (playerData == null) {
                clearDirty(playerId);
                continue;
            }

            databaseService.save(playerData);
            clearDirty(playerId);
            saved++;
        }

        Logger.debug("Flushed Velocity player data queue: " + saved + " player(s)");
    }
}
