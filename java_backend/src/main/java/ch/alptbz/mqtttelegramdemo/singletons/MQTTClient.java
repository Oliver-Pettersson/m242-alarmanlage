package ch.alptbz.mqtttelegramdemo.singletons;

import static ch.alptbz.mqtttelegramdemo.singletons.Config.getConfig;
import static ch.alptbz.mqtttelegramdemo.singletons.Logger.getLogger;

import ch.alptbz.mqtttelegramdemo.mqtt.Mqtt;
import java.util.logging.Level;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MQTTClient {
  private static Mqtt mqttClientInstance;

  private MQTTClient() {}

  private static void initClient() {
    mqttClientInstance = new Mqtt(getConfig().getProperty("mqtt-url"), getConfig().getProperty("mqtt-client"));
    try {
      mqttClientInstance.start();
    } catch (MqttException e) {
      getLogger().log(Level.SEVERE, "Failed to start MQTT Client", e);
    }
  }

  public static Mqtt getMqttClient() {
    if (mqttClientInstance == null) {
      initClient();
    }
    return mqttClientInstance;
  }

}
