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
import com.trickshotmlg.friendnet.core.config.CommentedYamlConfigUpdater;
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
import com.trickshotmlg.friendnet.core_api.models.PlayerData;
import com.trickshotmlg.friendnet.core_api.proxy.FriendNetProxyProtocol;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public final class FriendNetPlugin extends JavaPlugin {
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
        if (networkRole.delegatesPersistentState() && FriendNetProxyProtocol.isUnsafeToken(getConnectionToken())) {
            throw new PluginStartupException("Proxy mode requires a non-default, non-blank ConnectionToken.");
        }
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
            backfillMissingPlayerNamesFromOfflineProfiles();
            this.applicationServices.knownPlayerLookup().loadKnownPlayers();
            this.friendGuiService = new StandaloneFriendGuiService(this);
        } else {
            this.proxyMessagingClient = new SpigotProxyMessagingClient(this);
            this.proxyMessagingClient.register();
            this.friendGuiService = new ProxyBackendFriendGuiService(this);
        }
        this.playerDataSaveQueue.start(getPlayerDataFlushIntervalTicks());
        Logger.info("FriendNet Spigot running as " + networkAuthorityService.getNetworkRole());
    }

    private void backfillMissingPlayerNamesFromOfflineProfiles() {
        int updated = 0;
        int unresolved = 0;

        for (PlayerData playerData : databaseService.findAllPlayerData()) {
            if (playerData.getLastPlayerName() != null && !playerData.getLastPlayerName().isBlank()) {
                continue;
            }

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerData.getPlayerId());
            String playerName = offlinePlayer.getName();
            if (playerName == null || playerName.isBlank()) {
                unresolved++;
                continue;
            }

            playerData.setLastPlayerName(playerName);
            databaseService.save(playerData);
            updated++;
            Logger.debug("Backfilled last_player_name from Spigot offline profile: playerId="
                    + playerData.getPlayerId() + ", playerName=" + playerName);
        }

        if (updated > 0) {
            Logger.info("Backfilled last_player_name for " + updated + " known players from Spigot offline profiles.");
        }
        if (unresolved > 0) {
            Logger.debug("Could not backfill last_player_name for " + unresolved + " known players from Spigot offline profiles.");
        }
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
        boolean useSsl = config.getBoolean("MySQL.useSSL", false);
        boolean allowPublicKeyRetrieval = config.getBoolean("MySQL.allowPublicKeyRetrieval", false);
        return new MySQLDatabase(host, dbName, username, password, databaseType, useSsl, allowPublicKeyRetrieval);
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
        defaultConfigFile = new File(getDataFolder(), "config.yml");

        updateConfigFileFromTemplate();

        reloadConfig();
        config = this.getConfig();
        applyDefaultConfigValues();
        applyDebugConfig();
    }

    private void updateConfigFileFromTemplate() {
        if (defaultConfigFile == null) {
            defaultConfigFile = new File(getDataFolder(), "config.yml");
        }

        try (InputStream configResource = this.getResource("config.yml")) {
            if (configResource == null) {
                throw new PluginStartupException("Default config.yml resource is missing.");
            }

            String template = new String(configResource.readAllBytes(), StandardCharsets.UTF_8);
            CommentedYamlConfigUpdater.UpdateResult result = CommentedYamlConfigUpdater.update(defaultConfigFile.toPath(), template);
            if (result == CommentedYamlConfigUpdater.UpdateResult.UPDATED) {
                Logger.info("Updated Spigot config.yml from bundled template and preserved existing values.");
            }
        } catch (IOException e) {
            throw new PluginStartupException("Could not update config.yml from bundled template", e);
        }
    }

    private void applyDefaultConfigValues() {
        try (InputStream configResource = this.getResource("config.yml")) {
            if (configResource == null) {
                throw new PluginStartupException("Default config.yml resource is missing.");
            }

            InputStreamReader defConfigStream = new InputStreamReader(configResource, StandardCharsets.UTF_8);
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            config.setDefaults(defConfig);
            config.options().copyDefaults(false);
        } catch (IOException e) {
            throw new PluginStartupException("Could not load default config.yml defaults", e);
        }
    }

    public boolean reloadPluginConfigs() {
        try {
            updateConfigFileFromTemplate();
            super.reloadConfig();
            config = getConfig();
            applyDefaultConfigValues();
            applyDebugConfig();

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

            Logger.info("FriendNet Spigot configs reloaded.");
            return true;
        } catch (Exception e) {
            Logger.error("Could not reload plugin configs", e);
            return false;
        }
    }

    public void disableForProxyAuthenticationFailure(String message, Throwable throwable) {
        Logger.error(message, throwable);
        getLogger().severe(message);
        getLogger().severe("FriendNet will be disabled. Check that ConnectionToken matches the Velocity adapter.");
        getServer().getPluginManager().disablePlugin(this);
    }

    private void applyDebugConfig() {
        Logger.enableDebug(config.getBoolean("Debug", false));
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
