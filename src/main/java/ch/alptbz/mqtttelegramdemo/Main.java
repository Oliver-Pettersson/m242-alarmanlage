package ch.alptbz.mqtttelegramdemo;

import ch.alptbz.mqtttelegramdemo.handlers.TemperatureHandler;
import ch.alptbz.mqtttelegramdemo.mqtt.Mqtt;
import ch.alptbz.mqtttelegramdemo.mqtt.MqttConsumerInterface;
import ch.alptbz.mqtttelegramdemo.scheduler.GlobalScheduler;
import ch.alptbz.mqtttelegramdemo.scheduler.RecurringTaskInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramConsumerInterface;
import ch.alptbz.mqtttelegramdemo.telegram.TelegramNotificationBot;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static Logger logger;
    private static Properties config;
    private static TelegramNotificationBot tnb;
    private static Mqtt mqttClient;

    private static boolean loadConfig() {
        config = new Properties();
        try {
            config.load(new FileReader("config.properties"));
            return true;
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error loading config file",e);
        }
        return false;
    }

    private static void addHandler(Object handler) throws MqttException {
        if(handler instanceof TelegramConsumerInterface) {
            tnb.addHandler((TelegramConsumerInterface)handler);
        }
        if(handler instanceof MqttConsumerInterface) {
            mqttClient.addHandler((MqttConsumerInterface)handler);
        }
        if(handler instanceof RecurringTaskInterface) {
            GlobalScheduler.main().addTask((RecurringTaskInterface)handler);
        }
    }


    public final static void main(String[] args) throws InterruptedException {
        initLogger();

        if(!loadConfig()) return;
        logger.info("Config file loaded");

        tnb = new TelegramNotificationBot(config.getProperty("telegram-apikey"));
        logger.info("TelegramBot started");
        mqttClient = new Mqtt(config.getProperty("mqtt-url"), "runner-12");
        try {
            mqttClient.start();
        } catch (MqttException e) {
            logger.log(Level.SEVERE, "", e);
        }

        try {
            addHandler(new TemperatureHandler("m5core2", tnb));
        } catch (MqttException e) {
            logger.log(Level.SEVERE, "Failed to init handler", e);
        }

        logger.info("Initialization completed");

        while(true) {
            Thread.sleep(1000);
        }
    }

    private static void initLogger() {
        ConsoleHandler ch = new ConsoleHandler();
        ch.setLevel(Level.ALL);
        Logger.getGlobal().addHandler(ch);
        logger = Logger.getLogger("main");
    }

}
