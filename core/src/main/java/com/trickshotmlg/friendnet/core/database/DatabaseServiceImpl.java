package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.enums.FriendshipStatus;
import com.trickshotmlg.friendnet.core_api.enums.ServiceState;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseConnection;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.models.FriendshipData;
import com.trickshotmlg.friendnet.core_api.models.LocaleKey;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
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

        if (clazz.equals(FriendshipData.class)) {

            try {
                DatabaseConnection conn = getDatabase().getConnection();

                try (PreparedStatement ps = conn.prepareStatement(SQLQueries.TABLE_FRIENDSHIPS_SELECT)) {
                    ps.setObject(1, playerId);
                    ps.setObject(2, playerId);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {

                            UUID player1Id = UUID.fromString(rs.getString("player1_id"));
                            UUID player2Id = UUID.fromString(rs.getString("player2_id"));
                            UUID requesterId = UUID.fromString(rs.getString("requester_id"));
                            FriendshipStatus friendshipType = FriendshipStatus.valueOf(rs.getString("status"));
                            Timestamp requestSentTime = rs.getTimestamp("request_sent_time");
                            Timestamp friendSince = rs.getTimestamp("friend_since");
                            boolean favourite = rs.getBoolean("is_favourite");

                            // Create FriendshipData instance
                            FriendshipData friendshipData = new FriendshipData(
                                    requesterId,
                                    requesterId == player1Id ? player2Id : player1Id,
                                    friendshipType,
                                    friendSince,
                                    requestSentTime,
                                    favourite
                            );

                            // Cast to T to satisfy the generic method signature
                            return Optional.of(clazz.cast(friendshipData));
                        }
                    }
                } catch (SQLException e) {
                    Logger.error("Failed to fetch friendship data for player: " + playerId, e);
                }
            } catch (SQLException e) {
                Logger.error("Could not establish database connection", e);
            }
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
                            playerData.setLastDisplayName(rs.getString("last_display_name"));

                            // Load boolean settings
                            playerData.setAllowFriendRequests(rs.getBoolean("allow_friend_requests"));
                            playerData.setShowOnlineStatus(rs.getBoolean("show_online_status"));
                            playerData.setAutoAcceptFriends(rs.getBoolean("auto_accept_friends"));
                            playerData.setFriendRequestNotifications(rs.getBoolean("friend_request_notifications"));
                            playerData.setFriendListPublic(rs.getBoolean("friend_list_public"));
                            playerData.setLocale(LocaleKey.getOrFallback(rs.getString("locale")));

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
     * @param playerId
     * @param clazz
     * @param <T>
     * @return
     */
    @Override
    public <T> Optional<Set<T>> findAll(UUID playerId, Class<T> clazz) {
        if (clazz.equals(FriendshipData.class)) {

            Set<FriendshipData> friendships = new HashSet<>();

            try {
                DatabaseConnection conn = getDatabase().getConnection();

                try (PreparedStatement ps = conn.prepareStatement(SQLQueries.TABLE_FRIENDSHIPS_SELECT)) {
                    ps.setObject(1, playerId);
                    ps.setObject(2, playerId);

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {

                            UUID player1Id = UUID.fromString(rs.getString("player1_id"));
                            UUID player2Id = UUID.fromString(rs.getString("player2_id"));
                            UUID requesterId = UUID.fromString(rs.getString("requester_id"));
                            FriendshipStatus friendshipType = FriendshipStatus.valueOf(rs.getString("status"));
                            Timestamp requestSentTime = rs.getTimestamp("request_sent_time");
                            Timestamp friendSince = rs.getTimestamp("friend_since");
                            boolean favourite = rs.getBoolean("is_favourite");

                            // Create FriendshipData instance
                            FriendshipData friendshipData = new FriendshipData(
                                    requesterId,
                                    requesterId.equals(player1Id) ? player2Id : player1Id,
                                    friendshipType,
                                    requestSentTime,
                                    friendSince,
                                    favourite
                            );

                            friendships.add((FriendshipData) clazz.cast(friendshipData));
                        }

                        // Cast to T to satisfy the generic method signature
                        return Optional.of((Set<T>) friendships);
                    }
                } catch (SQLException e) {
                    Logger.error("Failed to fetch friendship data for player: " + playerId, e);
                }
            } catch (SQLException e) {
                Logger.error("Could not establish database connection", e);
            }
        }

        return Optional.empty();
    }


    /**
     * @param entity
     */
    @Override
    public void save(FriendshipData entity) {
        try {
            DatabaseConnection conn = getDatabase().getConnection();

            try (PreparedStatement ps = conn.prepareStatement(SQLQueries.TABLE_FRIENDSHIPS_UPSERT)){
                ps.setObject(1, entity.getPlayer1Id());
                ps.setObject(2, entity.getPlayer2Id());
                ps.setObject(3, entity.getRequesterId());
                ps.setObject(4, entity.getFriendshipStatus());
                ps.setTimestamp(5, entity.getRequestSentTime());
                ps.setTimestamp(6, entity.getFriendSince());
                ps.setBoolean(7, entity.isFavourite());

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            Logger.error("Could not save FriendshipData: " + entity, e);
        }
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
                ps.setString(2, entity.getLastDisplayName());
                ps.setBoolean(3, entity.isAllowFriendRequests());
                ps.setBoolean(4, entity.isShowOnlineStatus());
                ps.setBoolean(5, entity.isAutoAcceptFriends());
                ps.setBoolean(6, entity.isFriendRequestNotifications());
                ps.setBoolean(7, entity.isFriendListPublic());
                ps.setString(8, entity.getLocale().getCode());
                ps.setTimestamp(9, entity.getFirstSeen());
                ps.setTimestamp(10, entity.getLastSeen());

                ps.executeUpdate();
            }

        } catch (SQLException e) {
            Logger.error("Could not save PlayerData: " + entity, e);
        }
    }

    /**
     * @param entity
     */
    @Override
    public void delete(FriendshipData entity) {
        try {
            DatabaseConnection conn = getDatabase().getConnection();

            try (PreparedStatement ps = conn.prepareStatement(SQLQueries.TABLE_FRIENDSHIPS_DELETE)){
                ps.setObject(1, entity.getPlayer1Id());
                ps.setObject(2, entity.getPlayer2Id());
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            Logger.error("Could not delete FriendshipData: " + entity, e);
        }
    }

    /**
     * @param entity
     */
    @Override
    public void delete(PlayerData entity) {
        try {
            DatabaseConnection conn = getDatabase().getConnection();

            try (PreparedStatement ps = conn.prepareStatement(SQLQueries.TABLE_PLAYERS_DELETE)){
                ps.setObject(1, entity.getPlayerId());
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            Logger.error("Could not delete PlayerData: " + entity, e);
        }
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

            ps = conn.prepareStatement(SQLTables.TABLE_CREATE_BLOCKLIST);
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
