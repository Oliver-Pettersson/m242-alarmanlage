package ch.alptbz.mqtttelegramdemo.singletons;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

public class Logger {
  private static java.util.logging.Logger loggerInstance;

  private Logger() {}

  private static void initLogger() {
    ConsoleHandler ch = new ConsoleHandler();
    ch.setLevel(Level.ALL);
    java.util.logging.Logger.getGlobal().addHandler(ch);
    loggerInstance = java.util.logging.Logger.getLogger("main");
  }

  public static java.util.logging.Logger getLogger() {
    if (loggerInstance == null) {
      initLogger();
    }
    return loggerInstance;
  }
}
