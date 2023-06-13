package ch.alptbz.mqtttelegramdemo.telegram;

import com.pengrad.telegrambot.model.Update;

public interface TelegramConsumerInterface {

    public boolean acceptsCommand(String command);

    public void handleCommand(Update update, String message);

}
