package com.trickshotmlg.friendnet.adapter_velocity.listeners;

import com.trickshotmlg.friendnet.adapter_velocity.FriendNetVelocityPlugin;
import com.trickshotmlg.friendnet.adapter_velocity.VelocityPlayer;
import com.trickshotmlg.friendnet.adapter_velocity.services.VelocityPlayerDataSaveQueue;
import com.trickshotmlg.friendnet.adapter_velocity.utils.VelocityFriendStatusNotifier;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core.events.EventBus;
import com.trickshotmlg.friendnet.core_api.enums.EventSource;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.NetworkAuthorityService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.NetworkPlayerPresence;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class VelocityPlayerStatusListener {

    private final FriendNetVelocityPlugin plugin;
    private final FriendService friendService;
    private final PlayerService playerService;
    private final DatabaseService databaseService;
    private final NetworkAuthorityService networkAuthorityService;
    private final VelocityPlayerDataSaveQueue playerDataSaveQueue;

    public VelocityPlayerStatusListener(
            FriendNetVelocityPlugin plugin,
            FriendService friendService,
            PlayerService playerService,
            DatabaseService databaseService,
            NetworkAuthorityService networkAuthorityService
    ) {
        this.plugin = plugin;
        this.friendService = friendService;
        this.playerService = playerService;
        this.databaseService = databaseService;
        this.networkAuthorityService = networkAuthorityService;
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
            } else {
                playerData = initializedPlayerData;
            }
            Logger.debug("Loaded player data on proxy join: " + playerData);

            playerService.putPlayerData(playerData);
            playerService.setOnline(playerId, playerData.isShowOnlineStatus());
            networkAuthorityService.setPresence(toPresence(velocityPlayer, playerData, true));
            playerDataSaveQueue.saveNow(playerData);

            Optional<Set<FriendshipData>> friendships = databaseService.findAll(playerId, FriendshipData.class);
            friendships.ifPresent(friendshipDataSet -> {
                for (FriendshipData friendshipData : friendshipDataSet) {
                    friendService.putFriendshipData(friendshipData);
                    UUID otherPlayerId = friendshipData.getOtherPlayerId(playerId);
                    databaseService.find(otherPlayerId, PlayerData.class).ifPresent(playerService::putPlayerData);
                }
            });

            if (playerData.isShowOnlineStatus()) {
                VelocityFriendStatusNotifier.notifyOnline(plugin, playerId);
            }
        });

        Logger.debug(velocityPlayer.getName() + " joined the proxy!");
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        VelocityPlayer velocityPlayer = new VelocityPlayer(event.getPlayer());
        UUID playerId = velocityPlayer.getUniqueId();
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            return;
        }

        networkAuthorityService.setPresence(new NetworkPlayerPresence(
                playerId,
                velocityPlayer.getName(),
                velocityPlayer.getDisplayName(),
                event.getServer().getServerInfo().getName(),
                true,
                playerData.isShowOnlineStatus(),
                playerData.getLastSeen()
        ));
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
        networkAuthorityService.setPresence(toPresence(velocityPlayer, playerData, false));
        playerService.setOnline(playerId, false);

        if (wasVisibleOnline) {
            VelocityFriendStatusNotifier.notifyOffline(plugin, playerId);
        }

        playerDataSaveQueue.saveNow(playerData);
        networkAuthorityService.removePresence(playerId);
        playerService.removePlayerData(playerId);

        Logger.debug(velocityPlayer.getName() + " left the proxy!");
    }

    private NetworkPlayerPresence toPresence(VelocityPlayer player, PlayerData playerData, boolean online) {
        return new NetworkPlayerPresence(
                player.getUniqueId(),
                player.getName(),
                player.getDisplayName(),
                player.getCurrentServerName().orElse(null),
                online,
                playerData.isShowOnlineStatus(),
                playerData.getLastSeen()
        );
    }
}
