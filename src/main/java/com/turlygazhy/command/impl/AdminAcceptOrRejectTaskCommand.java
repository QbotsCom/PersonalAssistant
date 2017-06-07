package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by lol on 07.06.2017.
 */
public class AdminAcceptOrRejectTaskCommand extends Command {
    Task task;
    public AdminAcceptOrRejectTaskCommand() throws SQLException {
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        initMessage(update, bot);
        if (task == null) {
            String data = update.getCallbackQuery().getData();
            int id = Integer.parseInt(data.substring(data.indexOf(" ") + 1));
            task = taskDao.getTask(id);
            updateMessageText = data.substring(0, data.indexOf(" "));
        }

        if (updateMessageText.equals(buttonDao.getButtonText(67))) {     // Принять
            task.setStatus(Task.Status.DONE);
            taskDao.updateTask(task);
            sendMessage("OK", task.getAddedByUserId(), bot);
            return true;
        }

        if (updateMessageText.equals(buttonDao.getButtonText(68))) {     // Отклонить
            sendMessage(118, chatId, bot);
            waitingType = WaitingType.CAUSE;
            return false;
        }

        switch (waitingType) {
            case CAUSE:
                task.setStatus(Task.Status.REJECTED_BY_ADMIN);
                task.setCause(updateMessageText);
                taskDao.updateTask(task);
                bot.sendMessage(new SendMessage()
                        .setText(task.toString())
                        .setChatId(task.getUserId())
                        .setParseMode(ParseMode.HTML));
                sendMessage("OK", chatId, bot);
                return true;
        }

        return false;
    }
}
