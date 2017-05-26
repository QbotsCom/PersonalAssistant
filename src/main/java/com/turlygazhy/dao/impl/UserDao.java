package com.turlygazhy.dao.impl;

import com.turlygazhy.connection_pool.ConnectionPool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Created by user on 12/18/16.
 */
public class UserDao {
    private static final String SELECT_USER_CHAT_ID = "SELECT * FROM PUBLIC.USER WHERE ID=?";
    private static final String SELECT_FROM_USER = "SELECT * FROM USER";
    private static final int PARAMETER_USER_ID = 1;
    private static final int CHAT_ID_COLUMN_INDEX = 2;
    public static final int ADMIN_ID = 2;
    private Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
    }

    public Long getAdminChatId() {
        try {
            PreparedStatement ps = connection.prepareStatement(SELECT_USER_CHAT_ID);
            ps.setLong(PARAMETER_USER_ID, ADMIN_ID);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            rs.next();
            return rs.getLong(CHAT_ID_COLUMN_INDEX);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isAdmin(Long chatId) {
        return Objects.equals(chatId, getAdminChatId());
    }

    public Long getChatIdByUserId(Long id){
        try {
            PreparedStatement ps = connection.prepareStatement(SELECT_USER_CHAT_ID);
            ps.setLong(PARAMETER_USER_ID, id);
            ps.execute();
            ResultSet rs = ps.getResultSet();
            rs.next();
            return rs.getLong(CHAT_ID_COLUMN_INDEX);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    public ResultSet getUsers() throws SQLException {
        PreparedStatement ps = ConnectionPool.getConnection().prepareStatement(SELECT_FROM_USER);
        ps.execute();
        return ps.getResultSet();
    }
}
