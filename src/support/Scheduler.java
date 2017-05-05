package support;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

public class Scheduler extends Thread
{
    
    public interface SchedulerAction {
        public void runAction();
    }
    
    /**
     * Timer to fire off event
     */
    private Timer timer;
    
    
    /**
     * Initializes stop watch with a predetermined seconds
     * @param seconds Number of seconds to start from
     * @param delegate Controller to handle events
     */
    public Scheduler(double seconds, SchedulerAction function)
    {
        timer = new Timer((int)(seconds * 1000), new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                function.runAction();
            }
        });
    }
    
//    /**
//     * Starts the stop watch
//     */
//    public void start()
//    {
//        timer.start();
//    }
    

//    /**
//     * Stops the stop watch
//     */
//    public void stop()
//    {
//        timer.stop();
//    }
    
    public void stopThread()
    {
        timer.stop();
        interrupt();
    }
    

    @Override
    public void run()
    {
        System.out.println("Thread running");
        timer.start();
    }
}
