package com.turlygazhy.dao.impl;

import com.turlygazhy.dao.AbstractDao;
import com.turlygazhy.entity.Task;
import org.glassfish.hk2.utilities.reflection.Pretty;

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
    //    private static final String SELECT_TASK = "SELECT * FROM TASK";
    private final Connection connection;
    private final String INSERT_INTO_TASK = "INSERT INTO TASK VALUES (default, ?, ?, ?, default, ?, ?, ?, null, null, null)"; //

    public TaskDao(Connection connection) {
        this.connection = connection;
    }

    public Task insertTask(Task task) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(INSERT_INTO_TASK, PreparedStatement.RETURN_GENERATED_KEYS);
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
        ps.executeUpdate();
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            task.setId(rs.getInt(1));
        }
        return task;
    }

    public List<Task> getTasks(Long userId) throws SQLException {
        return getTasks(userId, null);
    }

    public List<Task> getTasks(Long userId, Task.Status status) throws SQLException {
        PreparedStatement ps;
        if (status == null) {
            ps = connection.prepareStatement("SELECT * FROM TASK WHERE USER_ID = ?");
        } else {
            if (status.equals(Task.Status.DONE)) {
                ps = connection.prepareStatement("SELECT * FROM TASK WHERE STATUS = 1 AND USER_ID = ?");
            } else {
                ps = connection.prepareStatement("SELECT * FROM TASK WHERE STATUS != 1 AND USER_ID = ?");
            }
        }
        ps.setLong(1, userId);

        ps.execute();

        ResultSet rs = ps.getResultSet();
        ArrayList<Task> tasks = new ArrayList<>();
        while (rs.next()) {
            tasks.add(parseTask(rs));
        }
        return tasks;
    }

    public List<Task> getTasksAddedBy() throws SQLException {
        return getTasksAddedBy(null, null);
    }

    public List<Task> getTasksAddedBy(Long addedBy) throws SQLException {
        return getTasksAddedBy(addedBy, null);
    }

    public List<Task> getTasksAddedBy(Long addedBy, Task.Status status) throws SQLException {
        List<Task> tasks = new ArrayList<>();
        PreparedStatement ps;
        if (status == null) {
            ps = connection.prepareStatement("SELECT * FROM TASK WHERE ADDED_BY_USER_ID = ?");
        } else {
            if (status.equals(Task.Status.DONE)) {
                ps = connection.prepareStatement("SELECT * FROM TASK WHERE STATUS = 1 AND ADDED_BY_USER_ID = ?");
            } else {
                ps = connection.prepareStatement("SELECT * FROM TASK WHERE STATUS != 1 AND ADDED_BY_USER_ID = ?");
            }
        }
        ps.setLong(1, addedBy);
        ps.execute();
        ResultSet rs = ps.getResultSet();
        while (rs.next()) {
            tasks.add(parseTask(rs));
        }

        return tasks;
    }

    private Task parseTask(ResultSet rs) throws SQLException {
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
        task.setCause(rs.getString(10));                // Cause
        task.setDateOfCompletion(rs.getString(11));     // Date of completion
        return task;
    }

    public void updateTask(Task task) throws SQLException {
        String UPDATE_TASK = "UPDATE TASK SET STATUS = ?, USER_ID = ?, REPORT = ?, CAUSE = ?, DATE_OF_COMPLETION = ? WHERE ID = ?";
        PreparedStatement ps = connection.prepareStatement(UPDATE_TASK);
        ps.setInt(1, task.getStatus().getId());
        ps.setLong(2, task.getUserId());
        ps.setString(3, task.getReport());
        ps.setString(4, task.getCause());
        ps.setString(5, task.getDateOfCompletion());
        ps.setInt(6, task.getId());
        ps.execute();
    }


    public Task getTask(int id) throws SQLException {
        PreparedStatement ps = connection.prepareStatement("SELECT * FROM TASK WHERE ID = ?");
        ps.setInt(1, id);
        ps.execute();

        ResultSet rs = ps.getResultSet();
        if (rs.next()) {
            return parseTask(rs);
        }
        return null;
    }
}
