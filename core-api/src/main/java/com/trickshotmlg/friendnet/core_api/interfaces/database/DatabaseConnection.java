package com.trickshotmlg.friendnet.core_api.interfaces.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DatabaseConnection extends AutoCloseable {

    PreparedStatement prepareStatement(String sql) throws SQLException;
    public Connection getConnection() throws SQLException;
    public void close() throws SQLException;

    public boolean isClosed() throws SQLException;
}
