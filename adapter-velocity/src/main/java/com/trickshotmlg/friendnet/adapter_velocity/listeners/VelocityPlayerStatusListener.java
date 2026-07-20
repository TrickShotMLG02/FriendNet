package com.trickshotmlg.friendnet.adapter_velocity.listeners;

import com.trickshotmlg.friendnet.adapter_velocity.FriendNetVelocityPlugin;
import com.trickshotmlg.friendnet.adapter_velocity.VelocityPlayer;
import com.trickshotmlg.friendnet.adapter_velocity.services.VelocityPlayerDataSaveQueue;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core.events.EventBus;
import com.trickshotmlg.friendnet.core_api.enums.EventSource;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class VelocityPlayerStatusListener {

    private final FriendNetVelocityPlugin plugin;
    private final FriendService friendService;
    private final PlayerService playerService;
    private final DatabaseService databaseService;
    private final VelocityPlayerDataSaveQueue playerDataSaveQueue;

    public VelocityPlayerStatusListener(
            FriendNetVelocityPlugin plugin,
            FriendService friendService,
            PlayerService playerService,
            DatabaseService databaseService
    ) {
        this.plugin = plugin;
        this.friendService = friendService;
        this.playerService = playerService;
        this.databaseService = databaseService;
        this.playerDataSaveQueue = plugin.getPlayerDataSaveQueue();
    }

    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        VelocityPlayer velocityPlayer = new VelocityPlayer(event.getPlayer());
        UUID playerId = velocityPlayer.getUniqueId();
        String lastDisplayName = velocityPlayer.getDisplayName();

        EventBus.publish(new com.trickshotmlg.friendnet.core.events.PlayerJoinEvent(EventSource.LOCAL, velocityPlayer));
        PlayerData initializedPlayerData = playerService.initPlayer(playerId);
        initializedPlayerData.setLastDisplayName(lastDisplayName);

        plugin.getPlatform().runAsync(() -> {
            Optional<PlayerData> storedPlayerData = databaseService.find(playerId, PlayerData.class);
            PlayerData playerData;
            if (storedPlayerData.isPresent()) {
                playerData = storedPlayerData.get();
                playerData.setLastDisplayName(lastDisplayName);
                Logger.debug("playerData: " + playerData);
            } else {
                playerData = initializedPlayerData;
            }

            playerService.putPlayerData(playerData);
            playerService.setOnline(playerId, playerData.isShowOnlineStatus());
            playerDataSaveQueue.saveNow(playerData);

            Optional<Set<FriendshipData>> friendships = databaseService.findAll(playerId, FriendshipData.class);
            friendships.ifPresent(friendshipDataSet -> {
                for (FriendshipData friendshipData : friendshipDataSet) {
                    friendService.putFriendshipData(friendshipData);
                    UUID otherPlayerId = friendshipData.getOtherPlayerId(playerId);
                    databaseService.find(otherPlayerId, PlayerData.class).ifPresent(playerService::putPlayerData);
                }
            });
        });

        Logger.debug(velocityPlayer.getName() + " joined the proxy!");
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        VelocityPlayer velocityPlayer = new VelocityPlayer(event.getPlayer());
        UUID playerId = velocityPlayer.getUniqueId();
        boolean wasVisibleOnline = playerService.isOnline(playerId);

        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            playerData = playerService.initPlayer(playerId);
        }

        if (wasVisibleOnline) {
            playerData.setLastSeen();
        }
        playerData.setLastDisplayName(velocityPlayer.getDisplayName());
        playerService.setOnline(playerId, false);

        PlayerData playerDataToSave = playerData;
        plugin.getPlatform().runAsync(() -> playerDataSaveQueue.saveNow(playerDataToSave));
        playerService.removePlayerData(playerId);

        Logger.debug(velocityPlayer.getName() + " left the proxy!");
    }
}
