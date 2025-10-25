package com.trickshotmlg.friendnet.core_api.interfaces.database;

import com.trickshotmlg.friendnet.core_api.enums.DatabaseType;

import java.sql.SQLException;

public interface Database {

    public DatabaseType getDatabaseType();
    public void connect() throws SQLException;
    public void disconnect() throws SQLException;
    public DatabaseConnection getConnection() throws SQLException;
}
