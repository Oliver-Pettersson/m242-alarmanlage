package ch.alptbz.mqtttelegramdemo.telegram;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TelegramNotificationBot
    extends Thread implements UpdatesListener, TelegramSenderInterface {

  private final TelegramBot bot;
  private final List<Long> users = Collections.synchronizedList(new ArrayList<Long>());

  private final ArrayList<TelegramConsumerInterface> consumers = new ArrayList<>();

  public TelegramNotificationBot(String botToken) {
    bot = new TelegramBot(botToken);

    bot.setUpdatesListener(this);


  }

  public void addHandler(TelegramConsumerInterface consumer) {
    consumers.add(consumer);
  }

  @Override
  public int process(List<Update> updates) {
    for (Update update : updates) {
      if (update.message() == null || update.message().text() == null) {
        continue;
      }
      String message = update.message().text();
      if (message.startsWith("/help")) {
        SendMessage reply = new SendMessage(update.message().chat().id(),
            """
                Use /subscribe to get alarm activity updates.\040
                Use /unsubscribe to stop getting alarm activity updates.\040
                Use /alarm-reset to reset the alarm.\040
                Use /alarm-shutdown to shutdown the alarm system.\040
                Use /alarm-activate to activate the alarm system.\040
                Use /alarm-activity to check if the alarm is active.\040
                Use /alarm-triggered to check if the sensor is detecting motion.
                """);
        bot.execute(reply);
      } else if(message.startsWith("/subscribe")) {
        if(!users.contains(update.message().chat().id())) {
          users.add(update.message().chat().id());
          SendMessage reply = new SendMessage(update.message().chat().id(),
              "Welcome! Use /unsubscribe to stop getting alarm activity updates.");
          bot.execute(reply);
        }else{
          SendMessage reply = new SendMessage(update.message().chat().id(),
              "You are already subscribed to the alarm activity updates!");
          bot.execute(reply);
        }
      }
      else if(message.startsWith("/unsubscribe")) {
        if(users.contains(update.message().chat().id())) {
          users.remove(update.message().chat().id());
          SendMessage reply = new SendMessage(update.message().chat().id(),
              "You will no longer receive alarm activity updates.");
          bot.execute(reply);
        }else{
          SendMessage reply = new SendMessage(update.message().chat().id(),
              "You cannot unsubscribe something you've never subscribed to.");
          bot.execute(reply);
        }
      }
      for (TelegramConsumerInterface consumer : consumers) {
        if (consumer.acceptsCommand(message)) {
          consumer.handleCommand(update, message);
        }
      }
    }

    return UpdatesListener.CONFIRMED_UPDATES_ALL;
  }

  @Override
  public void sendMessage(Long telegramUserId, String message) {
    SendMessage newMessage = new SendMessage(telegramUserId, message);
    bot.execute(newMessage);
  }

  @Override
  public void sendToAllSubscribers(String message) {
    for (Long user : users) {
      SendMessage reply = new SendMessage(user, message);
      bot.execute(reply);
    }
  }

  @Override
  public void sendReply(Update update, String message) {
    SendMessage reply = new SendMessage(update.message().chat().id(), message);
    bot.execute(reply);
  }
}
