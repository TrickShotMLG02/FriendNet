package com.trickshotmlg.friendnet.core.database;

public class SQLTables {

    public static final String TABLE_CREATE_PLAYERDATA =
            "CREATE TABLE IF NOT EXISTS players (" +
            "player_id UUID PRIMARY KEY, " +
            "first_seen TIMESTAMP NOT NULL, " +
            "last_seen TIMESTAMP" +
            ");";

    public static final String TABLE_CREATE_FRIENDSHIPS =
            "CREATE TABLE IF NOT EXISTS friendships (" +
            "user_id UUID NOT NULL, " +                  // first player
            "friend_id UUID NOT NULL, " +                // second player
            "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +  // when friendship started
            "is_favourite BOOLEAN DEFAULT FALSE, " +    // example setting
            "PRIMARY KEY (user_id, friend_id), " +
            "FOREIGN KEY (user_id) REFERENCES players(player_id), " +
            "FOREIGN KEY (friend_id) REFERENCES players(player_id)" +
            ");";
}
