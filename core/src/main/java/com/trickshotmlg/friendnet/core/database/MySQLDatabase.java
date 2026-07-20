package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core_api.enums.DatabaseType;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseConnection;

import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLDatabase implements Database {

    private final String host, database, username, password;
    private final DatabaseType databaseType;
    private DatabaseConnection connection;


    public MySQLDatabase(String host, String database, String username, String password) {
        this(host, database, username, password, DatabaseType.MySQL);
    }

    public MySQLDatabase(String host, String database, String username, String password, DatabaseType databaseType) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.databaseType = databaseType;
    }

    /**
     * @return
     */
    @Override
    public DatabaseType getDatabaseType() {
        return databaseType;
    }

    /**
     * @throws SQLException
     */
    @Override
    public void connect() throws SQLException {
        loadDriver();
        String protocol = databaseType == DatabaseType.MariaDB ? "mariadb" : "mysql";
        String url = "jdbc:" + protocol + "://" + host + "/" + database + "?useSSL=false";
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
        if (connection != null && !connection.isClosed()) return connection;

        // no active connection - connect and return new connection
        connect();
        return connection;
    }

    private void loadDriver() throws SQLException {
        String driverClass = getDriverClass();
        try {
            Class.forName(driverClass);
        } catch (ClassNotFoundException e) {
            throw new SQLException("JDBC driver was not found for " + databaseType + ". Missing class: " + driverClass, e);
        }
    }

    private String getDriverClass() throws SQLException {
        if (databaseType == DatabaseType.MySQL) {
            return "com.mysql.cj.jdbc.Driver";
        }

        if (databaseType == DatabaseType.MariaDB) {
            return "org.mariadb.jdbc.Driver";
        }

        throw new SQLException("Unsupported SQL database type for MySQLDatabase: " + databaseType);
    }
}
