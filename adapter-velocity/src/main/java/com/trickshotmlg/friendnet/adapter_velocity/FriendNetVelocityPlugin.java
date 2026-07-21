package com.trickshotmlg.friendnet.adapter_velocity;

import com.google.inject.Inject;
import com.trickshotmlg.friendnet.adapter_velocity.commands.FriendNetVelocityCommand;
import com.trickshotmlg.friendnet.adapter_velocity.commands.VelocityFriendCommand;
import com.trickshotmlg.friendnet.adapter_velocity.config.VelocityConfig;
import com.trickshotmlg.friendnet.adapter_velocity.listeners.VelocityPlayerStatusListener;
import com.trickshotmlg.friendnet.adapter_velocity.services.VelocityPlayerDataSaveQueue;
import com.trickshotmlg.friendnet.adapter_velocity.services.VelocityProxyMessagingService;
import com.trickshotmlg.friendnet.adapter_velocity.utils.VelocityLogger;
import com.trickshotmlg.friendnet.adapter_velocity.utils.VelocityMessageManager;
import com.trickshotmlg.friendnet.core.FriendServiceImpl;
import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core.NetworkAuthorityServiceImpl;
import com.trickshotmlg.friendnet.core.PlayerServiceImpl;
import com.trickshotmlg.friendnet.core.database.DatabaseServiceImpl;
import com.trickshotmlg.friendnet.core.database.MySQLDatabase;
import com.trickshotmlg.friendnet.core.database.SQLiteDatabase;
import com.trickshotmlg.friendnet.core.events.EventBus;
import com.trickshotmlg.friendnet.core_api.enums.DatabaseType;
import com.trickshotmlg.friendnet.core_api.enums.NetworkRole;
import com.trickshotmlg.friendnet.core_api.interfaces.Platform;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.NetworkAuthorityService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.proxy.FriendNetProxyProtocol;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Locale;

@Plugin(
        id = "friendnet",
        name = "FriendNet",
        version = "1.0-SNAPSHOT",
        description = "FriendNet proxy authority adapter for Velocity",
        authors = {"TrickshotMLG"}
)
public final class FriendNetVelocityPlugin {

    private final ProxyServer server;
    private final org.slf4j.Logger velocityLogger;
    private final Path dataDirectory;

    private VelocityConfig config;
    private VelocityMessageManager messageManager;
    private FriendService friendService;
    private PlayerService playerService;
    private DatabaseService databaseService;
    private NetworkAuthorityService networkAuthorityService;
    private VelocityPlayerDataSaveQueue playerDataSaveQueue;
    private Platform platform;
    private VelocityApplicationServices applicationServices;
    private VelocityProxyMessagingService proxyMessagingService;
    private CommandMeta friendNetCommandMeta;
    private CommandMeta friendCommandMeta;
    private boolean enabled;
    private boolean disabledDueToStartupFailure;

    @Inject
    public FriendNetVelocityPlugin(ProxyServer server, org.slf4j.Logger logger, @DataDirectory Path dataDirectory) {
        this.server = server;
        this.velocityLogger = logger != null ? logger : LoggerFactory.getLogger("FriendNet");
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Logger.setLogger(new VelocityLogger(velocityLogger));

        try {
            initializePlatform();
            initializeServices();
            registerListeners();
            registerCommands();
            enabled = true;
            Logger.info("FriendNet Velocity adapter enabled!");
        } catch (PluginStartupException e) {
            disableAfterStartupFailure(e.getMessage(), e);
        } catch (RuntimeException e) {
            disableAfterStartupFailure("Unexpected startup error: " + e.getMessage(), e);
        }
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        shutdownServices();
        Logger.info("FriendNet Velocity adapter disabled!");
    }

    private void shutdownServices() {
        EventBus.clear();
        if (playerDataSaveQueue != null) {
            playerDataSaveQueue.stopAndFlush();
            playerDataSaveQueue = null;
        }
        if (databaseService != null) {
            databaseService.stop();
            databaseService = null;
        }
        if (proxyMessagingService != null) {
            proxyMessagingService.unregister();
            proxyMessagingService = null;
        }
    }

    private void initializePlatform() {
        this.config = VelocityConfig.load(dataDirectory);
        Logger.enableDebug(config.getBoolean("Debug", true));
        this.messageManager = new VelocityMessageManager(this);
        this.messageManager.loadMessages();
        this.platform = new VelocityPlatform(server, this);
    }

    private void initializeServices() {
        this.databaseService = new DatabaseServiceImpl(createDatabaseFromConfig());
        this.playerService = new PlayerServiceImpl();
        this.friendService = new FriendServiceImpl(databaseService, playerService);
        this.networkAuthorityService = new NetworkAuthorityServiceImpl(NetworkRole.PROXY_AUTHORITY);
        this.playerDataSaveQueue = new VelocityPlayerDataSaveQueue(this, playerService, databaseService);
        this.applicationServices = new VelocityApplicationServices(this);
        this.proxyMessagingService = new VelocityProxyMessagingService(this);

        this.databaseService.init();
        this.databaseService.postInit();
        this.databaseService.start();
        this.playerDataSaveQueue.start(getPlayerDataFlushIntervalSeconds());
        this.proxyMessagingService.register();
        warnIfConnectionTokenUnsafe();
    }

    private void registerListeners() {
        server.getEventManager().register(this, new VelocityPlayerStatusListener(this, friendService, playerService, databaseService, networkAuthorityService));
    }

    private void registerCommands() {
        CommandManager commandManager = server.getCommandManager();
        friendNetCommandMeta = commandManager.metaBuilder("friendnet")
                .aliases("fn")
                .plugin(this)
                .build();
        commandManager.register(friendNetCommandMeta, new FriendNetVelocityCommand(this));

        friendCommandMeta = commandManager.metaBuilder("friend")
                .aliases("friends")
                .plugin(this)
                .build();
        commandManager.register(friendCommandMeta, new VelocityFriendCommand(this));
    }

    private void disableAfterStartupFailure(String message, Throwable throwable) {
        disabledDueToStartupFailure = true;
        enabled = false;

        Logger.error("FriendNet Velocity startup failed: " + message, throwable);
        velocityLogger.error("FriendNet Velocity startup failed. FriendNet will be disabled.");
        velocityLogger.error(message);
        velocityLogger.error("Check config.yml, especially DatabaseType and the matching database connection settings.");

        unregisterVelocityEntrypoints();
        shutdownServices();

        Logger.info("FriendNet Velocity adapter disabled due to startup failure.");
    }

    private void unregisterVelocityEntrypoints() {
        CommandManager commandManager = server.getCommandManager();
        if (friendNetCommandMeta != null) {
            commandManager.unregister(friendNetCommandMeta);
            friendNetCommandMeta = null;
        }
        if (friendCommandMeta != null) {
            commandManager.unregister(friendCommandMeta);
            friendCommandMeta = null;
        }

        server.getEventManager().unregisterListeners(this);
    }

    private Database createDatabaseFromConfig() {
        DatabaseType databaseType = parseDatabaseType(config.getString("DatabaseType", "SQLite"));

        if (databaseType == DatabaseType.SQLite) {
            String dbName = config.getString("SQLite.dbName", "friendnet");
            return new SQLiteDatabase(dataDirectory.toFile(), dbName);
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
        return switch (normalized) {
            case "sqlite" -> DatabaseType.SQLite;
            case "mysql" -> DatabaseType.MySQL;
            case "mariadb" -> DatabaseType.MariaDB;
            default -> throw new PluginStartupException("Unknown DatabaseType '" + value + "' in config.yml. Supported values: SQLite, MySQL, MariaDB.");
        };
    }

    public boolean reloadPluginConfigs() {
        try {
            this.config = VelocityConfig.load(dataDirectory);
            Logger.enableDebug(config.getBoolean("Debug", true));
            messageManager.loadMessages();
            return true;
        } catch (RuntimeException e) {
            Logger.error("Could not reload Velocity config", e);
            return false;
        }
    }

    private long getPlayerDataFlushIntervalSeconds() {
        return Math.max(5, config.getInt("PlayerDataFlushIntervalSeconds", 30));
    }

    public ProxyServer getServer() {
        return server;
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

    public NetworkAuthorityService getNetworkAuthorityService() {
        return networkAuthorityService;
    }

    public VelocityPlayerDataSaveQueue getPlayerDataSaveQueue() {
        return playerDataSaveQueue;
    }

    public Platform getPlatform() {
        return platform;
    }

    public VelocityApplicationServices getApplicationServices() {
        return applicationServices;
    }

    public VelocityProxyMessagingService getProxyMessagingService() {
        return proxyMessagingService;
    }

    public VelocityConfig getConfig() {
        return config;
    }

    public String getConnectionToken() {
        return config.getString("ConnectionToken", FriendNetProxyProtocol.DEFAULT_CONNECTION_TOKEN);
    }

    public VelocityMessageManager getMessageManager() {
        return messageManager;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isDisabledDueToStartupFailure() {
        return disabledDueToStartupFailure;
    }

    private void warnIfConnectionTokenUnsafe() {
        if (FriendNetProxyProtocol.isUnsafeToken(getConnectionToken())) {
            Logger.warn("FriendNet Velocity is using the default or blank ConnectionToken. Set a shared secret in Spigot and Velocity configs.");
        }
    }

    private static final class PluginStartupException extends RuntimeException {
        private PluginStartupException(String message) {
            super(message);
        }
    }
}
