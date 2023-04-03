package ch.alptbz.mqtttelegramdemo.mqtt;

import org.eclipse.paho.client.mqttv3.MqttMessage;

public interface MqttConsumerInterface {

    public boolean acceptsTopic(String topic);

    public String[] subscribesTopics();

    public void handleTopic(String topic, String messageStr, MqttMessage message);
}
