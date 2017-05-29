package com.turlygazhy.entity;

import com.turlygazhy.dao.DaoFactory;
import com.turlygazhy.dao.impl.MessageDao;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

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
    private String voiceMessageId;
    private boolean hasAudio;

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
    }

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

    public String getStatusString() {
        try {
            MessageDao messageDao = DaoFactory.getFactory().getMessageDao();
            switch (status) {
                case DOING:
                    return messageDao.getMessageText(92);
                case DONE:
                    return messageDao.getMessageText(93);
                case WAITING_FOR_CONFIRMATION:
                    return messageDao.getMessageText(94);
                case REJECTED:
                    return messageDao.getMessageText(95);
            }
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
        return null;
    }

    public void setStatus(int status) {
        for (Status type : Status.values()) {
            if (type.getId() == status) {
                this.status = type;
                return;
            }
        }
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public boolean isHasAudio() {
        return hasAudio;
    }

    public void setHasAudio(boolean hasAudio) {
        this.hasAudio = hasAudio;
    }


    public void setVoiceMessageId(String audioId) {
        this.voiceMessageId = audioId;
    }

    public String getVoiceMessageId() {
        return voiceMessageId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        MessageDao messageDao = DaoFactory.getFactory().getMessageDao();
        List<User> users;

        try {
            users = DaoFactory.getFactory().getUserDao().getUsers();
            for (User user : users) {
                if (Objects.equals(user.getChatId(), this.getUserId())) {
                    if (!this.isHasAudio()) {
                        sb.append("<b>").append(messageDao.getMessageText(96)).append("</b>\n").append(this.getText()).append("\n\n");
                    }
                    sb.append("<b>").append(messageDao.getMessageText(97)).append("</b>\n").append(user.getName()).append("\n\n")           // Ответственный
                            .append("<b>").append(messageDao.getMessageText(98)).append("</b>\n").append(this.getDeadline()).append("\n\n") // Дедлайн
                            .append("<b>").append(messageDao.getMessageText(99)).append("</b>\n").append(this.getStatusString());           // Статус
                    return sb.toString();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}

