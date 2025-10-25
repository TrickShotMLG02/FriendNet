package com.trickshotmlg.friendnet.core.database;

import com.trickshotmlg.friendnet.core_api.enums.ServiceState;
import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;
import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseConnection;
import com.trickshotmlg.friendnet.core_api.interfaces.database.DatabaseService;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseServiceImpl implements DatabaseService {

    private File dataFolder;
    private String dbName;

    private Database database;

    public DatabaseServiceImpl(File dataFolder, String dbName) {
        this.dataFolder = dataFolder;
        this.dbName = dbName;
    }

    /**
     * @return
     */
    @Override
    public Database getDatabase() {
        return database;
    }

    /**
     * @param sql
     * @param executor
     * @param <R>
     * @return
     * @throws SQLException
     */
    @Override
    public <R> R execute(String sql, SQLFunction<PreparedStatement, R> executor) throws SQLException {
        return null;
    }

    /**
     *
     */
    @Override
    public void init() {
        database = new SQLiteDatabase(dataFolder, dbName);
    }

    /**
     *
     */
    @Override
    public void postInit() {
        try {
            DatabaseConnection conn = database.getConnection();

            // create default tables
            PreparedStatement ps = conn.prepareStatement(SQLTables.TABLE_CREATE_PLAYERDATA);
            ps.execute();
            ps.close();

            ps = conn.prepareStatement(SQLTables.TABLE_CREATE_FRIENDSHIPS);
            ps.execute();
            ps.close();

        } catch (SQLException e) {

        }
    }

    /**
     *
     */
    @Override
    public void start() {

    }

    /**
     *
     */
    @Override
    public void stop() {

    }

    /**
     *
     */
    @Override
    public void destroy() {

    }

    /**
     * @return
     */
    @Override
    public String getName() {
        return "";
    }

    /**
     * @return
     */
    @Override
    public ServiceState getState() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public boolean isRunning() {
        return false;
    }
}
