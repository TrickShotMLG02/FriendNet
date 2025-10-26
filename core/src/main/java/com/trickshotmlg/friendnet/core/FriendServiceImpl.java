package com.trickshotmlg.friendnet.core;

import com.trickshotmlg.friendnet.core.database.SQLQueries;
import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseConnection;
import com.trickshotmlg.friendnet.core_api.interfaces.services.DatabaseService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.FriendService;
import com.trickshotmlg.friendnet.core_api.interfaces.services.PlayerService;
import com.trickshotmlg.friendnet.core_api.models.FriendData;
import com.trickshotmlg.friendnet.core_api.models.PlayerData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class FriendServiceImpl implements FriendService {

    private final DatabaseService databaseService;
    private final PlayerService playerService;

    public FriendServiceImpl(DatabaseService databaseService, PlayerService playerService) {
        this.databaseService = databaseService;
        this.playerService = playerService;
    }

    /**
     * Stores the friend data for all players.
     * <p>
     * The key is the player's {@link UUID}, and the value is the corresponding
     * {@link FriendData} object containing their friends list and online status.
     * <p>
     * Uses a {@link java.util.concurrent.ConcurrentHashMap} to ensure thread-safe
     * access, allowing multiple threads (e.g., event handlers, network updates)
     * to safely read and modify friend data concurrently.
     * </p>
     */
    private final Map<UUID, FriendData> friends = new ConcurrentHashMap<>();


    /**
     * Retrieves the {@link FriendData} object for the given player.
     * <p>
     * If no {@link FriendData} exists for the player, a new instance is
     * automatically created, added to the internal storage map, and returned.
     * This ensures that every player UUID queried has a corresponding
     * {@link FriendData} object.
     * </p>
     *
     * @param player the UUID of the player whose {@link FriendData} is requested
     * @return the existing or newly created {@link FriendData} associated with the player
     */
    private FriendData getOrCreate(UUID player) {
        return friends.computeIfAbsent(player, FriendData::new);
    }

    @Override
    public void addFriend(UUID player, UUID target) {
        getOrCreate(player).addFriend(target);
        getOrCreate(target).addFriend(player);
    }

    @Override
    public void removeFriend(UUID player, UUID target) {
        getOrCreate(player).removeFriend(target);
        getOrCreate(target).removeFriend(player);
    }

    @Override
    public boolean areFriends(UUID player, UUID target) {
        return getOrCreate(player).getFriends().contains(target);
    }

    @Override
    public Set<UUID> getFriends(UUID player) {
        return getOrCreate(player).getFriends();
    }

    @Override
    public void setOnline(UUID player, boolean online) {
        getOrCreate(player).setOnline(online);

        if (online) {
            try {
                DatabaseConnection conn = this.databaseService.getDatabase().getConnection();

                try (PreparedStatement ps = conn.prepareStatement(SQLQueries.TABLE_PLAYERS_GET_PLAYER)) {
                    ps.setObject(1, player);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            UUID id = UUID.fromString(rs.getString("player_id"));
                            Timestamp firstSeen = rs.getTimestamp("first_seen");
                            Timestamp lastSeen = rs.getTimestamp("last_seen");
                            PlayerData playerData = new PlayerData(id, firstSeen, lastSeen);
                            this.playerService.putPlayerData(playerData);
                        } else {
                            PlayerData playerData = playerService.initPlayer(player);
                        }
                    }
                }
            } catch (SQLException ex) {
                System.err.println("SQLException: " + ex.getMessage());
            }
        }
        else {
            playerService.setLastSeen(player);

            try {
                DatabaseConnection conn = this.databaseService.getDatabase().getConnection();

                try (PreparedStatement ps = conn.prepareStatement(SQLQueries.TABLE_PLAYERS_UPSERT_LASTSEEN)) {

                    PlayerData playerData = playerService.getPlayerData(player);

                    ps.setObject(1, playerData.getPlayerId());
                    ps.setTimestamp(2, playerData.getFirstSeen());
                    ps.setTimestamp(3, playerData.getLastSeen());

                    ps.executeUpdate();
                }
            } catch (SQLException ex) {
                System.err.println("SQLException: " + ex.getMessage());
            }
        }
    }

    @Override
    public boolean isOnline(UUID player) {
        return getOrCreate(player).isOnline();
    }
}
