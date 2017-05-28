package com.turlygazhy.dao.impl;

import com.turlygazhy.connection_pool.ConnectionPool;
import com.turlygazhy.entity.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by user on 12/18/16.
 */
public class UserDao {
    private static final String SELECT_USER_CHAT_ID = "SELECT * FROM PUBLIC.USER WHERE ID=?";
    private static final String SELECT_FROM_USER = "SELECT * FROM USER";
    private static final String SELECT_FROM_USER_BY_CHAT_ID = "SELECT * FROM USER WHERE CHAT_ID = ?";
    private static final int PARAMETER_USER_ID = 1;
    private static final int PARAMETER_CHAT_ID = 1;
    private static final int CHAT_ID_COLUMN_INDEX = 2;
    private static final int USER_ID_COLUMN_INDEX = 1;
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

    public Long getChatIdByUserId(Long id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(SELECT_USER_CHAT_ID);
        ps.setLong(PARAMETER_USER_ID, id);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        rs.next();
        return rs.getLong(CHAT_ID_COLUMN_INDEX);
    }

    public Long getUserIdByChatId(Long chatId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(SELECT_FROM_USER_BY_CHAT_ID);
        ps.setLong(PARAMETER_CHAT_ID, chatId);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        rs.next();
        return rs.getLong(USER_ID_COLUMN_INDEX);
    }

    public List<User> getUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        PreparedStatement ps = ConnectionPool.getConnection().prepareStatement(SELECT_FROM_USER);
        ps.execute();
        ResultSet rs = ps.getResultSet();

        while(rs.next()){
            User user = new User();
            user.setId(rs.getInt("ID"));
            user.setName(rs.getString("NAME"));
            user.setChatId(rs.getLong("CHAT_ID"));
            users.add(user);
        }

        return users;
    }
}
