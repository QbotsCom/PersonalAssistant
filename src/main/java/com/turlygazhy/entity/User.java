package com.turlygazhy.entity;

/**
 * Created by daniyar on 28.05.17.
 */
public class User {
    int id;
    private Long chatId;
    private String name;
    private Long addydBy;

    public Long getAddydBy() {
        return addydBy;
    }

    public void setAddydBy(Long addydBy) {
        this.addydBy = addydBy;
    }

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

    @Override
    public String toString() {
        return "/id" + id + " - " + name + "\n";
    }
}
