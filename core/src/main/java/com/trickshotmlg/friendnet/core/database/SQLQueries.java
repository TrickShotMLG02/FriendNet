package com.trickshotmlg.friendnet.core.database;

public class SQLQueries {

    public static final String TABLE_PLAYERS_GET_PLAYER =
            "SELECT player_id, first_seen, last_seen FROM players WHERE player_id = ?";

    public static final String TABLE_PLAYERS_UPSERT_LASTSEEN =
            "INSERT INTO players (player_id, first_seen, last_seen) " +
                    "VALUES (?, ?, ?) " +
                    "ON CONFLICT (player_id) " +
                    "DO UPDATE SET last_seen = EXCLUDED.last_seen";

}
