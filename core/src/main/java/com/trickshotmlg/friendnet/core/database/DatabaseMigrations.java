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
                                SQLTables.TABLE_CREATE_PLAYERDATA,
                                SQLTables.TABLE_CREATE_FRIENDSHIPS,
                                SQLTables.TABLE_CREATE_BLOCKLIST
                        )
                )
        );
    }
}
