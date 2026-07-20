package com.trickshotmlg.friendnet.adapter_spigot;

import com.trickshotmlg.friendnet.adapter_spigot.Commands.FriendCommand;
import com.trickshotmlg.friendnet.adapter_spigot.Configs.SpigotLocaleManager;
import com.trickshotmlg.friendnet.adapter_spigot.Listeners.GUIListener;
import com.trickshotmlg.friendnet.adapter_spigot.Services.FriendGuiService;
import com.trickshotmlg.friendnet.adapter_spigot.Services.PlayerDataSaveQueue;
import com.trickshotmlg.friendnet.adapter_spigot.Services.ProxyBackendFriendGuiService;
import com.trickshotmlg.friendnet.adapter_spigot.Services.ProxyBackendDatabaseService;
import com.trickshotmlg.friendnet.adapter_spigot.Services.SpigotProxyMessagingClient;
import com.trickshotmlg.friendnet.adapter_spigot.Services.StandaloneFriendGuiService;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.MessageManager;
import com.trickshotmlg.friendnet.adapter_spigot.Utils.SpigotLogger;
import com.trickshotmlg.friendnet.core.FriendServiceImpl;
import com.trickshotmlg.friendnet.adapter_spigot.Listeners.PlayerStatusListener;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core.NetworkAuthorityServiceImpl;
import com.trickshotmlg.friendnet.core.PlayerServiceImpl;
import com.trickshotmlg.friendnet.core.database.DatabaseServiceImpl;
import com.trickshotmlg.friendnet.core.database.MySQLDatabase;
import com.trickshotmlg.friendnet.core.database.SQLiteDatabase;
import com.trickshotmlg.friendnet.core.events.PlayerJoinEvent;
import com.trickshotmlg.friendnet.core.events.EventBus;
import com.trickshotmlg.friendnet.core_api.enums.DatabaseType;
import com.trickshotmlg.friendnet.core_api.enums.NetworkRole;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.Platform;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.NetworkAuthorityService;
import com.trickshotmlg.friendnet.core_api.proxy.FriendNetProxyProtocol;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

public final class FriendNetPlugin extends JavaPlugin {
    private static boolean DEBUG = true;

    public static SpigotLocaleManager LocaleManager;

    private FriendService friendService;
    private PlayerService playerService;
    private DatabaseService databaseService;
    private PlayerDataSaveQueue playerDataSaveQueue;
    private Platform platform;
    private SpigotApplicationServices applicationServices;
    private NetworkAuthorityService networkAuthorityService;
    private SpigotProxyMessagingClient proxyMessagingClient;
    private FriendGuiService friendGuiService;

    public FileConfiguration config = this.getConfig();
    File defaultConfigFile;
    File configFile;

    @Override
    public void onEnable() {
        Logger.enableDebug(DEBUG);
        Logger.setLogger(new SpigotLogger(this));

        try {
            MessageManager.init(this);

            initializePlatform();
            initializeServices();
            registerListeners();
            registerCommands();
            registerEvents();
            Logger.info("FriendNet enabled!");
        } catch (PluginStartupException e) {
            disableAfterStartupFailure(e.getMessage(), e);
        } catch (RuntimeException e) {
            disableAfterStartupFailure("Unexpected startup error: " + e.getMessage(), e);
        }
    }

    @Override
    public void onDisable() {
        EventBus.clear();
        if (playerDataSaveQueue != null) {
            playerDataSaveQueue.stopAndFlush();
        }
        if (databaseService != null) {
            databaseService.stop();
        }
        if (proxyMessagingClient != null) {
            proxyMessagingClient.unregister();
        }

        Logger.info("FriendNet disabled!");
    }

    private void initializePlatform() {
        this.platform = new SpigotPlatform(this);

        createConfigWithDefaults();

        LocaleManager = new SpigotLocaleManager(this);
        LocaleManager.loadLocales();
    }

    private void initializeServices() {
        NetworkRole networkRole = parseSpigotNetworkRole(config.getString("Mode", "Standalone"));
        this.networkAuthorityService = new NetworkAuthorityServiceImpl(networkRole);
        this.databaseService = networkAuthorityService.ownsPersistentState()
                ? new DatabaseServiceImpl(createDatabaseFromConfig())
                : new ProxyBackendDatabaseService();
        this.playerService = new PlayerServiceImpl();
        this.friendService = new FriendServiceImpl(this.databaseService, this.playerService);
        this.playerDataSaveQueue = new PlayerDataSaveQueue(this, playerService, databaseService, networkAuthorityService.ownsPersistentState());
        this.applicationServices = new SpigotApplicationServices(this);

        if (networkAuthorityService.ownsPersistentState()) {
            this.databaseService.init();
            this.databaseService.postInit();
            this.databaseService.start();
            this.friendGuiService = new StandaloneFriendGuiService(this);
        } else {
            this.proxyMessagingClient = new SpigotProxyMessagingClient(this);
            this.proxyMessagingClient.register();
            this.friendGuiService = new ProxyBackendFriendGuiService(this);
        }
        this.playerDataSaveQueue.start(getPlayerDataFlushIntervalTicks());
        Logger.info("FriendNet Spigot running as " + networkAuthorityService.getNetworkRole());
        warnIfProxyTokenUnsafe();
    }

    private Database createDatabaseFromConfig() {
        DatabaseType databaseType = parseDatabaseType(config.getString("DatabaseType", "SQLite"));

        if (databaseType == DatabaseType.SQLite) {
            String dbName = config.getString("SQLite.dbName", "friendnet");
            return new SQLiteDatabase(this.getDataFolder(), dbName);
        }

        String host = config.getString("MySQL.host", "localhost:3306");
        String dbName = config.getString("MySQL.dbName", "friendnet");
        String username = config.getString("MySQL.username", "friendnet");
        String password = config.getString("MySQL.password", "friendnet");
        return new MySQLDatabase(host, dbName, username, password, databaseType);
    }

    private DatabaseType parseDatabaseType(String value) {
        if (value == null || value.isBlank()) {
            throw new PluginStartupException("DatabaseType is missing in config.yml. Supported values: SQLite, MySQL, MariaDB.");
        }

        String normalized = value.trim().replace("-", "").replace("_", "").toLowerCase(Locale.ROOT);
        switch (normalized) {
            case "sqlite":
                return DatabaseType.SQLite;
            case "mysql":
                return DatabaseType.MySQL;
            case "mariadb":
                return DatabaseType.MariaDB;
            default:
                throw new PluginStartupException("Unknown DatabaseType '" + value + "' in config.yml. Supported values: SQLite, MySQL, MariaDB.");
        }
    }

    private void disableAfterStartupFailure(String message, Throwable throwable) {
        Logger.error("FriendNet startup failed: " + message, throwable);
        getLogger().severe("FriendNet startup failed. The plugin will be disabled.");
        getLogger().severe(message);
        getLogger().severe("Check config.yml, especially DatabaseType and the matching database connection settings.");
        getServer().getPluginManager().disablePlugin(this);
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

    public PlayerDataSaveQueue getPlayerDataSaveQueue() {
        return playerDataSaveQueue;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public Platform getPlatform() {
        return platform;
    }

    public SpigotApplicationServices getApplicationServices() {
        return applicationServices;
    }

    public NetworkAuthorityService getNetworkAuthorityService() {
        return networkAuthorityService;
    }

    public SpigotProxyMessagingClient getProxyMessagingClient() {
        return proxyMessagingClient;
    }

    public FriendGuiService getFriendGuiService() {
        return friendGuiService;
    }

    public boolean isProxyBackendMode() {
        return networkAuthorityService != null && networkAuthorityService.delegatesPersistentState();
    }

    public String getConnectionToken() {
        return config.getString("ConnectionToken", FriendNetProxyProtocol.DEFAULT_CONNECTION_TOKEN);
    }

    private void warnIfProxyTokenUnsafe() {
        if (isProxyBackendMode() && FriendNetProxyProtocol.isUnsafeToken(getConnectionToken())) {
            Logger.warn("FriendNet proxy mode is using the default or blank ConnectionToken. Set a shared secret in Spigot and Velocity configs.");
        }
    }

    private NetworkRole parseSpigotNetworkRole(String value) {
        if (value == null || value.isBlank()) {
            return NetworkRole.STANDALONE;
        }

        String normalized = value.trim().replace("-", "").replace("_", "").toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "standalone" -> NetworkRole.STANDALONE;
            case "proxy", "backend", "proxybackend" -> NetworkRole.PROXY_BACKEND;
            default -> throw new PluginStartupException("Unknown Mode '" + value + "' in config.yml. Supported values: Standalone, Proxy.");
        };
    }

    private long getPlayerDataFlushIntervalTicks() {
        int seconds = config.getInt("PlayerDataFlushIntervalSeconds", 30);
        return Math.max(5, seconds) * 20L;
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

            if (config.getStringList("SupportedLocales").isEmpty()) {
                Logger.error("Could not reload configs: SupportedLocales must contain at least one locale.", null);
                return false;
            }

            if (config.getString("DefaultLocale", "").isBlank()) {
                Logger.error("Could not reload configs: DefaultLocale must be set.", null);
                return false;
            }

            LocaleManager.loadLocales();
            MessageManager.loadMessages();

            return true;
        } catch (Exception e) {
            Logger.error("Could not reload plugin configs", e);
            return false;
        }
    }

    private static final class PluginStartupException extends RuntimeException {
        private PluginStartupException(String message) {
            super(message);
        }

        private PluginStartupException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
