package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SimpleDatabaseConnection implements DatabaseConnection {

    /**
     * The actual database connection
     */
    private final Connection connection;

    public SimpleDatabaseConnection(Connection connection) {
        this.connection = connection;
    }


    /**
     * @param sql
     * @return
     * @throws SQLException
     */
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return connection.prepareStatement(sql);
    }

    /**
     * @return
     * @throws SQLException
     */
    @Override
    public Connection getConnection() throws SQLException {
        return connection;
    }

    /**
     * @throws SQLException
     */
    @Override
    public void close() throws SQLException {

    }

    /**
     * @return
     * @throws SQLException
     */
    @Override
    public boolean isClosed() throws SQLException {
        if (connection == null) {
            return true;
        }

        return connection.isClosed();
    }
}
