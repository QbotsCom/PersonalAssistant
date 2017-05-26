package com.turlygazhy.entity;

import java.util.Date;

/**
 * Created by lol on 25.05.2017.
 */

public class Task {
    private int id;
    private String text;
    private String deadline;
    private Long userId;
    private Long addedByUserId;
    private Status status;

    public enum Status {
        DOING(0),
        DONE(1),
        WAITING_FOR_CONFIRMATION(2),
        REJECTED(3);

        private int id;

        Status(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    };

    public Task() {
    }

    public Task(int id) {
        this.id = id;
    }

    public Task(Long addedByUserId) {
        this.addedByUserId = addedByUserId;
    }

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAddedByUserId() {
        return addedByUserId;
    }

    public void setAddedByUserId(long addedByUserId) {
        this.addedByUserId = addedByUserId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(int status) {
        for (Status type : Status.values()){
            if (type.getId() == status){
                this.status = type;
                return;
            }
        }
    }

    public void setStatus(Status status){
        this.status = status;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.text).append("\n")
                .append(deadline).append("\n")
                .append(status).append("\n");

        return sb.toString();
    }
}
