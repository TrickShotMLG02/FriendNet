package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core_api.enums.DatabaseType;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseConnection;

import java.io.File;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

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
        return null;
    }

    /**
     * @throws SQLException
     */
    @Override
    public void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = new SimpleDatabaseConnection(DriverManager.getConnection("jdbc:sqlite:" + sqlFile));
        }
        catch (SQLException ex) {
            //TODO: plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        } catch (ClassNotFoundException ex) {
            //TODO: plugin.getLogger().log(Level.SEVERE, "You need the SQLite JDBC library. Google it. Put it in /lib folder.");
        }
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
        if (!this.sqlFile.exists()) {
            try {
                this.sqlFile.createNewFile();
            } catch (IOException e) {
                //TODO: plugin.getLogger().log(Level.SEVERE, "File write error: " + this.dbName + ".db");
            }
        }

        try {
            if (connection != null && !connection.isClosed()) {
                return connection;
            }

            // no active connection - connect and return new connection
            connect();
            return connection;

        } catch (SQLException ex) {
            System.err.println(ex.getMessage());
            //TODO: plugin.getLogger().log(Level.SEVERE,"SQLite exception on initialize", ex);
        }
        return null;
    }
}
