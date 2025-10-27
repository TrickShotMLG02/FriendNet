package com.trickshotmlg.friendnet.core.database;

public class SQLTables {

    public static final String TABLE_CREATE_PLAYERDATA =
            "CREATE TABLE IF NOT EXISTS players (" +
            "player_id UUID PRIMARY KEY, " +
            "allow_friend_requests BOOLEAN DEFAULT TRUE, " +
            "show_online_status BOOLEAN DEFAULT TRUE, " +
            "auto_accept_friends BOOLEAN DEFAULT FALSE, " +
            "friend_request_notifications BOOLEAN DEFAULT TRUE, " +
            "friend_list_public BOOLEAN DEFAULT TRUE, " +
            "first_seen TIMESTAMP NOT NULL, " +
            "last_seen TIMESTAMP" +
            ");";

    public static final String TABLE_CREATE_FRIENDSHIPS =
            "CREATE TABLE IF NOT EXISTS friendships (" +
            "player_id UUID NOT NULL, " +                  // first player
            "friend_id UUID NOT NULL, " +                // second player
            "friend_since TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +  // when friendship started
            "status VARCHAR(10) NOT NULL DEFAULT 'pending' CHECK (status IN ('pending', 'accepted', 'blocked')), " +
            "is_favourite BOOLEAN DEFAULT FALSE, " +    // example setting
            "PRIMARY KEY (player_id, friend_id), " +
            "FOREIGN KEY (player_id) REFERENCES players(player_id), " +
            "FOREIGN KEY (friend_id) REFERENCES players(player_id)" +
            ");";
}
