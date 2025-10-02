package com.trickshotmlg.friendnet.adapter_spigot.Listeners;

import com.trickshotmlg.friendnet.adapter_spigot.SpigotPlayer;
import com.trickshotmlg.friendnet.core_api.interfaces.FriendService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener class for player join/quit events.
 */
public class PlayerStatusListener implements Listener {

    private final FriendService friendService;

    public PlayerStatusListener(FriendService friendService) {
        this.friendService = friendService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        SpigotPlayer spigotPlayer = new SpigotPlayer(event.getPlayer());

        friendService.setOnline(spigotPlayer.getUniqueId(), true);
        log(spigotPlayer.getName() + " joined!");
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        SpigotPlayer spigotPlayer = new SpigotPlayer(event.getPlayer());

        friendService.setOnline(spigotPlayer.getUniqueId(), false);
        log(spigotPlayer.getName() + " quit!");
    }

    private void log(String message) {
        System.out.println("[FriendNet] " + message); // simple logging
    }
}
