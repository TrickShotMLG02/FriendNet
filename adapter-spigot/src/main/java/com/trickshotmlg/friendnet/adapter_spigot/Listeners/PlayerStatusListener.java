package com.trickshotmlg.friendnet.adapter_spigot.Listeners;

import com.trickshotmlg.friendnet.adapter_spigot.SpigotPlayer;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;
import java.util.Set;


/**
 * Listener class for player join/quit events.
 */
public class PlayerStatusListener extends AbstractListener {

    private final FriendService friendService;
    private final PlayerService playerService;
    private final DatabaseService databaseService;

    public PlayerStatusListener(JavaPlugin plugin, FriendService friendService, PlayerService playerService, DatabaseService databaseService) {
        super(plugin);

        this.friendService = friendService;
        this.playerService = playerService;
        this.databaseService = databaseService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        SpigotPlayer spigotPlayer = new SpigotPlayer(event.getPlayer());

        // load player data
        Optional<PlayerData> pld = databaseService.find(spigotPlayer.getUniqueId(), PlayerData.class);
        if (pld.isPresent()) {
            PlayerData playerData = pld.get();
            Logger.debug("playerData: " + playerData);
            playerService.putPlayerData(playerData);
        } else {
            playerService.initPlayer(spigotPlayer.getUniqueId());
        }

        // load player friendships into memory
        Optional<Set<FriendshipData>> friendships = databaseService.findAll(spigotPlayer.getUniqueId(), FriendshipData.class);
        if (friendships.isPresent()) {
            for (FriendshipData friendshipData : friendships.get()) {
                friendService.putFriendshipData(friendshipData);
            }
        }


        //TODO: Check if player has their status set to offline
        //friendService.setOnline(spigotPlayer.getUniqueId(), true);

        Logger.debug(spigotPlayer.getName() + " joined!");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        SpigotPlayer spigotPlayer = new SpigotPlayer(event.getPlayer());

        // TODO: only update last seen if player status was online before
        PlayerData playerData = playerService.getPlayerData(spigotPlayer.getUniqueId());
        // TODO: set player status to offline
        playerData.setLastSeen();
        databaseService.save(playerData);

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
        playerService.removePlayerData(spigotPlayer.getUniqueId());


        Logger.debug(spigotPlayer.getName() + " quit!");
    }
}
