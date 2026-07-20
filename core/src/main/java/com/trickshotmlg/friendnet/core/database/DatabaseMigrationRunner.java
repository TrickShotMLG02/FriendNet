package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core.Logger;
import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseMigrationRunner {
    private static final String SELECT_APPLIED_MIGRATIONS =
            "SELECT version FROM friendnet_schema_migrations";
    private static final String INSERT_APPLIED_MIGRATION =
            "INSERT INTO friendnet_schema_migrations (version, description) VALUES (?, ?)";

    private final List<DatabaseMigration> migrations;

    public DatabaseMigrationRunner(List<DatabaseMigration> migrations) {
        this.migrations = migrations.stream()
                .sorted(Comparator.comparingInt(DatabaseMigration::getVersion))
                .toList();
        validateUniqueVersions(this.migrations);
    }

    public void migrate(DatabaseConnection connection) throws SQLException {
        createMigrationTable(connection);
        Set<Integer> appliedVersions = getAppliedVersions(connection);

        for (DatabaseMigration migration : migrations) {
            if (appliedVersions.contains(migration.getVersion())) {
                continue;
            }

            applyMigration(connection, migration);
            Logger.info("Applied database migration " + migration.getVersion() + ": " + migration.getDescription());
        }
    }

    private void createMigrationTable(DatabaseConnection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SQLTables.TABLE_CREATE_SCHEMA_MIGRATIONS)) {
            ps.execute();
        }
    }

    private Set<Integer> getAppliedVersions(DatabaseConnection connection) throws SQLException {
        Set<Integer> appliedVersions = new HashSet<>();

        try (PreparedStatement ps = connection.prepareStatement(SELECT_APPLIED_MIGRATIONS);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                appliedVersions.add(rs.getInt("version"));
            }
        }

        return appliedVersions;
    }

    private void applyMigration(DatabaseConnection connection, DatabaseMigration migration) throws SQLException {
        for (String statement : migration.getStatements()) {
            try (PreparedStatement ps = connection.prepareStatement(statement)) {
                ps.execute();
            }
        }

        try (PreparedStatement ps = connection.prepareStatement(INSERT_APPLIED_MIGRATION)) {
            ps.setInt(1, migration.getVersion());
            ps.setString(2, migration.getDescription());
            ps.executeUpdate();
        }
    }

    private void validateUniqueVersions(List<DatabaseMigration> migrations) {
        Set<Integer> versions = new HashSet<>();
        for (DatabaseMigration migration : migrations) {
            if (!versions.add(migration.getVersion())) {
                throw new IllegalArgumentException("Duplicate database migration version: " + migration.getVersion());
            }
        }
    }
}
