package com.trickshotmlg.friendnet.adapter_spigot.Configs;

import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.interfaces.AbstractConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class SpigotConfig implements AbstractConfig {

    protected final JavaPlugin plugin;
    protected final String fileName;
    protected File file;
    protected FileConfiguration config;

    public SpigotConfig(JavaPlugin plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName;
    }

    @Override
    public File getFile() {
        return file;
    }

    public FileConfiguration getRawConfig() {
        return config;
    }

    @Override
    public boolean load() {
        try {
            file = new File(plugin.getDataFolder(), fileName);

            if (!file.exists()) {
                plugin.saveResource(fileName, false);
            }

            config = YamlConfiguration.loadConfiguration(file);
            return true;
        } catch (Exception e) {
            Logger.error("Error loading config file: " + fileName, e);
            return false;
        }
    }

    @Override
    public boolean save() {
        try {
            config.save(file);
            return true;
        } catch (IOException e) {
            Logger.error("Could not save config file: " + fileName, e);
            return false;
        }
    }

    @Override
    public boolean reload() {
        Logger.debug("Reloading config file: " + fileName);
        return load();
    }

    @Override
    public boolean reset() {
        return false;
    }

    @Override
    public boolean initDefaults() {
        return false;
    }

    @Override
    public Optional<String> getString(String path) {
        return Optional.ofNullable(config.getString(path));
    }

    @Override
    public Object get(String path) {
        return config.get(path);
    }
}
