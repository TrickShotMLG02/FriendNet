package com.trickshotmlg.friendnet.core.database;

import java.util.List;

public class DatabaseMigration {
    private final int version;
    private final String description;
    private final List<String> statements;

    public DatabaseMigration(int version, String description, List<String> statements) {
        this.version = version;
        this.description = description;
        this.statements = statements;
    }

    public int getVersion() {
        return version;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getStatements() {
        return statements;
    }
}
