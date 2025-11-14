package com.trickshotmlg.friendnet.adapter_spigot;

import com.trickshotmlg.friendnet.adapter_spigot.Commands.FriendCommand;
import com.trickshotmlg.friendnet.adapter_spigot.Configs.SpigotLocaleManager;
import com.trickshotmlg.friendnet.adapter_spigot.Listeners.GUIListener;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotLogger;
import com.trickshotmlg.friendnet.core.FriendServiceImpl;
import com.trickshotmlg.friendnet.adapter_spigot.Listeners.PlayerStatusListener;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core.PlayerServiceImpl;
import com.trickshotmlg.friendnet.core.database.DatabaseServiceImpl;
import com.trickshotmlg.friendnet.core.events.PlayerJoinEvent;
import com.trickshotmlg.friendnet.core_api.events.EventBus;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.Platform;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public final class FriendNetPlugin extends JavaPlugin {
    private static boolean DEBUG = true;

    public static SpigotLocaleManager LocaleManager;

    private FriendService friendService;
    private PlayerService playerService;
    private DatabaseService databaseService;
    private Platform platform;

    public FileConfiguration config = this.getConfig();
    File defaultConfigFile;
    File configFile;

    @Override
    public void onEnable() {
        Logger.enableDebug(DEBUG);
        Logger.setLogger(new SpigotLogger(this));

        MessageManager.init(this);

        initializePlatform();
        initializeServices();
        registerListeners();
        registerCommands();
        registerEvents();
        Logger.info("FriendNet enabled!");
    }

    @Override
    public void onDisable() {
        EventBus.clear();

        Logger.info("FriendNet disabled!");
    }

    private void initializePlatform() {
        this.platform = new SpigotPlatform(this);

        createConfigWithDefaults();

        LocaleManager = new SpigotLocaleManager(this);
        LocaleManager.loadLocales();
    }

    private void initializeServices() {
        this.databaseService = new DatabaseServiceImpl(this.getDataFolder(), "friendnet");
        this.playerService = new PlayerServiceImpl();
        this.friendService = new FriendServiceImpl(this.databaseService, this.playerService);

        this.databaseService.init();
        this.databaseService.postInit();
    }

    private void registerListeners() {
        new PlayerStatusListener(this, friendService, playerService, databaseService);
        new GUIListener(this);
    }

    private void registerCommands() {
        new FriendCommand(this);
    }

    private void registerEvents() {
        // TODO: Remove this as it is only for testing purposes
        EventBus.subscribe(PlayerJoinEvent.class, playerJoinEvent -> {
            Logger.info("Player joined event received: " + playerJoinEvent);
        });
    }

    public FriendService getFriendService() {
        return friendService;
    }

    public PlayerService getPlayerService() {
        return playerService;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public void createConfigWithDefaults() {
        config.options().copyDefaults(true);
        defaultConfigFile = new File(getDataFolder(), "config.yml");

        InputStreamReader defConfigStream;
        try {
            defConfigStream = new InputStreamReader(this.getResource("config.yml"), "UTF8");
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            config.setDefaults(defConfig);
            saveConfig();

            reloadConfig();
            config = this.getConfig();

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public boolean reloadPluginConfigs() {
        try {
            super.reloadConfig();
            config = getConfig();

            // load locales from disk
            LocaleManager.loadLocales();

            //TODO: Reload listeners
            //reloadListeners();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
