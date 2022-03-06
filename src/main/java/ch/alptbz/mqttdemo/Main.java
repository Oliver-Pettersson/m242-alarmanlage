package ch.alptbz.mqttdemo;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.function.BiConsumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    public static double temperature = 0;
    public static double humidity = 0;

    public final static void main(String[] args) throws InterruptedException {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        Logger.getGlobal().addHandler(ch);

        Mqtt mqttClient = new Mqtt("tcp://cloud.tbz.ch:1883", "runner-1");
        try {
            mqttClient.start();
            mqttClient.subscribe("m5core2/#");
            mqttClient.publish("M5Stack", "test");
        } catch (MqttException e) {
            e.printStackTrace();
        }

        mqttClient.addHandler(new BiConsumer<String, MqttMessage>() {
            @Override
            public void accept(String s, MqttMessage mqttMessage) {
                if(s.equals("m5core2/temp")) {
                    temperature = Double.parseDouble(mqttMessage.toString());
                }
                if(s.equals("m5core2/hum")) {
                    humidity = Double.parseDouble(mqttMessage.toString());
                }
            }
        });

        double lastTemperature = temperature;

        while(true) {
            if(Math.abs(lastTemperature - temperature) > 2)  {
                System.out.println("Temperature changed. Current: %.2f".formatted(temperature));
                if(temperature > 24) {
                    System.out.println("Temperature too high!");
                }
                lastTemperature = temperature;
            }
            Thread.sleep(1000);
        }

    }

}
