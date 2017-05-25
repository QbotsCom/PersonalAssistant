package com.turlygazhy.entity;

import java.util.Date;

/**
 * Created by lol on 25.05.2017.
 */

public class Task {
    private int id;
    private String text;
    private Date deadline;
    private Long userId;
    private Long addedByUserId;
    private Status status;

    private enum Status {
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

    public Task(Long userId) {
        this.id = id;
        this.text = text;
        this.deadline = deadline;
        this.userId = userId;
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

    public Date getDeadline() {
        return deadline;
    }

    public void setDeadline(Date deadline) {
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


    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


}
