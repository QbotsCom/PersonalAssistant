package com.turlygazhy;

import com.turlygazhy.command.Command;
import com.turlygazhy.command.impl.ShowInfoCommand;
import com.turlygazhy.dao.DaoFactory;
import com.turlygazhy.dao.impl.ButtonDao;
import com.turlygazhy.dao.impl.KeyboardMarkUpDao;
import com.turlygazhy.dao.impl.MessageDao;
import com.turlygazhy.entity.Message;
import com.turlygazhy.entity.WaitingType;
import com.turlygazhy.exception.CommandNotFoundException;
import com.turlygazhy.service.CommandService;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;

/**
 * Created by Yerassyl_Turlygazhy on 11/27/2016.
 */
public class Conversation {
    private CommandService commandService = new CommandService();
    private Command command;

    public void handleUpdate(Update update, Bot bot) throws SQLException, TelegramApiException {
        org.telegram.telegrambots.api.objects.Message updateMessage = update.getMessage();
        String inputtedText;
        if (updateMessage == null) {
            inputtedText = update.getCallbackQuery().getData();
            updateMessage = update.getCallbackQuery().getMessage();
        } else {
            inputtedText = updateMessage.getText();
        }

        try {
            command = commandService.getCommand(inputtedText);
        } catch (CommandNotFoundException e) {
            if (updateMessage.isGroupMessage()) {
                return;
            }
            if (command == null) {
                ShowInfoCommand showInfoCommand = new ShowInfoCommand();
                int cannotHandleUpdateMessageId = 7;
                showInfoCommand.setMessageId(cannotHandleUpdateMessageId);
                showInfoCommand.execute(update, bot);
                return;
            }
        }
        boolean commandFinished = command.execute(update, bot);
        if (commandFinished) {
            command = null;
        }
    }
}
