package ch.alptbz.mqtttelegramdemo.handlers;

import ch.alptbz.mqtttelegramdemo.mqtt.MqttConsumerInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramConsumerInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramSenderInterface;
import com.pengrad.telegrambot.model.Update;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class TemperatureHandler
implements MqttConsumerInterface, TelegramConsumerInterface
{
    private String mqttRoot;
    private TelegramSenderInterface telegramSend;

    double lastTemperature;

    public static final String TEMP_TOPIC = "temp";

    public TemperatureHandler(String mqttRoot, TelegramSenderInterface telegramSend) {
        this.mqttRoot = mqttRoot;

        this.telegramSend = telegramSend;
    }

    @Override
    public boolean acceptsTopic(String topic) {
        return (topic.equals(mqttRoot + "/" + TEMP_TOPIC));
    }

    @Override
    public String[] subscribesTopics() {
        return new String[] {mqttRoot + "/#"};
    }

    @Override
    public void handleTopic(String topic, String messageStr, MqttMessage message) {
        if (topic.endsWith(TEMP_TOPIC)) {
            double temperature = Double.parseDouble(messageStr);
            if (Math.abs(lastTemperature - temperature) >= 1) {
                System.out.println("Temperature changed. Current: %.2f".formatted(temperature));
                telegramSend.sendToAllSubscribers("The temperature has changed to %.2f".formatted(temperature));
                lastTemperature = temperature;
            }
        }
    }

    @Override
    public boolean acceptsCommand(String command) {
        return command.startsWith("/temp");
    }

    @Override
    public void handleCommand(Update update, String message) {
        if(message.startsWith("/temp")) {
            telegramSend.sendReply(update, "Current temperature: %.2f".formatted(lastTemperature));
        }
    }
}
