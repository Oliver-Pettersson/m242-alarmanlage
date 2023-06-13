package ch.alptbz.mqtttelegramdemo.telegram;

import com.pengrad.telegrambot.model.Update;

public interface TelegramSenderInterface {

    public void sendMessage(Long telegramUserId, String message);

    public void sendToAllSubscribers(String message);

    public void sendReply(Update update, String message);
}
