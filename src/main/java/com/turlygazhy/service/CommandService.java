package com.turlygazhy.service;

import com.turlygazhy.command.Command;
import com.turlygazhy.entity.Button;
import com.turlygazhy.exception.CommandNotFoundException;

import java.sql.SQLException;

/**
 * Created by user on 1/2/17.
 */
public class CommandService extends Service {
    public Command getCommand(String text) throws SQLException, CommandNotFoundException {
        Button button = buttonDao.getButton(text);
        return commandDao.getCommand(button.getCommandId());
    }
}
