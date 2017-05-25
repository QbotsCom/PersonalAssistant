package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.connection_pool.ConnectionPool;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AddNewTaskCommand extends Command {
    Task task;//todo создай объект Task

    private final String SELECT_FROM_USER = "SELECT * FROM USER";
    private final String INSERT_INTO_TASK = "INSERT INTO TASK VALUES (default, ?, ?, ?, default, ?)";

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        initMessage(update, bot);
        if (waitingType == null) {
            sendMessage(76, chatId, bot);
            waitingType = WaitingType.TASK_TEXT;
            Task task = new Task(chatId);
            return false;
        }

        switch (waitingType) {
            case TASK_TEXT:
                task.setText(updateMessageText);
                sendMessage(77, chatId, bot);
                waitingType = WaitingType.TASK_DEADLINE;
                return false;

            case TASK_DEADLINE:
                task.setDeadline(updateMessageText);
                sendMessage(78, chatId, bot);
                PreparedStatement ps = ConnectionPool.getConnection().prepareStatement(SELECT_FROM_USER);
                ps.execute();
                ResultSet rs = ps.getResultSet();
                rs.next();
                StringBuilder sb = new StringBuilder();
                while (!rs.isAfterLast()) {
                    sb.append("/id");
                    sb.append(rs.getInt("ID"));
                    sb.append(" ").append(rs.getString("NAME")).append("\n");
                    rs.next();
                }

                sendMessage(sb.toString(), chatId, bot);
                waitingType = WaitingType.TASK_WORKER;
                return false;

            case TASK_WORKER:
                task.setUserId(Long.valueOf(updateMessageText.substring(3)));

                ps = ConnectionPool.getConnection().prepareStatement(INSERT_INTO_TASK);
                ps.setLong(1, task.getUserId());
                ps.setLong(2, task.getAddedByUserId());
                ps.setString(3, task.getDeadline().toString());
                ps.setString(4, task.getText());
                ps.execute();
                sendMessage(79, chatId, bot);

                ps = ConnectionPool.getConnection().prepareStatement("SELECT * FROM USER WHERE ID = ?");
                ps.setLong(1, task.getUserId());
                ps.execute();
                rs = ps.getResultSet();
                rs.next();
                sendMessage(80, rs.getLong("CHAT_ID"), bot);
                return true;
        }
        return false;
    }
}
