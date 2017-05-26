package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.connection_pool.ConnectionPool;
import com.turlygazhy.dao.DaoFactory;
import com.turlygazhy.dao.impl.TaskDao;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.WaitingType;
import com.turlygazhy.tool.DateUtil;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;


public class AddNewTaskCommand extends Command {
    Task task;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        initMessage(update, bot);
        if (waitingType == null) {
            sendMessage(76, chatId, bot);
            waitingType = WaitingType.TASK_TEXT;
            task = new Task(chatId);
            return false;
        }

        switch (waitingType) {
            case TASK_TEXT:
                task.setText(updateMessageText);
                sendMessage(77, chatId, bot);
                waitingType = WaitingType.TASK_DEADLINE;
                return false;

            case TASK_DEADLINE:

                task.setDeadline(DateUtil.parseString(updateMessageText));

                sendMessage(78, chatId, bot);

                ResultSet rs = userDao.getUsers();
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
                taskDao.insertTask(task);
                sendMessage(79, chatId, bot);

                sendMessage(80,
                            userDao.getChatIdByUserId(task.getUserId()),
                            bot);
                return true;
        }
        return false;
    }
}
