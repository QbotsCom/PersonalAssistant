package com.turlygazhy.dao.impl;

import com.turlygazhy.dao.AbstractDao;
import com.turlygazhy.entity.Task;

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
    private static final String SELECT_TASK = "SELECT * FROM TASK";
    private final Connection connection;
    private final String INSERT_INTO_TASK = "INSERT INTO TASK VALUES (default, ?, ?, ?, default, ?, ?, ?, null, null)"; //
    private final String SELECT_TASK_BY_USER_ID_AND_STATUS = "SELECT * FROM TASK WHERE USER_ID = ? AND STATUS = ?"; //
    private final String SELECT_TASK_BY_USER_ID_AND_STATUS_NOT_EQUAL = "SELECT * FROM TASK WHERE USER_ID = ? AND STATUS != ?"; //
    private final String SELECT_TASK_BY_STATUS = "SELECT * FROM TASK WHERE STATUS = ?"; //
    private final String SELECT_TASK_BY_STATUS_NOT_EQUAL = "SELECT * FROM TASK WHERE STATUS != ?"; //
    private final String SELECT_TASK_BY_USER_ID = "SELECT * FROM TASK WHERE USER_ID = ?"; //
    private final String UPDATE_TASK = "UPDATE TASK SET STATUS = ?, USER_ID = ?, REPORT = ?, DATE_OF_COMPLETION = ? WHERE ID = ?"; //

    public TaskDao(Connection connection) {
        this.connection = connection;
    }

    public void insertTask(Task task) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(INSERT_INTO_TASK);
        ps.setLong(1, task.getUserId());
        ps.setLong(2, task.getAddedByUserId());
        ps.setString(3, task.getDeadline());
        ps.setBoolean(4, task.isHasAudio());
        if (task.isHasAudio()) {
            ps.setString(5, task.getVoiceMessageId());
            ps.setString(6, null);
        } else {
            ps.setString(5, null);
            ps.setString(6, task.getText());
        }
        ps.execute();
    }
    
    public List<Task> getTasks() throws SQLException {
        return getTasks(null, null);
    }

    public List<Task> getTasks(Long userId) throws SQLException {
        return getTasks(userId, null);
    }

    public List<Task> getTasks(Long userId, Task.Status status) throws SQLException {
        PreparedStatement ps;
        if (userId == null) {
            if (status == null) {
                ps = connection.prepareStatement(SELECT_TASK);
            } else if (status == Task.Status.DONE){
                ps = connection.prepareStatement(SELECT_TASK_BY_STATUS);
                ps.setInt(1, status.getId());
            } else {
                ps = connection.prepareStatement(SELECT_TASK_BY_STATUS_NOT_EQUAL);
                ps.setInt(1, Task.Status.DONE.getId());
            }
        } else {
            if (status == null) {
                ps = connection.prepareStatement(SELECT_TASK_BY_USER_ID);
            } else if(status == Task.Status.DONE){
                ps = connection.prepareStatement(SELECT_TASK_BY_USER_ID_AND_STATUS);
                ps.setInt(2, status.getId());
            } else {
                ps = connection.prepareStatement(SELECT_TASK_BY_USER_ID_AND_STATUS_NOT_EQUAL);
                ps.setInt(2, Task.Status.DONE.getId());
            }
            ps.setLong(1, userId);
        }
        
        ps.execute();

        ResultSet rs = ps.getResultSet();

        ArrayList<Task> tasks = new ArrayList<>();
        while (rs.next()) {
            Task task = new Task(rs.getInt(1));             // Task ID
            task.setUserId(rs.getLong(2));                  // Task Executor
            task.setAddedByUserId(rs.getLong(3));           // Added User ID
            task.setDeadline(rs.getString(4));              // Deadline
            task.setStatus(rs.getInt(5));                   // Status
            task.setHasAudio(rs.getBoolean(6));             // HasAudio
            if (task.isHasAudio()) {
                task.setVoiceMessageId(rs.getString(7));    // Task Voice
            } else {
                task.setText(rs.getString(8));              // Task Text
            }
            task.setReport(rs.getString(9));                // Report
            task.setDateOfCompletion(rs.getString(10));     // Date of completion
            tasks.add(task);
        }

        return tasks;
    }

    public void updateTask(Task task) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(UPDATE_TASK);
        ps.setInt(1, task.getStatus().getId());
        ps.setLong(2, task.getUserId());
        ps.setString(4, task.getDateOfCompletion());
        ps.setString(3, task.getReport());
        ps.setInt(5, task.getId());
        ps.execute();
    }

    public List<Task> getTasks(Task.Status status) throws SQLException {
        return getTasks(null, status);
    }
}
