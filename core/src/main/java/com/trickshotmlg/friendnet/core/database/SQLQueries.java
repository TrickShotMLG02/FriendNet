package com.trickshotmlg.friendnet.core.database;

public class SQLQueries {

    public static final String TABLE_PLAYERS_GET_PLAYER =
            "SELECT player_id, first_seen, last_seen FROM players WHERE player_id = ?";

    public static final String TABLE_PLAYERS_UPSERT_LASTSEEN =
            "INSERT INTO players (player_id, first_seen, last_seen) " +
                    "VALUES (?, ?, ?) " +
                    "ON CONFLICT (player_id) " +
                    "DO UPDATE SET last_seen = EXCLUDED.last_seen";

    public static final String TABLE_PLAYERS_UPSERT =
            "INSERT INTO players (" +
                    "player_id, allow_friend_requests, show_online_status, auto_accept_friends, " +
                    "friend_request_notifications, friend_list_public, first_seen, last_seen) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(player_id) DO UPDATE SET " +
                    "allow_friend_requests = excluded.allow_friend_requests, " +
                    "show_online_status = excluded.show_online_status, " +
                    "auto_accept_friends = excluded.auto_accept_friends, " +
                    "friend_request_notifications = excluded.friend_request_notifications, " +
                    "friend_list_public = excluded.friend_list_public, " +
                    "first_seen = excluded.first_seen, " +
                    "last_seen = excluded.last_seen;";

    public static final String TABLE_PLAYERS_SELECT =
            "SELECT * FROM players WHERE player_id = ?";
}
