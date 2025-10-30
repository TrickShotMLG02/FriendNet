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
    /*
    https://github.com/TrickShotMLG02/MinecraftPluginDevelopment/blob/ModularLobby/ModularLobby/src/main/java/com/trickshotdev/modularlobby/Spigot/ModularLobby.java
    Check this for more about configs, message files and other stuff
    */

    private static boolean DEBUG = true;

    public static SpigotLocaleManager LocaleManager;

    private FriendService friendService;
    private PlayerService playerService;
    private DatabaseService databaseService;
    private Platform platform;

    public FileConfiguration config = this.getConfig();
    public FileConfiguration messages;
    File defaultConfigFile;
    File defaultMessagesFile;
    File configFile;
    File messagesFile;

    @Override
    public void onEnable() {
        Logger.enableDebug(DEBUG);
        Logger.setLogger(new SpigotLogger(this));

        MessageManager.init(this);

        initializePlatform();
        initializeServices();
        registerListeners();
        registerCommands();
        Logger.info("FriendNet enabled!");
    }

    private void initializePlatform() {
        this.platform = new SpigotPlatform(this);

        createConfigWithDefaults();
        createMessagesFileWithDefaults();

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

            messagesFile = new File(getDataFolder(), "messages.yml");
            messages = YamlConfiguration.loadConfiguration(messagesFile);

            //TODO: Reload listeners
            //reloadListeners();

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void createMessagesFileWithDefaults() {
        //Create file
        defaultMessagesFile = new File(getDataFolder(), "messages.yml");
        if (!defaultMessagesFile.exists()) {
            defaultMessagesFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }

        messages = new YamlConfiguration();
        try {
            messages.load(defaultMessagesFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        //Fix config
        messages.options().copyDefaults(true);
        messagesFile = new File(getDataFolder(), "messages.yml");

        InputStreamReader defMessagesStream;
        try {
            defMessagesStream = new InputStreamReader(this.getResource("messages.yml"), "UTF8");
            YamlConfiguration defMessages = YamlConfiguration.loadConfiguration(defMessagesStream);
            messages.setDefaults(defMessages);
            messages.save(messagesFile);

            messagesFile = new File(getDataFolder(), "messages.yml");
            messages = YamlConfiguration.loadConfiguration(messagesFile);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
