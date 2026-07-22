package com.trickshotmlg.friendnet.core.database;

public class SQLTables {
    public static final String TABLE_CREATE_SCHEMA_MIGRATIONS =
            "CREATE TABLE IF NOT EXISTS friendnet_schema_migrations (" +
            "version INT PRIMARY KEY, " +
            "description VARCHAR(255) NOT NULL, " +
            "applied_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
            ");";

    public static final String TABLE_CREATE_PLAYERDATA =
            "CREATE TABLE IF NOT EXISTS players (" +
            "player_id VARCHAR(36) PRIMARY KEY, " +
            "last_player_name VARCHAR(64), " +
            "last_display_name VARCHAR(64), " +
            "first_seen TIMESTAMP NOT NULL, " +
            "last_seen TIMESTAMP, " +
            "last_server_name VARCHAR(64) DEFAULT NULL" +
            ");";

    public static final String TABLE_CREATE_LEGACY_PLAYERDATA =
            "CREATE TABLE IF NOT EXISTS players (" +
            "player_id VARCHAR(36) PRIMARY KEY, " +
            "last_display_name VARCHAR(64), " +
            "allow_friend_requests BOOLEAN DEFAULT TRUE, " +
            "show_online_status BOOLEAN DEFAULT TRUE, " +
            "auto_accept_friends BOOLEAN DEFAULT FALSE, " +
            "friend_request_notifications BOOLEAN DEFAULT TRUE, " +
            "friend_list_public BOOLEAN DEFAULT FALSE, " +
            "locale VARCHAR(10) NOT NULL DEFAULT 'EN', " +
            "first_seen TIMESTAMP NOT NULL, " +
            "last_seen TIMESTAMP" +
            ");";

    public static final String TABLE_CREATE_PLAYER_SETTINGS =
            "CREATE TABLE IF NOT EXISTS player_settings (" +
            "player_id VARCHAR(36) PRIMARY KEY, " +
            "allow_friend_requests BOOLEAN DEFAULT TRUE, " +
            "show_online_status BOOLEAN DEFAULT TRUE, " +
            "auto_accept_friends BOOLEAN DEFAULT FALSE, " +
            "friend_request_notifications BOOLEAN DEFAULT TRUE, " +
            "friend_list_public BOOLEAN DEFAULT FALSE, " +
            "locale VARCHAR(10) NOT NULL DEFAULT 'EN', " +
            "FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE" +
            ");";

    public static final String TABLE_CREATE_FRIENDSHIPS =
            "CREATE TABLE IF NOT EXISTS friendships (" +
            "player1_id VARCHAR(36) NOT NULL, " +
            "player2_id VARCHAR(36) NOT NULL, " +
            "requester_id VARCHAR(36) NOT NULL, " +
            "status VARCHAR(10) NOT NULL DEFAULT 'Pending' CHECK (status IN ('Pending', 'Accepted')), " +
            "request_sent_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            "friend_since TIMESTAMP DEFAULT NULL, " +
            "PRIMARY KEY (player1_id, player2_id), " +
            "FOREIGN KEY (player1_id) REFERENCES players(player_id), " +
            "FOREIGN KEY (player2_id) REFERENCES players(player_id), " +
            "FOREIGN KEY (requester_id) REFERENCES players(player_id)" +
            ");";

    public static final String TABLE_CREATE_LEGACY_FRIENDSHIPS =
            "CREATE TABLE IF NOT EXISTS friendships (" +
            "player1_id VARCHAR(36) NOT NULL, " +
            "player2_id VARCHAR(36) NOT NULL, " +
            "requester_id VARCHAR(36) NOT NULL, " +
            "status VARCHAR(10) NOT NULL DEFAULT 'Pending' CHECK (status IN ('Pending', 'Accepted')), " +
            "request_sent_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
            "friend_since TIMESTAMP DEFAULT NULL, " +
            "is_favourite BOOLEAN DEFAULT FALSE, " +
            "PRIMARY KEY (player1_id, player2_id), " +
            "FOREIGN KEY (player1_id) REFERENCES players(player_id), " +
            "FOREIGN KEY (player2_id) REFERENCES players(player_id), " +
            "FOREIGN KEY (requester_id) REFERENCES players(player_id)" +
            ");";

    public static final String TABLE_CREATE_FAVOURITES =
            "CREATE TABLE IF NOT EXISTS favourites (" +
                    "favouriter_id VARCHAR(36) NOT NULL, " +
                    "favourite_id VARCHAR(36) NOT NULL, " +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (favouriter_id, favourite_id), " +
                    "FOREIGN KEY (favouriter_id) REFERENCES players(player_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (favourite_id) REFERENCES players(player_id) ON DELETE CASCADE" +
                    ");";

    public static final String TABLE_CREATE_BLOCKLIST =
            "CREATE TABLE IF NOT EXISTS blocklist (" +
                    "blocker_id VARCHAR(36) NOT NULL, " +
                    "blocked_id VARCHAR(36) NOT NULL, " +
                    "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                    "PRIMARY KEY (blocker_id, blocked_id), " +
                    "FOREIGN KEY (blocker_id) REFERENCES players(player_id), " +
                    "FOREIGN KEY (blocked_id) REFERENCES players(player_id)" +
                    ");";

}
