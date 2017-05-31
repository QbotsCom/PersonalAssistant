package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import com.turlygazhy.entity.User;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.groupadministration.GetChat;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.ChatMember;
import org.telegram.telegrambots.api.objects.Contact;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

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
                    List<User> users = userDao.getUsers();
                    StringBuilder sb = new StringBuilder();
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
                    sendMessage(104, chatId, bot);      // Выберите пользователя
                    StringBuilder sb = new StringBuilder();
                    List<User> users = userDao.getUsers();
                    for (User user : users){
                        sb.append(user.toString());
                    }
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
                Contact contact = updateMessage.getContact();
                if (contact == null) {
                    sendMessage(103, chatId, bot);      // Данный пользоваетль не зарегистрирован в Telegram
                } else {
                    if (userDao.addUser(contact))
                    {
                        sendMessage(6, contact.getUserID(), bot);   // Главное меню для работника
                        sendMessage(102, chatId, bot);      // Сотрудник добавлен
                        waitingType = WaitingType.COMMAND;
                    } else {
                        sendMessage(109, chatId, bot);      // Данный сотрудник уже добавлен
                        waitingType = WaitingType.COMMAND;
                    }
                }
                return false;
            case CHOOSE_USER:
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
