package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.User;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendVoice;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Created by lol on 24.05.2017.
 */
public class ShowTasksCommand extends Command {
    private WaitingType waitingType;

    private List<Task> tasks;
    private Task task;
    private int taskIndex = 0;
    private int taskType;

    public ShowTasksCommand() throws SQLException {
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        initMessage(update, bot);

        if (waitingType == null) {
            sendMessage(114, chatId, bot);
            waitingType = WaitingType.CHOOSE_TASK_TYPE;
            return false;
        }

        switch (waitingType) {
            case CHOOSE_TASK_TYPE:
                return chooseTaskType(bot);

            case CHOOSE_TASK_STATUS:
                return chooseTaskStatus(bot);


            case TASK:
                if (updateMessageText.equals(buttonDao.getButtonText(10))) {
                    sendMessage(113, chatId, bot);
                    waitingType = WaitingType.CHOOSE_TASK_STATUS;
                    return false;
                }
                int taskId = Integer.valueOf(updateMessageText.substring(3));
                for (Task task : tasks) {
                    if (task.getId() == taskId) {
                        sendTask(task, bot);
                        this.task = task;
                        break;
                    }
                }
                waitingType = WaitingType.COMMAND;
                return false;

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
                List<User> users = userDao.getUsers(chatId);
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

                        if (!hasTasks()) {
                            sendMessageWithMainMenu(bot);
                            return true;
                        }

                        sendTask(task, bot);
                        waitingType = WaitingType.COMMAND;
                        return false;
                    }
                }
                return false;
            case TASK_REPORT:
                task.setStatus(Task.Status.DONE);
                task.setReport(updateMessageText);
                Date date = new Date();
                int dateToString = date.getDate();
                String stringDate;
                if (dateToString > 9) {
                    stringDate = String.valueOf(dateToString);
                } else {
                    stringDate = "0" + dateToString;
                }
                int monthToString = date.getMonth() + 1;
                String stringMonth;
                if (monthToString > 9) {
                    stringMonth = String.valueOf(monthToString);
                } else {
                    stringMonth = "0" + monthToString;
                }
                task.setDateOfCompletion(stringDate + "." + stringMonth);
                taskDao.updateTask(task);
                tasks.remove(taskIndex);
                waitingType = WaitingType.COMMAND;
                informAdmin(bot, 84);

                if (taskIndex != 0) {
                    taskIndex--;
                }
                if (!hasTasks()) {
                    sendMessageWithMainMenu(bot);
                    return true;
                }
                task = tasks.get(taskIndex);
                return false;
        }

        return false;
    }

    private boolean chooseTaskStatus(Bot bot) throws SQLException, TelegramApiException {
        Task.Status status = null;
        if (updateMessageText.equals(buttonDao.getButtonText(60))) {         // Выполненные
            status = Task.Status.DONE;
        } else if (updateMessageText.equals(buttonDao.getButtonText(61))) {  // Не выполненные
            status = Task.Status.DOING;
        } else if (updateMessageText.equals(buttonDao.getButtonText(10))) {  // Назад
            waitingType = WaitingType.CHOOSE_TASK_TYPE;
            sendMessage(114, chatId, bot);          // Выберите
            return false;
        }

        if (taskType == 0) {
            if (status == null) {
                tasks = taskDao.getTasks(chatId);           // Все задания для пользователя
            } else {
                tasks = taskDao.getTasks(chatId, status);   // Задания для пользователя с определенным статусом
            }
        } else if (taskType == 1) {
            if (status == null) {
                tasks = taskDao.getTasksAddedBy(chatId);           // Задания для подченных пользователя
            } else {
                tasks = taskDao.getTasksAddedBy(chatId, status);   // Задания для подчиненных пользователя с определенным статусом
            }
        }
        if (!hasTasks()) {
            sendMessage(82, chatId, bot);       // Задании нет
            return false;
        }
        task = tasks.get(taskIndex);
//                sendMessage(81, chatId, bot); // Ваши задания
        StringBuilder sb = new StringBuilder();
        for (Task task : tasks) {
            sb.append("/id").append(task.getId()).append(" - ").append(task.getText()).append("\n");
        }
        bot.sendMessage(new SendMessage()
        .setReplyMarkup(keyboardMarkUpDao.select(2))
        .setText(sb.toString())
        .setChatId(chatId));
        waitingType = WaitingType.TASK;
        return false;
    }

    private boolean chooseTaskType(Bot bot) throws SQLException, TelegramApiException {
        if (updateMessageText.equals(buttonDao.getButtonText(63))) {         // Задания для меня
            taskType = 0;                   //
        } else if (updateMessageText.equals(buttonDao.getButtonText(64))) {  // Задания для работников
            taskType = 1;                   //
        } else if (updateMessageText.equals(buttonDao.getButtonText(10))) { // Назад
            sendMessage(5, chatId, bot);                // Главное меню
            return true;
        }
        sendMessage(113, chatId, bot);
        waitingType = WaitingType.CHOOSE_TASK_STATUS;
        return false;
    }

    private void sendMessageWithMainMenu(Bot bot) throws SQLException, TelegramApiException {
        sendMessage(82, chatId, bot); // Задании нет, меню админа
    }

    private void taskIsDone(Bot bot) throws SQLException, TelegramApiException {
        waitingType = WaitingType.TASK_REPORT;
        sendMessage(107, chatId, bot);      // Напиште отчет
    }

    private boolean hasTasks() {
        return !(tasks == null || tasks.size() == 0);
    }

    private void previousTask(Bot bot) throws SQLException, TelegramApiException {
        try {
            task = tasks.get(--taskIndex);
        } catch (Exception ex) {
            sendMessage(91, chatId, bot);   // Дальше задании нет
            taskIndex = 0;
        }
        sendTask(task, bot);
    }

    private void nextTask(Bot bot) throws SQLException, TelegramApiException {
        try {
            task = tasks.get(++taskIndex);
        } catch (Exception ex) {
            sendMessage(91, chatId, bot);   // Дальше задании нет
            taskIndex = tasks.size() - 1;
        }
        sendTask(task, bot);

    }

    private void sendToAnotherWorker(Bot bot) throws SQLException, TelegramApiException {
        List<User> users = userDao.getUsers(chatId);
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
        if (task.isHasAudio()) {
            bot.sendVoice(new SendVoice()
                    .setChatId(task.getAddedByUserId())
                    .setVoice(task.getVoiceMessageId()));
        }
        bot.sendMessage(new SendMessage()
                .setParseMode(ParseMode.HTML)
                .setChatId(task.getAddedByUserId())
                .setText(task.toString()));
    }

    private void sendTask(Task task, Bot bot) throws SQLException, TelegramApiException {

        if (task.isHasAudio()) {
            bot.sendVoice(new SendVoice()
                    .setChatId(task.getUserId())
                    .setVoice(task.getVoiceMessageId()));
        }
        bot.sendMessage(new SendMessage()
                .setText(task.toString())
                .setParseMode(ParseMode.HTML)
                .setChatId(chatId)
                .setReplyMarkup(keyboardMarkUpDao.select(messageDao.getMessage(81).getKeyboardMarkUpId())));


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