package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core_api.enums.DatabaseType;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseConnection;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase implements Database {

    private final File dataFolder, sqlFile;
    private final String dbName;

    private DatabaseConnection connection;

    public SQLiteDatabase(File dataFolder, String databaseName) {

        this.dataFolder = dataFolder;
        this.dbName = databaseName;
        this.sqlFile = new File(dataFolder, databaseName + ".db");
    }

    /**
     * @return
     */
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.SQLite;
    }

    /**
     * @throws SQLException
     */
    @Override
    public void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new SQLException("SQLite JDBC driver was not found. Make sure sqlite-jdbc is available in the plugin jar.", ex);
        }

        connection = new SimpleDatabaseConnection(DriverManager.getConnection("jdbc:sqlite:" + sqlFile));
    }

    /**
     * @throws SQLException
     */
    @Override
    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
    }

    /**
     * @return
     * @throws SQLException
     */
    @Override
    public DatabaseConnection getConnection() throws SQLException {
        if (!this.dataFolder.exists() && !this.dataFolder.mkdirs()) {
            throw new SQLException("Could not create plugin data folder: " + this.dataFolder.getAbsolutePath());
        }

        if (!this.sqlFile.exists()) {
            try {
                if (!this.sqlFile.createNewFile()) {
                    throw new SQLException("Could not create SQLite database file: " + this.sqlFile.getAbsolutePath());
                }
            } catch (IOException e) {
                throw new SQLException("Could not create SQLite database file: " + this.sqlFile.getAbsolutePath(), e);
            }
        }

        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        // no active connection - connect and return new connection
        connect();
        return connection;
    }
}
