package com.turlygazhy.command;

import com.turlygazhy.exception.NotRealizedMethodException;

/**
 * Created by user on 1/1/17.
 */
public enum CommandType {
    SHOW_INFO(1),
    ADD_NEW_TASK(49),
    SHOW_TASKS(51),
    SHOW_WORKERS_MENU(52),
    SHOW_STATISTIC(53),
    ACCEPT_OR_REJECT_TASK(54),
    ADMIN_ACCEPT_OR_REJECT_TASK(55);

    private final int id;

    CommandType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static CommandType getType(long id) {
        for (CommandType type : values()) {
            if (type.getId() == id) {
                return type;
            }
        }
        throw new NotRealizedMethodException("There are no type for id: " + id);
    }
}
