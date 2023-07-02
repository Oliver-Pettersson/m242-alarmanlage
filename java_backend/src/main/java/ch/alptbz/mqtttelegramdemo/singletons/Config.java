package ch.alptbz.mqtttelegramdemo.singletons;

import static ch.alptbz.mqtttelegramdemo.singletons.Logger.getLogger;

import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;

public class Config {
  private static Properties configProperties;

  private Config() {}

  private static void loadConfig() {
    configProperties = new Properties();
    try (FileReader fr = new FileReader("config.properties")) {
      configProperties.load(fr);
      getLogger().info("Config file loaded");
    } catch (IOException e) {
      getLogger().log(Level.SEVERE, "Error loading config file", e);
    }
  }

  public static Properties getConfig() {
    if (configProperties == null) {
      loadConfig();
    }
    return configProperties;
  }
}
