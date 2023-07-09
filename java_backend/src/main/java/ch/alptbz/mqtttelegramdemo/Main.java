package ch.alptbz.mqtttelegramdemo;

import static ch.alptbz.mqtttelegramdemo.singletons.Config.getConfig;
import static ch.alptbz.mqtttelegramdemo.singletons.Logger.getLogger;
import static ch.alptbz.mqtttelegramdemo.singletons.MQTTClient.getMqttClient;

import ch.alptbz.mqtttelegramdemo.handlers.AlarmSystemHandler;
import ch.alptbz.mqtttelegramdemo.handlers.TemperatureHandler;
import ch.alptbz.mqtttelegramdemo.logdata.LogDataRepository;
import ch.alptbz.mqtttelegramdemo.mqtt.MqttConsumerInterface;
import ch.alptbz.mqtttelegramdemo.scheduler.GlobalScheduler;
import ch.alptbz.mqtttelegramdemo.scheduler.RecurringTaskInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramConsumerInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramNotificationBot;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.logging.Level;

public class Main {
    private static TelegramNotificationBot tnb;

    private static void addHandler(Object handler) throws MqttException {
        if(handler instanceof TelegramConsumerInterface) {
            tnb.addHandler((TelegramConsumerInterface)handler);
        }
        if(handler instanceof MqttConsumerInterface) {
            getMqttClient().addHandler((MqttConsumerInterface)handler);
        }
        if(handler instanceof RecurringTaskInterface) {
            GlobalScheduler.main().addTask((RecurringTaskInterface)handler);
        }
    }


    public static void main(String[] args) throws InterruptedException {
        tnb = new TelegramNotificationBot(getConfig().getProperty("telegram-apikey"));
        getLogger().info("TelegramBot started");

        try {
            addHandler(new AlarmSystemHandler(tnb));
        } catch (MqttException e) {
            getLogger().log(Level.SEVERE, "Failed to init handler", e);
        }
        getLogger().info("Initialization completed");
        while(true) {
            Thread.sleep(1000);
        }
    }
}
