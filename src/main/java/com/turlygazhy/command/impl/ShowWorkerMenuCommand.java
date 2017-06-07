package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.User;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Contact;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.List;

/**
 * Created by daniyar on 29.05.17.
 */

public class ShowWorkerMenuCommand extends Command {
    public ShowWorkerMenuCommand() throws SQLException {
    }

    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        initMessage(update, bot);
        if (waitingType == null) {
            sendMessage(100, chatId, bot);
            waitingType = WaitingType.COMMAND;
        }

        switch (waitingType) {
            case COMMAND:
                if (updateMessageText.equals(buttonDao.getButtonText(57))) {     // Добавить сотрудника
                    sendMessage(101, chatId, bot);      // Введите номер
                    waitingType = WaitingType.PHONE_NUMBER;
                    return false;
                }

                if (updateMessageText.equals(buttonDao.getButtonText(58))) {      // Список сотрудников
                    List<User> users = userDao.getUsers(chatId);
                    StringBuilder sb = new StringBuilder();
                    if (users.size() == 0) {
                        sendMessage(10, chatId, bot);
                        return false;
                    }
                    for (User user : users) {
//                        sb.append("<b>").append(user.getName()).append("</b>\n");
                        sb.append(user.getName()).append("\n");
                    }
                    bot.sendMessage(new SendMessage()
                            .setText(sb.toString())
                            .setChatId(chatId)
                            .setParseMode(ParseMode.HTML));
                }

                if (updateMessageText.equals(buttonDao.getButtonText(59))) {      // Удалить сотрудника
                    StringBuilder sb = new StringBuilder();
                    List<User> users = userDao.getUsers(chatId);
                    if (users.size() == 0) {
                        sendMessage(10, chatId, bot);
                        return false;
                    }
                    for (User user : users) {
                        sb.append(user.toString());
                    }
                    sendMessage(104, chatId, bot);      // Выберите пользователя
                    sendMessage(sb.toString(), chatId, bot);
                    waitingType = WaitingType.CHOOSE_USER;
                }

                if (updateMessageText.equals(buttonDao.getButtonText(10))) {     // Назад
                    sendMessage(5, chatId, bot);        // Главное меню
                    waitingType = null;
                    return true;
                }
                return false;

            case PHONE_NUMBER:
                if (updateMessageText != null) {
                    if (updateMessageText.equals(buttonDao.getButtonText(10))) {
                        sendMessage(100, chatId, bot);
                        waitingType = WaitingType.COMMAND;
                        return false;
                    }
                }
                Contact contact = updateMessage.getContact();
                if (contact == null) {
                    sendMessage(103, chatId, bot);      // Данный пользоваетль не зарегистрирован в Telegram
                } else {
                    if (userDao.addUser(contact, chatId)) {
                        sendMessage(5, contact.getUserID(), bot);   // Главное меню для работника
                        sendMessage(102, chatId, bot);      // Сотрудник добавлен
                        waitingType = WaitingType.COMMAND;
                    } else {
                        sendMessage(109, chatId, bot);      // Данный сотрудник уже добавлен
                    }
                }
                waitingType = WaitingType.COMMAND;
                return false;
            case CHOOSE_USER:
                if (updateMessageText.equals(buttonDao.getButtonText(10))) {
                    sendMessage(100, chatId, bot);
                    waitingType = WaitingType.COMMAND;
                    return false;
                }
                int userId = Integer.valueOf(updateMessageText.substring(3));
                System.out.println(userId);
                userDao.deleteUser(userId);
                sendMessage(105, chatId, bot);
                waitingType = WaitingType.COMMAND;
                return false;
        }
        return true;
    }
}
