package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.User;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendVideo;
import org.telegram.telegrambots.api.methods.send.SendVoice;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AddNewTaskCommand extends Command {
    private Task task;
    private int shownDates = 0;
    private List<User> users;

    public AddNewTaskCommand() throws SQLException {
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        initMessage(update, bot);
        if (waitingType == null) {
            return addNewTask(bot);
        }

        switch (waitingType) {
            case TASK_WORKER:
                if (updateMessageText.equals(buttonDao.getButtonText(10))) {
                    sendMessage(5, chatId, bot);
                    return true;
                }
                return chooseTaskWorker(bot);

            case TASK_TEXT:
                if (updateMessageText != null) {
                    if (updateMessageText.equals(buttonDao.getButtonText(10))) {
                        waitingType = WaitingType.TASK_WORKER;
                        return addNewTask(bot);
                    }
                }
                return setTaskText(bot);

            case TASK_DEADLINE:
                if (updateMessageText.equals(buttonDao.getButtonText(10))) {
                    updateMessageText = "";
                    waitingType = WaitingType.TASK_TEXT;
                    return chooseTaskWorker(bot);
                }
                return setTaskDeadline(bot);

        }
        return false;
    }

    private boolean addNewTask(Bot bot) throws SQLException, TelegramApiException {
        waitingType = WaitingType.TASK_WORKER;
        sendMessage(78, chatId, bot); // Выберите работника
        users = userDao.getUsers(chatId);

        StringBuilder sb = new StringBuilder();
        for (User user : users) {
            sb.append(user.toString());
        }

        sendMessage(sb.toString(), chatId, bot);
        task = new Task(chatId);
        return false;
    }

    private boolean chooseTaskWorker(Bot bot) throws SQLException, TelegramApiException {
        users = userDao.getUsers(chatId);
        if (task.getUserId() == null) {
            Long taskWorker = userDao.getChatIdByUserId(Long.valueOf(updateMessageText.substring(3)));
            task.setUserId(taskWorker);
        }
        waitingType = WaitingType.TASK_TEXT;
        sendMessage(76, chatId, bot); // Опишите задание
        return false;
    }

    private boolean setTaskText(Bot bot) throws SQLException, TelegramApiException {
        if (updateMessage.getVoice() == null) {
            task.setText(updateMessageText);
            task.setHasAudio(false);
        } else {
            task.setHasAudio(true);
            task.setVoiceMessageId(updateMessage.getVoice().getFileId());
        }
        sendMessage(77, getDeadlineKeyboard(shownDates)); // Введите дедлайн
        waitingType = WaitingType.TASK_DEADLINE;
        return false;
    }

    private boolean setTaskDeadline(Bot bot) throws SQLException, TelegramApiException {
        if (updateMessageText.equals(nextText)) {
            shownDates++;
            bot.editMessageText(new EditMessageText()
                    .setMessageId(updateMessage.getMessageId())
                    .setChatId(chatId)
                    .setText(messageDao.getMessageText(77)) // Введите дедлайн
                    .setReplyMarkup(getDeadlineKeyboard(shownDates))
            );
            return false;
        }

        if (updateMessageText.equals(prevText)) {
            shownDates--;
            bot.editMessageText(new EditMessageText()
                    .setMessageId(updateMessage.getMessageId())
                    .setChatId(chatId)
                    .setText(messageDao.getMessageText(77)) // Введите дедлайн
                    .setReplyMarkup(getDeadlineKeyboard(shownDates))
            );
            return false;
        }

        task.setDeadline(updateMessageText);
        waitingType = null;
        taskDao.insertTask(task);
        informExecutor(bot);
        sendMessage(79, chatId, bot); // Задание записано
        return true;
    }

    private InlineKeyboardMarkup getDeadlineKeyboard(int shownDates) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        Date date = new Date();
        date.setDate(date.getDate() + (shownDates * 9));
        List<InlineKeyboardButton> row = null;
        for (int i = 1; i < 10; i++) {
            if (row == null) {
                row = new ArrayList<>();
            }
            InlineKeyboardButton button = new InlineKeyboardButton();
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
            String dateText = stringDate + "." + stringMonth;
            button.setText(dateText);
            button.setCallbackData(dateText);
            row.add(button);
            if (i % 3 == 0) {
                rows.add(row);
                row = null;
            }
            date.setDate(date.getDate() + 1);
        }

        if (shownDates > 0) {
            rows.add(getNextPrevRows(true, true));
        } else {
            rows.add(getNextPrevRows(false, true));
        }


        keyboard.setKeyboard(rows);
        return keyboard;
    }

    private void informExecutor(Bot bot) throws SQLException, TelegramApiException { //передача задания
        StringBuilder sb = new StringBuilder();
        sendMessage(80, task.getUserId(),bot);
        if (task.isHasAudio()) {
            bot.sendVoice(new SendVoice()
                    .setVoice(task.getVoiceMessageId())
                    .setChatId(task.getUserId()));
        } else {
            sb.append("<b>").append(messageDao.getMessageText(96)).append("</b>").append(task.getText()).append("\n");
        }
        sb.append("<b>").append(messageDao.getMessageText(98)).append("</b>").append(task.getDeadline());
        bot.sendMessage(new SendMessage()
                .setChatId(task.getUserId())
                .setText(sb.toString())
                .setReplyMarkup(getTaskKeyboard())
                .setParseMode(ParseMode.HTML));
    }

    private InlineKeyboardMarkup getTaskKeyboard() throws SQLException {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(new InlineKeyboardButton()
                .setText(buttonDao.getButtonText(65))   // Accept
                .setCallbackData(buttonDao.getButtonText(65) + " " + task.getId()));
        row.add(new InlineKeyboardButton()
                .setText(buttonDao.getButtonText(66))   // Reject
                .setCallbackData(buttonDao.getButtonText(66) + " " + task.getId()));

        rows.add(row);
        keyboard.setKeyboard(rows);

        return keyboard;
    }
}
