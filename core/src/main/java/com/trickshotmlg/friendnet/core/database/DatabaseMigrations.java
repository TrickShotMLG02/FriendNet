package com.trickshotmlg.friendnet.core.database;

import java.util.List;

public final class DatabaseMigrations {
    private DatabaseMigrations() {
    }

    public static List<DatabaseMigration> all() {
        return List.of(
                new DatabaseMigration(
                        1,
                        "Create initial FriendNet schema",
                        List.of(
                                SQLTables.TABLE_CREATE_LEGACY_PLAYERDATA,
                                SQLTables.TABLE_CREATE_LEGACY_FRIENDSHIPS,
                                SQLTables.TABLE_CREATE_BLOCKLIST
                        )
                ),
                new DatabaseMigration(
                        2,
                        "Move favourites and player settings to dedicated tables",
                        List.of(
                                SQLTables.TABLE_CREATE_PLAYER_SETTINGS,
                                SQLTables.TABLE_CREATE_FAVOURITES,
                                "INSERT INTO player_settings (" +
                                        "player_id, allow_friend_requests, show_online_status, auto_accept_friends, " +
                                        "friend_request_notifications, friend_list_public, locale) " +
                                        "SELECT player_id, allow_friend_requests, show_online_status, auto_accept_friends, " +
                                        "friend_request_notifications, friend_list_public, locale FROM players p " +
                                        "WHERE NOT EXISTS (" +
                                        "SELECT 1 FROM player_settings s WHERE s.player_id = p.player_id" +
                                        ")",
                                "INSERT INTO favourites (favouriter_id, favourite_id) " +
                                        "SELECT player1_id, player2_id FROM friendships f " +
                                        "WHERE is_favourite = TRUE AND status = 'Accepted' " +
                                        "AND player1_id <> player2_id " +
                                        "AND NOT EXISTS (" +
                                        "SELECT 1 FROM favourites fav " +
                                        "WHERE fav.favouriter_id = f.player1_id AND fav.favourite_id = f.player2_id" +
                                        ")",
                                "INSERT INTO favourites (favouriter_id, favourite_id) " +
                                        "SELECT player2_id, player1_id FROM friendships f " +
                                        "WHERE is_favourite = TRUE AND status = 'Accepted' " +
                                        "AND player1_id <> player2_id " +
                                        "AND NOT EXISTS (" +
                                        "SELECT 1 FROM favourites fav " +
                                        "WHERE fav.favouriter_id = f.player2_id AND fav.favourite_id = f.player1_id" +
                                        ")",
                                "ALTER TABLE players ADD COLUMN last_server_name VARCHAR(64) DEFAULT NULL",
                                "ALTER TABLE players DROP COLUMN allow_friend_requests",
                                "ALTER TABLE players DROP COLUMN show_online_status",
                                "ALTER TABLE players DROP COLUMN auto_accept_friends",
                                "ALTER TABLE players DROP COLUMN friend_request_notifications",
                                "ALTER TABLE players DROP COLUMN friend_list_public",
                                "ALTER TABLE players DROP COLUMN locale",
                                "ALTER TABLE friendships DROP COLUMN is_favourite"
                        )
                ),
                new DatabaseMigration(
                        3,
                        "Store last plain player name",
                        List.of(
                                "ALTER TABLE players ADD COLUMN last_player_name VARCHAR(64) DEFAULT NULL"
                        )
                ),
                new DatabaseMigration(
                        4,
                        "Store cached player skin textures",
                        List.of(
                                "ALTER TABLE players ADD COLUMN skin_texture TEXT DEFAULT NULL",
                                "ALTER TABLE players ADD COLUMN skin_signature TEXT DEFAULT NULL"
                        )
                )
        );
    }
}
