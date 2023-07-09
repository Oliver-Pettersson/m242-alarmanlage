package ch.alptbz.mqtttelegramdemo.logdata;


import static ch.alptbz.mqtttelegramdemo.singletons.Logger.getLogger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class LogDataRepository {
  private SessionFactory sessionFactory;
  private static final LogDataRepository REPOSITORY = new LogDataRepository();

  private LogDataRepository() {
    // configures settings from hibernate.cfg.xml
    StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
    try {
      this.sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
    } catch (Exception e) {
      getLogger().log(Level.INFO, "FAILED IN CONSTRUCTOR");
      getLogger().log(Level.INFO, e.toString());
    }
  }

  public List<LogData> getLogs() {
    Session session = this.sessionFactory.openSession();
    session.beginTransaction();

    List<LogData> result = session.createQuery("from LogData", LogData.class).list();

    session.getTransaction().commit();
    session.close();

    return result;
  }

  public void saveLog(String message) {
    Session session = this.sessionFactory.openSession();
    session.beginTransaction();
    LogData newEntry = new LogData();
    newEntry.setMessage(message);
    newEntry.setDateTime(LocalDateTime.now());
    session.persist(newEntry);
    session.getTransaction().commit();
    session.close();
  }

  public static LogDataRepository getRepository() {
    return REPOSITORY;
  }
}
