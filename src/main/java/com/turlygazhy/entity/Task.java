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
    private String report;
    private String dateOfCompletion;
    private String cause;

    private boolean hasAudio;

    public void setId(int id) {
        this.id = id;
    }

    public enum Status {
        DOING(0),
        DONE(1),
        WAITING_FOR_CONFIRMATION(2),
        REJECTED(3),
        WAITING_ADMIN_CONFIRMATION(4),
        REJECTED_BY_ADMIN(5);
        ;

        private int id;

        Status(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }

        private static Status getStatus(int id) {
            return Status.values()[id];
        }

        public String getStatusString(int id) {
            return getStatusString(Status.getStatus(id));
        }

        public String getStatusString(Status status) {
            try {
                MessageDao messageDao = DaoFactory.getFactory().getMessageDao();
                switch (status) {
                    case WAITING_FOR_CONFIRMATION:
                        return messageDao.getMessageText(87);   // Ожидание подтверждения
                    case WAITING_ADMIN_CONFIRMATION:
                        return messageDao.getMessageText(116);  // Ожидание подтверждения начальника
                    case DOING:
                        return messageDao.getMessageText(110);  // Выполняется
                    case REJECTED:
                        return messageDao.getMessageText(86);   // Отклонено
                    case REJECTED_BY_ADMIN:
                        return messageDao.getMessageText(117);  // Отклонено начальником
                    case DONE:
                        return messageDao.getMessageText(85);   // Выполнено
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return null;
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
                case WAITING_ADMIN_CONFIRMATION:
                    return messageDao.getMessageText(116);  // Ожидание подтверждения начальника
                case REJECTED:
                    return messageDao.getMessageText(95);
                case REJECTED_BY_ADMIN:
                    return messageDao.getMessageText(117);  // Отклонено начальником
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

    public String getReport() {
        return report;
    }

    public void setReport(String report) {
        this.report = report;
    }


    public String getDateOfCompletion() {
        return dateOfCompletion;
    }

    public void setDateOfCompletion(String dateOfCompletion) {
        this.dateOfCompletion = dateOfCompletion;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        MessageDao messageDao = DaoFactory.getFactory().getMessageDao();

        try {
            User user = DaoFactory.getFactory().getUserDao().getUserByChatId(this.userId);
            if (Objects.equals(user.getChatId(), this.getUserId())) {
                if (!this.isHasAudio()) {
                    sb.append("<b>").append(messageDao.getMessageText(96)).append("</b>\n").append(this.getText()).append("\n\n");
                }
                sb.append("<b>").append(messageDao.getMessageText(97)).append("</b>\n").append(user.getName()).append("\n\n")           // Ответственный
                        .append("<b>").append(messageDao.getMessageText(98)).append("</b>\n").append(this.getDeadline()).append("\n\n") // Дедлайн
                        .append("<b>").append(messageDao.getMessageText(99)).append("</b>\n").append(this.getStatusString()).append("\n\n");           // Статус
                if (status.equals(Status.WAITING_ADMIN_CONFIRMATION) || status.equals(Status.DONE)) {
                    sb.append("<b>").append(messageDao.getMessageText(106)).append("</b>\n").append(report).append("\n\n");               // Отчет
                    sb.append("<b>").append(messageDao.getMessageText(108)).append("</b>\n").append(dateOfCompletion).append("\n");     // Закончен
                } else if (status.equals(Status.REJECTED) || status.equals(Status.REJECTED_BY_ADMIN)) {
                    sb.append("<b>").append(messageDao.getMessageText(115)).append("</b>\n").append(cause);
                }

                return sb.toString();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}

