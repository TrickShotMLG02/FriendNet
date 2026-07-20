package com.trickshotmlg.friendnet.adapter_spigot.Listeners;

import com.trickshotmlg.friendnet.adapter_spigot.SpigotPlayer;
import com.trickshotmlg.friendnet.adapter_spigot.FriendNetPlugin;
import com.trickshotmlg.friendnet.adapter_spigot.Services.PlayerDataSaveQueue;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.FriendStatusNotifier;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.enums.EventSource;
import com.trickshotmlg.friendnet.core.events.EventBus;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


/**
 * Listener class for player join/quit events.
 */
public class PlayerStatusListener extends AbstractListener {

    private final FriendNetPlugin plugin;
    private final FriendService friendService;
    private final PlayerService playerService;
    private final DatabaseService databaseService;
    private final PlayerDataSaveQueue playerDataSaveQueue;

    public PlayerStatusListener(JavaPlugin plugin, FriendService friendService, PlayerService playerService, DatabaseService databaseService) {
        super(plugin);

        this.plugin = (FriendNetPlugin) plugin;
        this.friendService = friendService;
        this.playerService = playerService;
        this.databaseService = databaseService;
        this.playerDataSaveQueue = this.plugin.getPlayerDataSaveQueue();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        SpigotPlayer spigotPlayer = new SpigotPlayer(event.getPlayer());
        UUID playerId = spigotPlayer.getUniqueId();
        String lastDisplayName = event.getPlayer().getDisplayName();

        EventBus.publish(new com.trickshotmlg.friendnet.core.events.PlayerJoinEvent(EventSource.LOCAL, spigotPlayer));
        PlayerData initializedPlayerData = playerService.initPlayer(playerId);
        initializedPlayerData.setLastDisplayName(lastDisplayName);

        if (plugin.isProxyBackendMode()) {
            playerService.putPlayerData(initializedPlayerData);
            playerService.setOnline(playerId, true);
            Logger.debug(spigotPlayer.getName() + " joined in proxy backend mode; status notification is owned by proxy.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // load player data
            Optional<PlayerData> pld = databaseService.find(playerId, PlayerData.class);
            PlayerData playerData;
            if (pld.isPresent()) {
                playerData = pld.get();
                playerData.setLastDisplayName(lastDisplayName);
                Logger.debug("playerData: " + playerData);
            } else {
                playerData = initializedPlayerData;
            }
            playerService.putPlayerData(playerData);
            playerService.setOnline(playerId, playerData.isShowOnlineStatus());
            playerDataSaveQueue.saveNow(playerData);

            // load player friendships into memory
            Optional<Set<FriendshipData>> friendships = databaseService.findAll(playerId, FriendshipData.class);
            if (friendships.isPresent()) {
                for (FriendshipData friendshipData : friendships.get()) {
                    friendService.putFriendshipData(friendshipData);
                    UUID otherPlayerId = friendshipData.getOtherPlayerId(playerId);
                    databaseService.find(otherPlayerId, PlayerData.class).ifPresent(playerService::putPlayerData);
                }
            }

            if (playerData.isShowOnlineStatus()) {
                Bukkit.getScheduler().runTask(plugin, () -> FriendStatusNotifier.notifyOnline(plugin, playerId));
            }
            notifyPendingRequests(playerId, playerData);
        });

        Logger.debug(spigotPlayer.getName() + " joined!");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        SpigotPlayer spigotPlayer = new SpigotPlayer(event.getPlayer());
        UUID playerId = spigotPlayer.getUniqueId();
        boolean wasVisibleOnline = playerService.isOnline(playerId);

        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            playerData = playerService.initPlayer(playerId);
        }

        if (wasVisibleOnline) {
            playerData.setLastSeen();
        }
        playerData.setLastDisplayName(event.getPlayer().getDisplayName());
        playerService.setOnline(playerId, false);

        if (wasVisibleOnline && !plugin.isProxyBackendMode()) {
            FriendStatusNotifier.notifyOffline(plugin, playerId);
        }

        PlayerData playerDataToSave = playerData;
        if (!plugin.isProxyBackendMode()) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> playerDataSaveQueue.saveNow(playerDataToSave));
        }

        // remove playerData as it is no longer needed
        playerService.removePlayerData(playerId);


        Logger.debug(spigotPlayer.getName() + " quit!");
    }

    private void notifyPendingRequests(UUID playerId, PlayerData playerData) {
        if (playerData == null || !playerData.isFriendRequestNotifications()) {
            return;
        }

        int pendingRequestCount = friendService.getPendingRequests(playerId).size();
        if (pendingRequestCount <= 0) {
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () ->
                MessageManager.send(playerId, "requestList.pendingSummary", Map.of("count", pendingRequestCount))
        );
    }
}
