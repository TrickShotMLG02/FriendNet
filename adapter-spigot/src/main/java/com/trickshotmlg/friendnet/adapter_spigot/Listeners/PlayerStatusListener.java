package com.trickshotmlg.friendnet.adapter_spigot.Listeners;

import com.trickshotmlg.friendnet.adapter_spigot.SpigotPlayer;
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
import java.util.Set;
import java.util.UUID;


/**
 * Listener class for player join/quit events.
 */
public class PlayerStatusListener extends AbstractListener {

    private final JavaPlugin plugin;
    private final FriendService friendService;
    private final PlayerService playerService;
    private final DatabaseService databaseService;

    public PlayerStatusListener(JavaPlugin plugin, FriendService friendService, PlayerService playerService, DatabaseService databaseService) {
        super(plugin);

        this.plugin = plugin;
        this.friendService = friendService;
        this.playerService = playerService;
        this.databaseService = databaseService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        SpigotPlayer spigotPlayer = new SpigotPlayer(event.getPlayer());
        UUID playerId = spigotPlayer.getUniqueId();

        // TODO: Remove this as it is only for testing purposes
        EventBus.publish(new com.trickshotmlg.friendnet.core.events.PlayerJoinEvent(EventSource.LOCAL, spigotPlayer));
        playerService.initPlayer(playerId);

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            // load player data
            Optional<PlayerData> pld = databaseService.find(playerId, PlayerData.class);
            if (pld.isPresent()) {
                PlayerData playerData = pld.get();
                Logger.debug("playerData: " + playerData);
                playerService.putPlayerData(playerData);
            } else {
                playerService.initPlayer(playerId);
            }

            // load player friendships into memory
            Optional<Set<FriendshipData>> friendships = databaseService.findAll(playerId, FriendshipData.class);
            if (friendships.isPresent()) {
                for (FriendshipData friendshipData : friendships.get()) {
                    friendService.putFriendshipData(friendshipData);
                }
            }
        });


        //TODO: Check if player has their status set to offline
        //friendService.setOnline(spigotPlayer.getUniqueId(), true);

        // TODO: Remove this junk
        /*
        SpigotLocaleManager l = FriendNetPlugin.LocaleManager;
        l.loadLocales();
        l.setDefaultLocale(Locale.DE);
        Player player = event.getPlayer();
        if (player != null) {
            UUID uuid = player.getUniqueId();
            String m = l.getMessage(uuid, "test", "noPermission");
            spigotPlayer.sendMessage(m);
        }
        */

        Logger.debug(spigotPlayer.getName() + " joined!");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        SpigotPlayer spigotPlayer = new SpigotPlayer(event.getPlayer());
        UUID playerId = spigotPlayer.getUniqueId();

        // TODO: only update last seen if player status was online before
        PlayerData playerData = playerService.getPlayerData(playerId);
        if (playerData == null) {
            playerData = playerService.initPlayer(playerId);
        }
        // TODO: set player status to offline
        playerData.setLastSeen();
        playerData.setLastDisplayName(event.getPlayer().getDisplayName());
        PlayerData playerDataToSave = playerData;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> databaseService.save(playerDataToSave));

        // TODO: Remove this junk
        //friendService.acceptFriendRequest(UUID.fromString("0f8fbcdd-dd36-4409-a689-dc9fb761b55d"), spigotPlayer.getUniqueId());

        /*
        FriendshipData d1 = new FriendshipData(spigotPlayer.getUniqueId(), UUID.fromString("efbdd36b-a9b0-4d99-9a6a-b92b6f3e149e"));
        FriendshipData d2 = new FriendshipData(spigotPlayer.getUniqueId(), UUID.fromString("412334cc-8ee9-4edf-bfba-6a8267aece38"));
        FriendshipData d3 = new FriendshipData(UUID.fromString("0f8fbcdd-dd36-4409-a689-dc9fb761b55d"), spigotPlayer.getUniqueId());

        databaseService.save(d1);
        databaseService.save(d2);
        databaseService.save(d3);

        */

        //friendService.setOnline(spigotPlayer.getUniqueId(), false);

        // remove playerData as it is no longer needed
        playerService.removePlayerData(playerId);


        Logger.debug(spigotPlayer.getName() + " quit!");
    }
}
