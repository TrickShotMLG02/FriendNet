package com.trickshotmlg.friendnet.adapter_spigot.Listeners;

import com.trickshotmlg.friendnet.adapter_spigot.SpigotPlayer;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Listener class for player join/quit events.
 */
public class PlayerStatusListener extends AbstractListener {

    private final FriendService friendService;
    private final PlayerService playerService;

    public PlayerStatusListener(JavaPlugin plugin, FriendService friendService, PlayerService playerService) {
        super(plugin);

        this.friendService = friendService;
        this.playerService = playerService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        SpigotPlayer spigotPlayer = new SpigotPlayer(event.getPlayer());

        //TODO: Check if player has their status set to offline
        friendService.setOnline(spigotPlayer.getUniqueId(), true);

        Logger.debug(spigotPlayer.getName() + " joined!");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        SpigotPlayer spigotPlayer = new SpigotPlayer(event.getPlayer());

        friendService.setOnline(spigotPlayer.getUniqueId(), false);

        Logger.debug(spigotPlayer.getName() + " quit!");
    }
}
