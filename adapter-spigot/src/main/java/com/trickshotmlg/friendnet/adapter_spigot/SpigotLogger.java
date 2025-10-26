package com.trickshotmlg.friendnet.adapter_spigot;

import com.trickshotmlg.friendnet.core_api.interfaces.FriendNetLogger;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotLogger implements FriendNetLogger {

    private final JavaPlugin plugin;

    public SpigotLogger(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void info(String message) {
        plugin.getLogger().info(message);
    }

    @Override
    public void warn(String message) {
        plugin.getLogger().warning(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        plugin.getLogger().severe(message);
        if (throwable != null) throwable.printStackTrace();
    }

    @Override
    public void debug(String message) {
        plugin.getLogger().info("[DEBUG] " + message);
    }
}
