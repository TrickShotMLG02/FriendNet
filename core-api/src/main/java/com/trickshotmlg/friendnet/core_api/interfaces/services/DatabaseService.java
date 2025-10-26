package com.trickshotmlg.friendnet.core_api.interfaces.services;

import com.trickshotmlg.friendnet.core_api.interfaces.database.Database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public interface DatabaseService extends BaseService {

    Database getDatabase();

    <R> R execute(String sql, SQLFunction<PreparedStatement, R> executor) throws SQLException;

    @FunctionalInterface
    interface SQLFunction<T, R> {
        R apply(T t) throws SQLException;
    }

}
