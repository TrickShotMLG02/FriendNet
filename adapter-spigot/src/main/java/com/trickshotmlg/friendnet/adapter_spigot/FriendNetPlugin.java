package com.trickshotmlg.friendnet.adapter_spigot;

import com.trickshotmlg.friendnet.FriendServiceImpl;
import com.trickshotmlg.friendnet.core_api.interfaces.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.Platform;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class FriendNetPlugin extends JavaPlugin {

    private FriendService friendService;
    private Platform platform;

    @Override
    public void onEnable() {
        initializePlatform();
        initializeServices();
        registerListeners();
        getLogger().info("FriendNet enabled!");
    }

    private void initializePlatform() {
        this.platform = new SpigotPlatform(this);
    }

    private void initializeServices() {
        this.friendService = new FriendServiceImpl();
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerStatusListener(friendService), this);
    }

    public FriendService getFriendService() {
        return friendService;
    }

    /**
     * Listener class for player join/quit events.
     */
    private static class PlayerStatusListener implements Listener {

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
}
