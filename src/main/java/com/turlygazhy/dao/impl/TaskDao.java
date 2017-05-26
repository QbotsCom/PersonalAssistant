package com.turlygazhy.dao.impl;

import com.turlygazhy.connection_pool.ConnectionPool;
import com.turlygazhy.dao.AbstractDao;
import com.turlygazhy.entity.Task;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by lol on 25.05.2017.
 */
public class TaskDao extends AbstractDao {
    private final Connection connection;
    private final String INSERT_INTO_TASK = "INSERT INTO TASK VALUES (default, ?, ?, ?, default, ?)";

    public TaskDao(Connection connection) {
        this.connection = connection;
    }

    public void insertTask(Task task) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(INSERT_INTO_TASK);
        ps.setLong(1, task.getUserId());
        ps.setLong(2, task.getAddedByUserId());
        ps.setString(3, task.getDeadline().toString());
        ps.setString(4, task.getText());
        ps.execute();
    }
}
