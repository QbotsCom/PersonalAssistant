package com.turlygazhy.command.impl;

import com.turlygazhy.Bot;
import com.turlygazhy.command.Command;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by daniyar on 26.05.17.
 */
public class StartCommand extends Command {
    /*todo удали этот класс
    * вместо этого используй класс ShowInfoCommand
    * как этот класс работает: в button пишешь id command, в command пишешь id message, в message пишешь id keyboard
    * и он все отправляет*/
    @Override
    public boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException {
        initMessage(update, bot);
        sendMessage(5, chatId, bot);
        return true;
    }
}
