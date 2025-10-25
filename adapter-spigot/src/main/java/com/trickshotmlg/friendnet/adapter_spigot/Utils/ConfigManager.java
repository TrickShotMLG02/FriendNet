package com.trickshotmlg.friendnet.adapter_spigot.Utils;

import com.trickshotmlg.friendnet.adapter_spigot.Configs.PluginConfig;
import org.bukkit.plugin.java.JavaPlugin;


public class ConfigManager {

    private final JavaPlugin plugin;

    private PluginConfig pluginConfig;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;

        pluginConfig = new PluginConfig();
    }

    public void InitializeConfigs() {
        pluginConfig.LoadConfig();
    }

}
