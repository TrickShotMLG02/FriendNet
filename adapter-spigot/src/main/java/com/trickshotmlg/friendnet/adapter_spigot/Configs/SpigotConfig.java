package com.trickshotmlg.friendnet.adapter_spigot.Configs;

import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.interfaces.AbstractConfig;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
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

            // Only load if the file exists
            if (!file.exists()) {
                Logger.warn("Config file not found: " + fileName);
                return false;
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
        try {
            file = new File(plugin.getDataFolder(), fileName);
            if (file.exists() && !file.delete()) {
                Logger.error("Could not delete config file while resetting: " + fileName, null);
                return false;
            }

            return initDefaults();
        } catch (Exception e) {
            Logger.error("Error resetting config file: " + fileName, e);
            return false;
        }
    }

    @Override
    public boolean initDefaults() {
        file = new File(plugin.getDataFolder(), fileName);

        // check if file and directories exist
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(fileName, false);
        }

        if (!load()) {
            Logger.error("Failed to load messages file after creating defaults.", null);
            return false;
        }

        // Apply defaults from JAR in memory only. Saving here rewrites YAML and strips comments.
        try (InputStream resource = plugin.getResource(fileName)) {
            if (resource == null) {
                Logger.error("Default resource not found: " + fileName, null);
                return false;
            }

            InputStreamReader reader = new InputStreamReader(resource, StandardCharsets.UTF_8);
            YamlConfiguration defaults = YamlConfiguration.loadConfiguration(reader);
            config.setDefaults(defaults);
            config.options().copyDefaults(false);
            return true;
        } catch (IOException e) {
            Logger.error("Failed to apply defaults to messages file.", e);
            return false;
        }
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
