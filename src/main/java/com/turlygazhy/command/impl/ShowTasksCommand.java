package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.User;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

/**
 * Created by lol on 24.05.2017.
 */
public class ShowTasksCommand extends Command {
    private WaitingType waitingType;
    private Long userId;

    private List<Task> tasks;
    private Task task;
    List<User> users;
    private int taskIndex = 0;

    public ShowTasksCommand() throws SQLException {
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        initMessage(update, bot);

        if (waitingType == null) {

            userId = userDao.getUserIdByChatId(chatId);
            tasks = taskDao.getTasks(chatId);

            if (tasks == null || tasks.size() == 0) {
                sendMessage(82, chatId, bot);
                return true;
            }
            task = tasks.get(taskIndex);

            sendMessage(81, chatId, bot); // Ваши задания

//            sendStatistic();


            sendTask(task, bot);
            waitingType = WaitingType.COMMAND;
            return false;
        }

        if (tasks.size() == 0) {
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
                // Previous task
                if (updateMessageText.equals(buttonDao.getButtonText(55))) {
                    previousTask(bot);
                    return false;
                }
                // Task is done
                if (updateMessageText.equals(buttonDao.getButtonText(56))) {
                    taskIsDone(bot);
                    return false;
                }
                // Back
                if (updateMessageText.equals(buttonDao.getButtonText(10))) {
                    sendMessage(5, chatId, bot);
                    return true;
                }

                return false;


            // Waiting for choose worker
            case TASK_WORKER:
                List<User> users = userDao.getUsers();
                String taskWorker = updateMessageText.substring(3);
                for (User user : users) {
                    if (user.getName().equals(taskWorker)) {
                        task.setUserId(user.getChatId());
                        task.setStatus(Task.Status.WAITING_FOR_CONFIRMATION);
                        taskDao.updateTask(task);
                        tasks.remove(taskIndex);
                        if (taskIndex != 0) {
                            taskIndex--;
                        }

                        sendMessage(79, chatId, bot);
                        sendMessage(80, task.getUserId(), bot);
                        if (tasks.size() == 0) {
                            sendMessage(82, chatId, bot);
                            return true;
                        }
                        sendTask(task, bot);
                        waitingType = WaitingType.COMMAND;
                        return false;
                    }
                }
                return false;
        }

        return false;
    }

    private void sendStatic(Bot bot) throws SQLException, TelegramApiException {
        sendMessage(buttonDao.getButtonText(85), chatId, bot); // Выполненные задания
        sendMessage(getTaskCountByStatus(Task.Status.DONE), chatId, bot);
        sendMessage(86, chatId, bot); // Невыполненные задания
        sendMessage(getTaskCountByStatus(Task.Status.DOING) + "", chatId, bot);
        sendMessage(87, chatId, bot); // Ожидающие подтверждения
        sendMessage(getTaskCountByStatus(Task.Status.WAITING_FOR_CONFIRMATION) + "", chatId, bot);
    }

    private void taskIsDone(Bot bot) throws SQLException, TelegramApiException {
        task.setStatus(Task.Status.DONE);
        taskDao.updateTask(task);
        tasks.remove(taskIndex);
        informAdmin(bot, 84);

        if (taskIndex != 0) {
            taskIndex--;
        }
        try {
            task = tasks.get(taskIndex);
        } catch (IndexOutOfBoundsException ex) {
            sendMessage(82, chatId, bot);
            return;
        }

    }

    private void previousTask(Bot bot) throws SQLException, TelegramApiException {
        try {
            task = tasks.get(--taskIndex);
        } catch (Exception ex) {
            sendMessage(91, chatId, bot);
            taskIndex = 0;
        }
        sendTask(task, bot);
    }

    private void nextTask(Bot bot) throws SQLException, TelegramApiException {
        try {
            task = tasks.get(++taskIndex);
        } catch (Exception ex) {
            sendMessage(91, chatId, bot);
            taskIndex = tasks.size() - 1;
        }
        sendTask(task, bot);

    }

    private void sendToAnotherWorker(Bot bot) throws SQLException, TelegramApiException {
        List<User> users = userDao.getUsers();
        StringBuilder sb = new StringBuilder();
        for (User user : users) {
            sb.append("/id");
            sb.append(user.getName()).append("\n");
        }
        sendMessage(sb.toString(), chatId, bot);
        waitingType = WaitingType.TASK_WORKER;
    }

    private void rejectTask(Bot bot) throws SQLException, TelegramApiException {
        task.setStatus(Task.Status.REJECTED);
        taskDao.updateTask(task);
        tasks.remove(taskIndex);
        informAdmin(bot, 83); // Задание отклонено

        if (taskIndex != 0) {
            taskIndex--;
        }

        try {
            task = tasks.get(taskIndex);
        } catch (IndexOutOfBoundsException ex) {
            sendMessage(82, chatId, bot); // Задании нет
            return;
        }

        sendTask(task, bot);
    }

    private void acceptTask(Bot bot) throws SQLException, TelegramApiException {
        task.setStatus(Task.Status.DOING);
        taskDao.updateTask(task);
        tasks.remove(taskIndex);

        if (taskIndex != tasks.size() - 1) {
            taskIndex++;
        }

        try {
            task = tasks.get(taskIndex);
        } catch (IndexOutOfBoundsException ex) {
            sendMessage(82, chatId, bot);
            return;
        }

        sendTask(task, bot);
    }

    private void informAdmin(Bot bot, int message) throws SQLException, TelegramApiException {
        sendMessage(message, task.getAddedByUserId(), bot);
        sendMessage(task.toString(), task.getAddedByUserId(), bot);
    }

    private void sendTask(Task task, Bot bot) throws SQLException, TelegramApiException {
        if(users == null) {
            users = userDao.getUsers();
        }
        for (User user : users) {
            if (Objects.equals(user.getChatId(), task.getUserId())) {
                String sb = "<b>" + messageDao.getMessageText(96) + "</b>\n"+ task.getText() + "\n\n" +         // Задание
                        "<b>" + messageDao.getMessageText(97) + "</b>\n" + user.getName() + "\n\n" +            // Ответственный
                        "<b>" + messageDao.getMessageText(98) + "</b>\n" + task.getDeadline() + "\n\n" +        // Дедлайн
                        "<b>" + messageDao.getMessageText(99) + "</b>\n" + task.getStatusString();              // Статус

                bot.sendMessage(new SendMessage()
                        .setText(sb)
                        .setParseMode(ParseMode.HTML)
                        .setChatId(chatId));
                return;
            }
        }
    }

    private int getTaskCountByStatus(Task.Status status) {
        int count = 0;
        for (Task task : tasks) {
            if (task.getStatus().equals(status)) {
                count++;
            }
        }
        return count;
    }
}
