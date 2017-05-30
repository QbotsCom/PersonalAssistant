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
import java.util.List;

/**
 * Created by daniyar on 30.05.17.
 */
public class ShowStatisticCommand extends Command {

    List<User> users;
    Long workerId;

    public ShowStatisticCommand() throws SQLException {
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        initMessage(update, bot);
        if (waitingType == null) {
            waitingType = WaitingType.CHOOSE_USER;
            users = userDao.getUsers();
            sendMessage(104, chatId, bot);      // Выберите сотрудника
            showUsers(chatId, bot);
            return false;
        }

        switch (waitingType) {
            case CHOOSE_USER:
                workerId = Long.valueOf(updateMessageText.substring(3));
                showUserStatistic(chatId, workerId, bot);
                waitingType = WaitingType.CHOOSE_STATUS;
                return false;
            case CHOOSE_STATUS:
                Task.Status status = Task.Status.values()[Integer.parseInt(updateMessageText.substring(3))];    // Выбираем статус
                showUserStatisticByStatus(status, workerId, bot);
                return false;
        }
        return false;
    }

    private void showUserStatisticByStatus(Task.Status status, Long workerId, Bot bot) throws SQLException, TelegramApiException {
        List<Task> tasks = taskDao.getTasks(workerId, status);
        StringBuilder sb;
        for (Task task : tasks) {
            sb = new StringBuilder();
            if (task.isHasAudio()) {
                bot.sendVoice(new SendVoice()
                        .setVoice(task.getVoiceMessageId())
                        .setChatId(chatId));
                sb.append(task.toString()).append("\n");
            }
            sb.append(task.toString());
            bot.sendMessage(new SendMessage()
                    .setParseMode(ParseMode.HTML)
                    .setChatId(chatId)
                    .setText(sb.toString()));

        }

    }



    private void showUserStatistic(Long chatId, Long workerId, Bot bot) throws SQLException, TelegramApiException {
        List<Task> tasks = taskDao.getTasks(workerId);
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(messageDao.getMessageText(111)).append("</b>").append(tasks.size()).append("\n");
        for (Task.Status status : Task.Status.values()) {
            sb.append("/id").append(status.getId()).append(" <b>").append(status.getStatusString(status)).append("</b>");
            int count = 0;
            int countDoneTask = 0;
            for (Task task : tasks) {
                if (task.getStatus().equals(status)) {
                    if (status.equals(Task.Status.DONE)){
                        String deadlineStr = task.getDeadline();
                        String dateOfCompletionStr = task.getDateOfCompletion();

                        int deadline = Integer.valueOf(deadlineStr.substring(0, 2))
                                + Integer.valueOf(deadlineStr.substring(3))*1000;

                        int dateOfCompletion = Integer.valueOf(dateOfCompletionStr.substring(0, 2))
                                + Integer.valueOf(dateOfCompletionStr.substring(3))*1000;
                        System.out.print(deadline + " " + dateOfCompletion);
                        if (deadline > dateOfCompletion){
                            countDoneTask++;
                        }
                    }
                    count++;
                }
            }
            sb.append(count).append("\n");
            if (status.equals(Task.Status.DONE)){
                sb.append("<b>").append(messageDao.getMessageText(112)).append("</b>").append(countDoneTask).append("\n");
            }
        }

        bot.sendMessage(new SendMessage()
                .setText(sb.toString())
                .setChatId(chatId)
                .setParseMode(ParseMode.HTML));
    }

    private void showUsers(Long chatId, Bot bot) throws SQLException, TelegramApiException {
        StringBuilder sb = new StringBuilder();
        for (User user : users) {
            sb.append(user.toString());
        }
        sendMessage(sb.toString(), chatId, bot);
    }
}
