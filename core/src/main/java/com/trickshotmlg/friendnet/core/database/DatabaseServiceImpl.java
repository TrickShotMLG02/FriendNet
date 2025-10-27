package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.enums.ServiceState;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseConnection;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.models.FriendData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;

public class DatabaseServiceImpl implements DatabaseService {

    private File dataFolder;
    private String dbName;

    private Database database;

    public DatabaseServiceImpl(File dataFolder, String dbName) {
        this.dataFolder = dataFolder;
        this.dbName = dbName;
    }

    /**
     * @return
     */
    @Override
    public Database getDatabase() {
        return database;
    }

    /**
     * @param playerId
     * @param clazz
     * @param <T>
     * @return
     */
    @Override
    public <T> Optional<T> find(UUID playerId, Class<T> clazz) {
        if (clazz.equals(FriendData.class)) {
            return Optional.empty();
        }

        if (clazz.equals(PlayerData.class)) {
            try {
                DatabaseConnection conn = getDatabase().getConnection();

                try (PreparedStatement ps = conn.prepareStatement(SQLQueries.TABLE_PLAYERS_SELECT)) {
                    ps.setObject(1, playerId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            // Retrieve timestamps
                            Timestamp firstSeen = rs.getTimestamp("first_seen");
                            Timestamp lastSeen = rs.getTimestamp("last_seen");

                            // Create PlayerData instance
                            PlayerData playerData = new PlayerData(playerId, firstSeen, lastSeen);

                            // Load boolean settings
                            playerData.setAllowFriendRequests(rs.getBoolean("allow_friend_requests"));
                            playerData.setShowOnlineStatus(rs.getBoolean("show_online_status"));
                            playerData.setAutoAcceptFriends(rs.getBoolean("auto_accept_friends"));
                            playerData.setFriendRequestNotifications(rs.getBoolean("friend_request_notifications"));
                            playerData.setFriendListPublic(rs.getBoolean("friend_list_public"));

                            // Cast to T to satisfy the generic method signature
                            return Optional.of(clazz.cast(playerData));
                        }
                    }
                } catch (SQLException e) {
                    Logger.error("Failed to fetch player: " + playerId, e);
                }
            } catch (SQLException e) {
                Logger.error("Could not establish database connection", e);
            }

            return Optional.empty();
        }

        return Optional.empty();
    }

    /**
     * @param entity
     */
    @Override
    public void save(FriendData entity) {

    }

    /**
     * @param entity
     */
    @Override
    public void save(PlayerData entity) {
        try {
            DatabaseConnection conn = getDatabase().getConnection();

            try (PreparedStatement ps = conn.prepareStatement(SQLQueries.TABLE_PLAYERS_UPSERT)){
                ps.setObject(1, entity.getPlayerId());
                ps.setBoolean(2, entity.isAllowFriendRequests());
                ps.setBoolean(3, entity.isShowOnlineStatus());
                ps.setBoolean(4, entity.isAutoAcceptFriends());
                ps.setBoolean(5, entity.isFriendRequestNotifications());
                ps.setBoolean(6, entity.isFriendListPublic());
                ps.setTimestamp(7, entity.getFirstSeen());
                ps.setTimestamp(8, entity.getLastSeen());

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            Logger.error("Could not save PlayerData for player " + entity.getPlayerId(), e);
        }
    }

    /**
     * @param entity
     */
    @Override
    public void delete(FriendData entity) {

    }

    /**
     * @param entity
     */
    @Override
    public void delete(PlayerData entity) {

    }

    /**
     *
     */
    @Override
    public void init() {
        database = new SQLiteDatabase(dataFolder, dbName);
    }

    /**
     *
     */
    @Override
    public void postInit() {
        try {
            DatabaseConnection conn = database.getConnection();

            // create default tables
            PreparedStatement ps = conn.prepareStatement(SQLTables.TABLE_CREATE_PLAYERDATA);
            ps.execute();
            ps.close();

            ps = conn.prepareStatement(SQLTables.TABLE_CREATE_FRIENDSHIPS);
            ps.execute();
            ps.close();

        } catch (SQLException e) {
            Logger.error("Error while creating initial database tables!", e);
        }
    }

    /**
     *
     */
    @Override
    public void start() {

    }

    /**
     *
     */
    @Override
    public void stop() {

    }

    /**
     *
     */
    @Override
    public void destroy() {

    }

    /**
     * @return
     */
    @Override
    public ServiceState getState() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public boolean isRunning() {
        return false;
    }
}
