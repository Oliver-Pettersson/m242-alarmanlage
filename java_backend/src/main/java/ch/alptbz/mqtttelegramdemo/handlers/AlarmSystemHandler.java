package ch.alptbz.mqtttelegramdemo.handlers;

import static ch.alptbz.mqtttelegramdemo.singletons.Config.getConfig;
import static ch.alptbz.mqtttelegramdemo.singletons.Logger.getLogger;
import static ch.alptbz.mqtttelegramdemo.singletons.MQTTClient.getMqttClient;

import ch.alptbz.mqtttelegramdemo.logdata.LogData;
import ch.alptbz.mqtttelegramdemo.logdata.LogDataRepository;
import ch.alptbz.mqtttelegramdemo.mqtt.MqttConsumerInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramConsumerInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramSenderInterface;
import com.pengrad.telegrambot.model.Update;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class AlarmSystemHandler implements MqttConsumerInterface, TelegramConsumerInterface {

  private final String mqttRootTopic;
  private final String mqttActivityTopic;
  private final String mqttTriggeredTopic;
  private boolean lastRegisteredActivityStatus;
  private boolean lastRegisteredTriggeredStatus;
  private final TelegramSenderInterface telegramSend;
  private final ScheduledExecutorService executor;
  private static final String DELAYED_START_STRING = "/alarm-delayed-start";

  public AlarmSystemHandler(TelegramSenderInterface telegramSend) {
    this.mqttRootTopic = getConfig().getProperty("mqtt-root");
    this.mqttActivityTopic = getConfig().getProperty("mqtt-activity-topic");
    this.mqttTriggeredTopic = getConfig().getProperty("mqtt-triggered-topic");
    this.lastRegisteredTriggeredStatus = false;
    this.lastRegisteredActivityStatus = false;
    this.telegramSend = telegramSend;
    this.executor = Executors.newSingleThreadScheduledExecutor();
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
      String msg;
      if (isActive) {
        msg = "The alarm system has been turned on";
      } else {
        msg = "The alarm system has been turned off";
      }
      LogDataRepository.getRepository().saveLog(msg);
      telegramSend.sendToAllSubscribers(msg);
      lastRegisteredActivityStatus = isActive;
    }
    if (topic.endsWith(mqttTriggeredTopic)) {
      lastRegisteredTriggeredStatus = Boolean.parseBoolean(messageStr);
      String msg;
      if (lastRegisteredTriggeredStatus) {
        msg = "The alarm has been triggered";
      } else {
        msg = "The alarm has been reset";
      }
      telegramSend.sendToAllSubscribers(msg);
      LogDataRepository.getRepository().saveLog(msg);
    }
  }

  @Override
  public boolean acceptsCommand(String command) {
    return command.startsWith("/alarm");
  }

  @Override
  public void handleCommand(Update update, String message) {
    try {
      if (message.startsWith("/alarm-reset")) {
        telegramSend.sendReply(update, "resetting alarm...");
        getMqttClient().publish(mqttRootTopic + mqttTriggeredTopic, "false");
      } else if (message.startsWith("/alarm-triggered")) {
        if (lastRegisteredTriggeredStatus) {
          telegramSend.sendReply(update, "Sensor is detecting motion");
        } else {
          telegramSend.sendReply(update, "Sensor is not detecting motion");
        }
      } else if (message.startsWith("/alarm-logs")) {
        StringBuilder msg = new StringBuilder();
        List<LogData> logDataList = LogDataRepository.getRepository().getLogs().stream()
            .sorted(Comparator.comparing(LogData::getDateTime)).toList();
        for (LogData logData : logDataList) {
          LocalDateTime dateTime = logData.getDateTime();
          msg.append(dateTime.getYear())
              .append("-")
              .append(dateTime.getMonth())
              .append("-")
              .append(dateTime.getDayOfMonth())
              .append(" ")
              .append(dateTime.getHour())
              .append(":")
              .append(dateTime.getMinute())
              .append(" ")
              .append(logData.getMessage())
              .append("\n");
        }
        telegramSend.sendReply(update, msg.toString());
      } else if (lastRegisteredTriggeredStatus) {
        telegramSend.sendReply(update,
            "The alarm is currently being triggered, please resolve this issue before interacting with it further.");
      } else if (message.startsWith("/alarm-shutdown")) {
        telegramSend.sendReply(update, "initiating shutdown...");
        getMqttClient().publish(mqttRootTopic + mqttActivityTopic, "false");
      } else if (message.startsWith("/alarm-activate")) {
        telegramSend.sendReply(update, "starting alarm...");
        getMqttClient().publish(mqttRootTopic + mqttActivityTopic, "true");
      } else if (message.startsWith(DELAYED_START_STRING)) {
        int numberOfSeconds = Integer.parseInt(message.substring(
            message.indexOf(DELAYED_START_STRING) + DELAYED_START_STRING.length() +
                1)); //added +1 to skip space
        telegramSend.sendReply(update, "starting alarm after " + numberOfSeconds + " seconds");
        executor.schedule(() -> {
          telegramSend.sendReply(update, "starting alarm...");
          try {
            getMqttClient().publish(mqttRootTopic + mqttActivityTopic, "true");
          } catch (MqttException e) {
            getLogger().log(Level.SEVERE, "failed sending mqtt message", e);
          }
        }, numberOfSeconds, TimeUnit.SECONDS);
      } else if (message.startsWith("/alarm-activity")) {
        telegramSend.sendReply(update,
            lastRegisteredActivityStatus ? "alarm is active" : "alarm is inactive");
      }
    } catch (MqttException e) {
      getLogger().log(Level.SEVERE, "failed sending mqtt message", e);
    }
  }
}
