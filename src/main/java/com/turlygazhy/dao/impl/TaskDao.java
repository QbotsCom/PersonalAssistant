package com.turlygazhy.dao.impl;

import com.turlygazhy.dao.AbstractDao;
import com.turlygazhy.entity.Task;
import com.turlygazhy.tool.DateUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lol on 25.05.2017.
 */
public class TaskDao extends AbstractDao {
    private final Connection connection;
    private final String INSERT_INTO_TASK = "INSERT INTO TASK VALUES (default, ?, ?, ?, default, ?)"; //
    private final String SELECT_TASK_BY_USER_ID_AND_STATUS = "SELECT * FROM TASK WHERE USER_ID = ? AND STATUS = ?"; //
    private final String SELECT_TASK_BY_USER_ID = "SELECT * FROM TASK WHERE USER_ID = ?"; //
    private final String UPDATE_TASK = "UPDATE TASK SET STATUS = ?, USER_ID = ? WHERE ID = ?"; //

    public TaskDao(Connection connection) {
        this.connection = connection;
    }

    public void insertTask(Task task) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(INSERT_INTO_TASK);
        ps.setLong(1, task.getUserId());
        ps.setLong(2, task.getAddedByUserId());
        ps.setString(3, task.getDeadline());
        ps.setString(4, task.getText());
        ps.execute();
    }

    public List<Task> getTasks(Long userId) throws SQLException {
        return getTasks(userId, null);
    }

    public List<Task> getTasks(Long userId, Task.Status status) throws SQLException {
        PreparedStatement ps;

        if(status == null){
            ps = connection.prepareStatement(SELECT_TASK_BY_USER_ID);
        } else {
            ps = connection.prepareStatement(SELECT_TASK_BY_USER_ID_AND_STATUS);
            ps.setInt(2, status.getId());
        }

        ps.setLong(1, userId);
        ps.execute();

        ResultSet rs = ps.getResultSet();

        ArrayList<Task> tasks = new ArrayList<>();
        while (rs.next()){
            Task task = new Task(rs.getInt("ID"));
            task.setUserId(rs.getLong("USER_ID"));
            task.setText(rs.getString("TEXT"));
            task.setStatus(rs.getInt("STATUS"));
            task.setAddedByUserId(rs.getLong("ADDED_BY_USER_ID"));
            task.setDeadline(rs.getString("DEADLINE"));
            tasks.add(task);
        }

        return tasks;
    }

    public void updateTask(Task task) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(UPDATE_TASK);
        ps.setInt(1, task.getStatus().getId());
        ps.setLong(2, task.getUserId());
        ps.setInt(3, task.getId());
        ps.execute();
    }
}
