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
    public synchronized void connect() throws SQLException {
        loadDriver();
        connection = new SimpleDatabaseConnection(DriverManager.getConnection(buildJdbcUrl(host, database, databaseType), username, password));
    }

    /**
     * @throws SQLException
     */
    @Override
    public synchronized void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) connection.close();
        connection = null;
    }

    /**
     * @return
     * @throws SQLException
     */
    @Override
    public synchronized DatabaseConnection getConnection() throws SQLException {
        try {
            if (isConnectionUsable()) return connection;
        } catch (SQLException e) {
            closeQuietly();
        }

        // no active connection - connect and return new connection
        closeQuietly();
        connect();
        return connection;
    }

    private boolean isConnectionUsable() throws SQLException {
        return connection != null
                && !connection.isClosed()
                && connection.getConnection().isValid(2);
    }

    private void closeQuietly() {
        if (connection == null) {
            return;
        }

        try {
            if (!connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException ignored) {
            // Reconnect path: stale connections should not prevent opening a new one.
        } finally {
            connection = null;
        }
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

    static String buildJdbcUrl(String host, String database, DatabaseType databaseType) throws SQLException {
        if (databaseType == DatabaseType.MySQL) {
            return "jdbc:mysql://" + host + "/" + database +
                    "?useSSL=false&connectTimeout=10000&socketTimeout=30000&tcpKeepAlive=true";
        }

        if (databaseType == DatabaseType.MariaDB) {
            return "jdbc:mariadb://" + host + "/" + database +
                    "?useSsl=false&connectTimeout=10000&socketTimeout=30000&tcpKeepAlive=true";
        }

        throw new SQLException("Unsupported SQL database type for MySQLDatabase: " + databaseType);
    }
}
