package com.turlygazhy.entity;

/**
 * Created by daniyar on 28.05.17.
 */
public class User {
    int id;
    Long chatId;
    String name;

    public int getId() {
        return id;
    }

    public Long getChatId() {
        return chatId;
    }

    public String getName() {
        return name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public void setName(String name) {
        this.name = name;
    }
}
