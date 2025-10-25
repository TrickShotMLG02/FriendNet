package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core_api.enums.DatabaseType;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseConnection;

import java.sql.SQLException;

public class SQLiteDatabase implements Database {
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

    }

    /**
     * @throws SQLException
     */
    @Override
    public void disconnect() throws SQLException {

    }

    /**
     * @return
     * @throws SQLException
     */
    @Override
    public DatabaseConnection getConnection() throws SQLException {
        return null;
    }

    /**
     * @throws SQLException
     */
    @Override
    public void setupTables() throws SQLException {

    }
}
