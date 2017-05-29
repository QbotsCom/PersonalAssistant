package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.User;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
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
            waitingType = WaitingType.TASK_WORKER;
            sendMessage(78, chatId, bot); // Выберите работника
            users = userDao.getUsers();

            StringBuilder sb = new StringBuilder();
            for (User user : users) {
                sb.append("/id");
                sb.append(user.getName()).append("\n");
            }

            sendMessage(sb.toString(), chatId, bot);
            task = new Task(chatId);
            return false;
        }

        switch (waitingType) {
            case TASK_WORKER:
                users = userDao.getUsers();
                String taskWorker = updateMessageText.substring(3);
                for (User user : users) {
                    if (user.getName().equals(taskWorker)) {
                        task.setUserId(user.getChatId());
                        waitingType = WaitingType.TASK_TEXT;
                        sendMessage(76, chatId, bot); // Опишите задание
                        return false;
                    }
                }
                return false;

            case TASK_TEXT:
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

            case TASK_DEADLINE:
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

                waitingType = WaitingType.COMMAND;
                taskDao.insertTask(task);
                informExecutor(bot, task.getUserId());
                sendMessage(79, chatId, bot); //Задание записано
                return true;

        }
        return false;
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

    private void informExecutor(Bot bot, Long chatId) throws SQLException, TelegramApiException { //передача задания
        sendMessage(80, chatId, bot);
    }
}
