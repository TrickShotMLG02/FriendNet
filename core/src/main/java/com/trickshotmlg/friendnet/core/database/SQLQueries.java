package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core_api.enums.DatabaseType;

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
                    "player_id, last_display_name, first_seen, last_seen, last_server_name) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON CONFLICT(player_id) DO UPDATE SET " +
                    "last_display_name = excluded.last_display_name, " +
                    "first_seen = excluded.first_seen, " +
                    "last_seen = excluded.last_seen, " +
                    "last_server_name = excluded.last_server_name;";

    public static final String TABLE_PLAYERS_UPSERT_MYSQL =
            "INSERT INTO players (" +
                    "player_id, last_display_name, first_seen, last_seen, last_server_name) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "last_display_name = VALUES(last_display_name), " +
                    "first_seen = VALUES(first_seen), " +
                    "last_seen = VALUES(last_seen), " +
                    "last_server_name = VALUES(last_server_name);";

    public static final String TABLE_PLAYER_SETTINGS_UPSERT =
            "INSERT INTO player_settings (" +
                    "player_id, allow_friend_requests, show_online_status, auto_accept_friends, " +
                    "friend_request_notifications, friend_list_public, locale) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(player_id) DO UPDATE SET " +
                    "allow_friend_requests = excluded.allow_friend_requests, " +
                    "show_online_status = excluded.show_online_status, " +
                    "auto_accept_friends = excluded.auto_accept_friends, " +
                    "friend_request_notifications = excluded.friend_request_notifications, " +
                    "friend_list_public = excluded.friend_list_public, " +
                    "locale = excluded.locale;";

    public static final String TABLE_PLAYER_SETTINGS_UPSERT_MYSQL =
            "INSERT INTO player_settings (" +
                    "player_id, allow_friend_requests, show_online_status, auto_accept_friends, " +
                    "friend_request_notifications, friend_list_public, locale) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "allow_friend_requests = VALUES(allow_friend_requests), " +
                    "show_online_status = VALUES(show_online_status), " +
                    "auto_accept_friends = VALUES(auto_accept_friends), " +
                    "friend_request_notifications = VALUES(friend_request_notifications), " +
                    "friend_list_public = VALUES(friend_list_public), " +
                    "locale = VALUES(locale);";

    public static final String TABLE_PLAYERS_SELECT =
            "SELECT p.*, " +
                    "COALESCE(s.allow_friend_requests, TRUE) AS allow_friend_requests, " +
                    "COALESCE(s.show_online_status, TRUE) AS show_online_status, " +
                    "COALESCE(s.auto_accept_friends, FALSE) AS auto_accept_friends, " +
                    "COALESCE(s.friend_request_notifications, TRUE) AS friend_request_notifications, " +
                    "COALESCE(s.friend_list_public, FALSE) AS friend_list_public, " +
                    "COALESCE(s.locale, 'EN') AS locale " +
                    "FROM players p LEFT JOIN player_settings s ON s.player_id = p.player_id " +
                    "WHERE p.player_id = ?";

    public static final String TABLE_PLAYERS_SELECT_BY_LAST_DISPLAY_NAME =
            "SELECT p.*, " +
                    "COALESCE(s.allow_friend_requests, TRUE) AS allow_friend_requests, " +
                    "COALESCE(s.show_online_status, TRUE) AS show_online_status, " +
                    "COALESCE(s.auto_accept_friends, FALSE) AS auto_accept_friends, " +
                    "COALESCE(s.friend_request_notifications, TRUE) AS friend_request_notifications, " +
                    "COALESCE(s.friend_list_public, FALSE) AS friend_list_public, " +
                    "COALESCE(s.locale, 'EN') AS locale " +
                    "FROM players p LEFT JOIN player_settings s ON s.player_id = p.player_id " +
                    "WHERE LOWER(p.last_display_name) = LOWER(?)";

    public static final String TABLE_PLAYERS_DELETE =
            "DELETE FROM players WHERE player_id = ?";


    public static final String TABLE_FRIENDSHIPS_UPSERT =
            "INSERT INTO friendships (" +
                    "player1_id, player2_id, requester_id, status, request_sent_time, friend_since) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT(player1_id, player2_id) DO UPDATE SET " +
                    "requester_id = excluded.requester_id, " +
                    "status = excluded.status, " +
                    "request_sent_time = excluded.request_sent_time, " +
                    "friend_since = excluded.friend_since;";

    public static final String TABLE_FRIENDSHIPS_UPSERT_MYSQL =
            "INSERT INTO friendships (" +
                    "player1_id, player2_id, requester_id, status, request_sent_time, friend_since) " +
                    "VALUES (?, ?, ?, ?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "requester_id = VALUES(requester_id), " +
                    "status = VALUES(status), " +
                    "request_sent_time = VALUES(request_sent_time), " +
                    "friend_since = VALUES(friend_since);";

    public static final String TABLE_FRIENDSHIPS_SELECT =
            "SELECT * FROM friendships WHERE player1_id = ? OR player2_id = ?";

    public static final String TABLE_FRIENDSHIPS_DELETE =
            "DELETE FROM friendships WHERE (player1_id = ? AND player2_id = ?) OR (player2_id = ? AND player1_id = ?)";

    public static final String TABLE_BLOCKLIST_UPSERT =
            "INSERT INTO blocklist (blocker_id, blocked_id, created_at) " +
                    "VALUES (?, ?, ?) " +
                    "ON CONFLICT(blocker_id, blocked_id) DO UPDATE SET " +
                    "created_at = excluded.created_at;";

    public static final String TABLE_BLOCKLIST_UPSERT_MYSQL =
            "INSERT INTO blocklist (blocker_id, blocked_id, created_at) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "created_at = VALUES(created_at);";

    public static final String TABLE_BLOCKLIST_SELECT =
            "SELECT * FROM blocklist WHERE blocker_id = ?";

    public static final String TABLE_BLOCKLIST_DELETE =
            "DELETE FROM blocklist WHERE blocker_id = ? AND blocked_id = ?";

    public static final String TABLE_FAVOURITES_UPSERT =
            "INSERT INTO favourites (favouriter_id, favourite_id, created_at) " +
                    "VALUES (?, ?, ?) " +
                    "ON CONFLICT(favouriter_id, favourite_id) DO UPDATE SET " +
                    "created_at = excluded.created_at;";

    public static final String TABLE_FAVOURITES_UPSERT_MYSQL =
            "INSERT INTO favourites (favouriter_id, favourite_id, created_at) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "created_at = VALUES(created_at);";

    public static final String TABLE_FAVOURITES_DELETE =
            "DELETE FROM favourites WHERE favouriter_id = ? AND favourite_id = ?";

    public static final String TABLE_FAVOURITES_DELETE_PAIR =
            "DELETE FROM favourites WHERE (favouriter_id = ? AND favourite_id = ?) OR (favouriter_id = ? AND favourite_id = ?)";

    public static String playersUpsert(DatabaseType databaseType) {
        return databaseType == DatabaseType.SQLite ? TABLE_PLAYERS_UPSERT : TABLE_PLAYERS_UPSERT_MYSQL;
    }

    public static String friendshipsUpsert(DatabaseType databaseType) {
        return databaseType == DatabaseType.SQLite ? TABLE_FRIENDSHIPS_UPSERT : TABLE_FRIENDSHIPS_UPSERT_MYSQL;
    }

    public static String blocklistUpsert(DatabaseType databaseType) {
        return databaseType == DatabaseType.SQLite ? TABLE_BLOCKLIST_UPSERT : TABLE_BLOCKLIST_UPSERT_MYSQL;
    }

    public static String playerSettingsUpsert(DatabaseType databaseType) {
        return databaseType == DatabaseType.SQLite ? TABLE_PLAYER_SETTINGS_UPSERT : TABLE_PLAYER_SETTINGS_UPSERT_MYSQL;
    }

    public static String favouritesUpsert(DatabaseType databaseType) {
        return databaseType == DatabaseType.SQLite ? TABLE_FAVOURITES_UPSERT : TABLE_FAVOURITES_UPSERT_MYSQL;
    }
}
