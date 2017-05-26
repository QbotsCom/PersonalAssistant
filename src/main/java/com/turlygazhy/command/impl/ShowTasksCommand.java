package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.connection_pool.ConnectionPool;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by lol on 24.05.2017.
 */
public class ShowTasksCommand extends Command {
    WaitingType waitingType;
    Long userId;

    ArrayList<Task> tasks;
    Task task;
    int taskIndex = 0;

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        initMessage(update, bot);

        if (waitingType == null) {

            userId = userDao.getUserIdByChatId(chatId);
            tasks = taskDao.getTasks(userId);
            task = tasks.get(taskIndex);

            if (tasks == null) {
                sendMessage(82, chatId, bot);
                return true;
            }

            sendMessage(81, chatId, bot);
            sendMessage(task.toString(), chatId, bot);
            waitingType = WaitingType.COMMAND;
            return false;
        }

        if (tasks.size() == taskIndex) {
            sendMessage(82, chatId, bot);
            return true;
        }

        switch (waitingType) {
            case COMMAND:
                // Accept
                if (updateMessageText.equals(buttonDao.getButtonText(51))) {
                    acceptTask(bot);
                    return false;
                }
                // Reject
                if (updateMessageText.equals(buttonDao.getButtonText(52))) {
                    rejectTask(bot);
                    return false;
                }
                // For another worker
                if (updateMessageText.equals(buttonDao.getButtonText(53))) {
                    sendToAnotherWorker(bot);
                    return false;
                }
                // Next task
                if (updateMessageText.equals(buttonDao.getButtonText(54))) {
                    nextTask(bot);
                    return false;
                }
                //Previous task
                if (updateMessageText.equals(buttonDao.getButtonText(55))) {
                    previousTask(bot);
                    return false;
                }
                // Task is done
                if (updateMessageText.equals(buttonDao.getButtonText(56))) {
                    taskIsDone(bot);
                    return false;
                }
                if (updateMessageText.equals(buttonDao.getButtonText(10))){
                    sendMessage(5, chatId, bot);
                    return true;
                }

                return false;


            // Waiting for choose worker
            case TASK_WORKER:
                if (updateMessageText.substring(3).matches("a-zA-Zа-яА-Я.:;/")) {
                    sendMessage(78, chatId, bot);
                    return false;
                }
                Long taskWorker = Long.valueOf(updateMessageText.substring(3));
                task.setUserId(taskWorker);
                taskDao.updateTask(task);
                tasks.remove(task);
                if (taskIndex != 0 ){
                    taskIndex--;
                }
                sendMessage(79, chatId, bot);
                sendMessage(80, userDao.getChatIdByUserId(taskWorker), bot);
                if (tasks.size() == taskIndex) {
                    sendMessage(82, chatId, bot);
                    return true;
                }
                sendMessage(task.toString(), chatId, bot);
                waitingType = WaitingType.COMMAND;
                return false;
        }

        return false;
    }

    private void taskIsDone(Bot bot) throws SQLException, TelegramApiException {
        task.setStatus(Task.Status.DONE);
        taskDao.updateTask(task);
        sendMessage(84, task.getAddedByUserId(), bot);
        sendMessage(task.toString(), task.getAddedByUserId(), bot);
    }

    private void previousTask(Bot bot) throws SQLException, TelegramApiException {
        try {
            task = tasks.get(--taskIndex);
        } catch (Exception ex) {
            sendMessage(82, chatId, bot);
            taskIndex = 0;
        }
        sendMessage(task.toString(), chatId, bot);
    }

    private void nextTask(Bot bot) throws SQLException, TelegramApiException {
        try {
            task = tasks.get(++taskIndex);
        } catch (Exception ex) {
            sendMessage(82, chatId, bot);
            taskIndex = tasks.size() - 1;
        }
        sendMessage(task.toString(), chatId, bot);

    }

    private void sendToAnotherWorker(Bot bot) throws SQLException, TelegramApiException {
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
    }

    private void rejectTask(Bot bot) throws SQLException, TelegramApiException {
        task.setStatus(Task.Status.REJECTED);
        taskDao.updateTask(task);
        sendMessage(83, task.getAddedByUserId(), bot);
        sendMessage(task.toString(), task.getAddedByUserId(), bot);
        tasks.remove(task);

        if (taskIndex != 0) {
            taskIndex--;
        }
        task = tasks.get(taskIndex);

        if (!sendTask(chatId, bot)) {
            sendMessage(82, chatId, bot);
        }
    }

    private void acceptTask(Bot bot) throws SQLException, TelegramApiException {
        task.setStatus(Task.Status.DOING);
        taskDao.updateTask(task);
        if (taskIndex != tasks.size() - 1) {
            taskIndex++;
        }

        if (!sendTask(chatId, bot)) {
            sendMessage(82, chatId, bot);
        }
    }

    private boolean sendTask(Long chatId, Bot bot) throws SQLException, TelegramApiException {
        if (taskIndex < tasks.size() && taskIndex >= 0) {
            sendMessage(task.toString(), chatId, bot);
            return true;
        }
        return false;
    }
}
