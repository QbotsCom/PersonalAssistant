package com.turlygazhy.command;

import com.turlygazhy.command.impl.AddNewTaskCommand;
import com.turlygazhy.command.impl.ShowTasksCommand;
import com.turlygazhy.exception.NotRealizedMethodException;

/**
 * Created by user on 1/2/17.
 */
public class CommandFactory {
    public static Command getCommand(long id) {
        CommandType type = CommandType.getType(id);
        switch (type) {
            case ADD_NEW_TASK:
                return new AddNewTaskCommand();
            case SHOW_TASKS:
                return new ShowTasksCommand();
            default:
                throw new NotRealizedMethodException("Not realized for type: " + type);
        }
    }
}
