package com.trickshotmlg.friendnet.adapter_spigot;

import com.trickshotmlg.friendnet.core_api.interfaces.FriendNetLogger;
import org.bukkit.plugin.Plugin;

public class SpigotLogger implements FriendNetLogger {

    private static SpigotLogger instance;

    private final Plugin plugin;

    public SpigotLogger(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes the logger singleton.
     * Should be called once in the plugin's onEnable().
     */
    public static void initialize(Plugin plugin) {
        if (instance == null) {
            instance = new SpigotLogger(plugin);
        }
    }

    /**
     * Returns the singleton instance.
     *
     * @return the global SpigotLogger instance
     * @throws IllegalStateException if initialize() has not been called
     */
    public static SpigotLogger getInstance() {
        if (instance == null) {
            throw new IllegalStateException("SpigotLogger is not initialized!");
        }
        return instance;
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
