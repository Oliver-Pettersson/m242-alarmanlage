package ch.alptbz.mqtttelegramdemo.handlers;

import static ch.alptbz.mqtttelegramdemo.singletons.Config.getConfig;
import static ch.alptbz.mqtttelegramdemo.singletons.Logger.getLogger;
import static ch.alptbz.mqtttelegramdemo.singletons.MQTTClient.getMqttClient;

import ch.alptbz.mqtttelegramdemo.mqtt.MqttConsumerInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramConsumerInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramSenderInterface;
import com.pengrad.telegrambot.model.Update;
import java.util.logging.Level;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class AlarmSystemHandler implements MqttConsumerInterface, TelegramConsumerInterface {

  private final String mqttRootTopic;
  private final String mqttActivityTopic;
  private boolean lastRegisteredActivityStatus;
  private TelegramSenderInterface telegramSend;

  public AlarmSystemHandler(TelegramSenderInterface telegramSend) {
    this.mqttRootTopic = getConfig().getProperty("mqtt-root");
    this.mqttActivityTopic = getConfig().getProperty("mqtt-activity-topic");
    this.telegramSend = telegramSend;
  }

  @Override
  public boolean acceptsTopic(String topic) {
    return (topic.startsWith(mqttRootTopic));
  }

  @Override
  public String[] subscribesTopics() {
    return new String[] {mqttRootTopic + "/#"};
  }

  @Override
  public void handleTopic(String topic, String messageStr, MqttMessage message) {
    if (topic.endsWith(mqttActivityTopic)) {
      boolean isActive = Boolean.parseBoolean(messageStr);
      if (isActive) {
        telegramSend.sendToAllSubscribers("The alarm system has been turned on");
      } else {
        telegramSend.sendToAllSubscribers("The alarm system has been turned off");
      }
      lastRegisteredActivityStatus = isActive;
    }

  }

  @Override
  public boolean acceptsCommand(String command) {
    return command.startsWith("/alarm");
  }

  @Override
  public void handleCommand(Update update, String message) {
    try {
      if (message.startsWith("/alarm-shutdown")) {
        telegramSend.sendReply(update, "initiating shutdown...");
        getMqttClient().publish(mqttRootTopic + mqttActivityTopic, "false");
      }
      if (message.startsWith("/alarm-activate")) {
        telegramSend.sendReply(update, "starting alarm...");
        getMqttClient().publish(mqttRootTopic + mqttActivityTopic, "true");
      }
      if (message.startsWith("/alarm-activity")) {
        telegramSend.sendReply(update,
            lastRegisteredActivityStatus ? "alarm is active" : "alarm is inactive");
      }
    } catch (MqttException e) {
      getLogger().log(Level.SEVERE, "failed sending mqtt message", e);
    }
  }
}
