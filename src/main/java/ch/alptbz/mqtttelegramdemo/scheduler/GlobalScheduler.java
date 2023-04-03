package ch.alptbz.mqtttelegramdemo.scheduler;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GlobalScheduler extends TimerTask {

    public static Logger logger = java.util.logging.Logger.getLogger("GlobalScheduler");
    private final Timer mainTimer;
    private ArrayList<RecurringTaskInterface> tasks = new ArrayList<>();

    private static GlobalScheduler _main;

    public static GlobalScheduler main() {
        if(_main == null) {
            _main = new GlobalScheduler();
        }
        return _main;
    }

    public GlobalScheduler() {
        mainTimer = new Timer();

        mainTimer.schedule(this, 1000);
    }

    public void addTask(RecurringTaskInterface task) {
        tasks.add(task);
    }

    public void run() {
        logger = java.util.logging.Logger.getLogger(tasks.getClass().getName());
        try{
            for(RecurringTaskInterface task: tasks) {
                task.run();
            }
        }catch (Exception e) {
            logger.log(Level.SEVERE, "", e);
        }
    }

}
