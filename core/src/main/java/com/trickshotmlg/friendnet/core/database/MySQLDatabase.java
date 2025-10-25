package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core_api.enums.DatabaseType;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseConnection;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase implements Database {

    private final String host, database, username, password;
    private DatabaseConnection connection;


    public MySQLDatabase(String host, String database, String username, String password) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    /**
     * @return
     */
    @Override
    public DatabaseType getDatabaseType() {
        return DatabaseType.MySQL;
    }

    /**
     * @throws SQLException
     */
    @Override
    public void connect() throws SQLException {
        String url = "jdbc:mysql://" + host + "/" + database + "?useSSL=false";
        connection = new SimpleDatabaseConnection(DriverManager.getConnection(url, username, password));
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
        return connection;
    }

    /**
     * @throws SQLException
     */
    @Override
    public void setupTables() throws SQLException {

    }
}
