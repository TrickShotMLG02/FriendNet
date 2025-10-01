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

    @Override
    public void onEnable() {
        Platform platform = new SpigotPlatform(this);
        this.friendService = new FriendServiceImpl();

        // Example: listen to join/quit events and update online state
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                friendService.setOnline(event.getPlayer().getUniqueId(), true);

                String joinLog = "Player: " + event.getPlayer().getDisplayName() + " joined!";
                getLogger().info(joinLog);
            }

            @EventHandler
            public void onQuit(PlayerQuitEvent event) {
                friendService.setOnline(event.getPlayer().getUniqueId(), false);

                String quitLog = "Player: " + event.getPlayer().getDisplayName() + " quited!";
                getLogger().info(quitLog);
            }
        }, this);

        getLogger().info("FriendNet enabled!");
    }

    public FriendService getFriendService() {
        return friendService;
    }
}

