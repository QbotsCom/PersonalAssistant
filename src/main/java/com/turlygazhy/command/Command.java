package com.turlygazhy.command;

import com.turlygazhy.Bot;
import com.turlygazhy.dao.DaoFactory;
import com.turlygazhy.dao.GoalDao;
import com.turlygazhy.dao.impl.*;
import com.turlygazhy.entity.Message;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendContact;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Contact;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yerassyl_Turlygazhy on 11/27/2016.
 */
public abstract class Command {
    protected long id;
    protected long messageId;

    protected DaoFactory factory = DaoFactory.getFactory();
    protected UserDao userDao = factory.getUserDao();
    protected MessageDao messageDao = factory.getMessageDao();
    protected KeyboardMarkUpDao keyboardMarkUpDao = factory.getKeyboardMarkUpDao();
    protected ButtonDao buttonDao = factory.getButtonDao();
    protected CommandDao commandDao = factory.getCommandDao();
    protected ConstDao constDao = factory.getConstDao();
    protected MemberDao memberDao = factory.getMemberDao();
    protected KeyWordDao keyWordDao = factory.getKeyWordDao();
    protected ReservationDao reservationDao = factory.getReservationDao();
    protected GroupDao groupDao = factory.getGroupDao();
    protected GoalDao goalDao = factory.getGoalDao();
    protected ThesisDao thesisDao = factory.getThesisDao();
    protected SavedResultsDao savedResultsDao = factory.getSavedResultsDao();
    protected TaskDao taskDao = factory.getTaskDao();

    protected WaitingType waitingType;
    protected org.telegram.telegrambots.api.objects.Message updateMessage;
    protected String updateMessageText;
    protected Long chatId;
    private Bot bot;

    public void initMessage(Update update, Bot bot) throws TelegramApiException, SQLException {
        this.bot = bot;
        updateMessage = update.getMessage();
        if (updateMessage == null) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            updateMessage = callbackQuery.getMessage();
            updateMessageText = callbackQuery.getData();
            String waitText = messageDao.getMessageText(208);
            if (chatId == null) {
                chatId = updateMessage.getChatId();
            }
            bot.editMessageText(new EditMessageText()
                    .setText(waitText)
                    .setChatId(chatId)
                    .setMessageId(updateMessage.getMessageId())
            );
        } else {
            updateMessageText = updateMessage.getText();
            if (chatId == null) {
                chatId = updateMessage.getChatId();
            }
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return is command finished
     */
    public abstract boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException;

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void sendMessage(long messageId, long chatId, TelegramLongPollingBot bot) throws SQLException, TelegramApiException {
        sendMessage(messageId, chatId, bot, null);
    }

    public void sendMessage(String text, long chatId, TelegramLongPollingBot bot) throws SQLException, TelegramApiException {
        sendMessage(text, chatId, bot, null);
    }

    public void sendMessage(long messageId, long chatId, TelegramLongPollingBot bot, Contact contact) throws SQLException, TelegramApiException {
        Message message = messageDao.getMessage(messageId);
        SendMessage sendMessage = message.getSendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(keyboardMarkUpDao.select(message.getKeyboardMarkUpId()));
        bot.sendMessage(sendMessage);
        if (contact != null) {
            bot.sendContact(new SendContact()
                    .setChatId(chatId)
                    .setFirstName(contact.getFirstName())
                    .setLastName(contact.getLastName())
                    .setPhoneNumber(contact.getPhoneNumber())
            );
        }
    }

    public void sendMessage(long messageId, int keyboardMarkUpId) throws SQLException, TelegramApiException {
        sendMessage(messageId, keyboardMarkUpDao.select(keyboardMarkUpId));
    }

    public void sendMessage(long messageId, ReplyKeyboard keyboard) throws SQLException, TelegramApiException {
        bot.sendMessage(new SendMessage()
                .setChatId(chatId)
                .setText(messageDao.getMessageText(messageId))
                .setReplyMarkup(keyboard)
                .setParseMode(ParseMode.HTML)
        );
    }

    public void sendMessage(String text, long chatId, TelegramLongPollingBot bot, Contact contact) throws SQLException, TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        bot.sendMessage(sendMessage);
        if (contact != null) {
            bot.sendContact(new SendContact()
                    .setChatId(chatId)
                    .setFirstName(contact.getFirstName())
                    .setLastName(contact.getLastName())
                    .setPhoneNumber(contact.getPhoneNumber())
            );
        }
    }

    public void sendMessageToAdmin(long messageId, TelegramLongPollingBot bot) throws SQLException, TelegramApiException {
        long adminChatId = getAdminChatId();
        sendMessage(messageId, adminChatId, bot);
    }

    public long getAdminChatId() {
        return userDao.getAdminChatId();
    }

    public void sendMessageToAdmin(long messageId, Bot bot, Contact contact) throws SQLException, TelegramApiException {
        long adminChatId = getAdminChatId();
        sendMessage(messageId, adminChatId, bot, contact);
    }

    public void sendMessageToAdmin(String text, TelegramLongPollingBot bot) throws SQLException, TelegramApiException {
        long adminChatId = getAdminChatId();
        sendMessage(text, adminChatId, bot);
    }

    public void sendPhotoToAdmin(String photo, Bot bot) throws TelegramApiException {
        long adminChatId = getAdminChatId();
        bot.sendPhoto(new SendPhoto()
                .setChatId(adminChatId)
                .setPhoto(photo)
        );
    }

    public boolean validateTime(String theTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm"); //HH = 24h format
        dateFormat.setLenient(false); //this will not enable 25:67 for example
        try {
            dateFormat.parse(theTime);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    protected List<InlineKeyboardButton> getNextPrevRows(boolean prev, boolean next) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        if (prev) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton();
            String prevText = "prev";//todo это должно браться из бд
            prevButton.setText(prevText);
            prevButton.setCallbackData(prevText);
            row.add(prevButton);
        }
        if (next) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            String prevText = "next";//todo это должно браться из бд
            nextButton.setText(prevText);
            nextButton.setCallbackData(prevText);
            row.add(nextButton);
        }

        return row;
    }

}
