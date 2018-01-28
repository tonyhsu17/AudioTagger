package support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;



public class Scheduler extends Thread implements Logger {

    public interface SchedulerAction {
        public void runAction();
    }

    /**
     * Timer to fire off event
     */
    private Timer timer;
    int count;
    int maxCount;

    /**
     * Initializes stop watch with a predetermined seconds
     * 
     * @param seconds Number of seconds to start from
     * @param delegate Controller to handle events
     */
    public Scheduler(double seconds, SchedulerAction function) {
        timer = new Timer((int)(seconds * 1000), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                function.runAction();
                count++;
                if(count >= maxCount) {
                    stopThread();
                }
            }
        });
    }

    public void runNTimes(int count) {
        info("Thread with max count: " + count);
        maxCount = count;
        timer.start();
    }

    public void stopThread() {
        timer.stop();
        interrupt();
    }


    @Override
    public void run() {
        info("Thread running");
        count = 0;
        timer.start();
    }
}
