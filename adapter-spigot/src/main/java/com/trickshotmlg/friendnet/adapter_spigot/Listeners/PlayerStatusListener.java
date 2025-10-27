package com.trickshotmlg.friendnet.adapter_spigot.Listeners;

import com.trickshotmlg.friendnet.adapter_spigot.SpigotPlayer;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;


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

        Optional<PlayerData> pld = databaseService.find(spigotPlayer.getUniqueId(), PlayerData.class);
        if (pld.isPresent()) {
            PlayerData playerData = pld.get();
            Logger.debug("playerData: " + playerData);
            playerService.putPlayerData(playerData);
        } else {
            playerService.initPlayer(spigotPlayer.getUniqueId());
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

        //friendService.setOnline(spigotPlayer.getUniqueId(), false);

        // remove playerData as it is no longer needed
        playerService.removePlayerData(spigotPlayer.getUniqueId());

        Logger.debug(spigotPlayer.getName() + " quit!");
    }
}
