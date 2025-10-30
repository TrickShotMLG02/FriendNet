package com.trickshotmlg.friendnet.adapter_spigot.Configs;

import org.bukkit.plugin.java.JavaPlugin;


public class ConfigManager {

    private final JavaPlugin plugin;

    private SpigotConfig pluginConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;

        pluginConfig = new SpigotConfig(plugin, "config.yml");
    }

    public void InitializeConfigs() {
        pluginConfig.load();
    }

}
