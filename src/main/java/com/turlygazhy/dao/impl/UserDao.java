package com.turlygazhy.dao.impl;

import com.turlygazhy.connection_pool.ConnectionPool;
import com.turlygazhy.dao.DaoFactory;
import com.turlygazhy.entity.User;
import org.telegram.telegrambots.api.objects.Contact;

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
    private static final String ADD_USER = "INSERT INTO USER (ID, CHAT_ID, NAME) VALUES (default, ?, ?)";
    private static final String DELETE_USER = "DELETE FROM USER WHERE CHAT_ID = ?";
    private static final int PARAMETER_USER_ID = 1;
    private static final int PARAMETER_CHAT_ID = 1;
    private static final int CHAT_ID_COLUMN_INDEX = 2;
    private static final int USER_ID_COLUMN_INDEX = 1;
    public static final int ADMIN_ID = 2;
    private static List<User> users;
    private Connection connection;

    public UserDao(Connection connection) {
        this.connection = connection;
        try {
            getUsers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private void updateUsers() throws SQLException {

        users = null;
        getUsers();
    }

    public List<User> getUsers() throws SQLException {
        if (users == null) {
            users = new ArrayList<>();
            PreparedStatement ps = ConnectionPool.getConnection().prepareStatement(SELECT_FROM_USER);
            ps.execute();
            ResultSet rs = ps.getResultSet();

            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("ID"));
                user.setName(rs.getString("NAME"));
                user.setChatId(rs.getLong("CHAT_ID"));
                users.add(user);
            }
        }
        return users;
    }

    public boolean addUser(Contact contact) throws SQLException {
        if (hasUser(contact)){
            return false;
        }
        PreparedStatement ps = connection.prepareStatement(ADD_USER);
        ps.setInt(1, contact.getUserID());
        ps.setString(2, contact.getFirstName());
        ps.execute();
        updateUsers();
        return true;
    }

    public void deleteUser(int userId) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(DELETE_USER);
        ps.setInt(1, userId);
        ps.execute();
        updateUsers();
    }

    public boolean hasUser(Contact contact){
        for (User user : users){
            if (user.getChatId().equals(Long.valueOf(contact.getUserID())))
                return true;
        }
        return false;
    }
}
