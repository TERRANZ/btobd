package ru.terra.btdiag.obd.io;

import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import roboguice.service.RoboService;
import ru.terra.btdiag.core.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


public abstract class AbstractGatewayService extends RoboService {
    private static final String TAG = AbstractGatewayService.class.getName();

    protected Context ctx;
    protected boolean isRunning = false;
    private final IBinder binder = new AbstractGatewayServiceBinder();
    protected boolean isQueueRunning = false;
    protected Long queueCounter = 0L;
    protected BlockingQueue<ObdCommandJob> jobsQueue = new LinkedBlockingQueue<ObdCommandJob>();

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Logger.d(this, TAG, "Creating service..");
        Logger.d(this, TAG, "Service created.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Logger.d(this, TAG, "Destroying service...");
//        notificationManager.cancel(NOTIFICATION_ID);
        Logger.d(this, TAG, "Service destroyed.");
    }

    public boolean isRunning() {
        return isRunning;
    }


    public class AbstractGatewayServiceBinder extends Binder {
        public AbstractGatewayService getService() {
            return AbstractGatewayService.this;
        }
    }

    /**
     * This method will add a job to the queue while setting its ID to the
     * internal queue counter.
     *
     * @param job the job to queue.
     */
    public void queueJob(ObdCommandJob job) {
        queueCounter++;
        Logger.d(this, TAG, "Adding job[" + queueCounter + "] to queue..");

        job.setId(queueCounter);
        try {
            jobsQueue.put(job);
            Logger.d(this, TAG, "Job queued successfully.");
        } catch (InterruptedException e) {
            job.setState(ObdCommandJob.ObdCommandJobState.QUEUE_ERROR);
            Logger.e(this, TAG, "Failed to queue job.", e);
        }

        if (!isQueueRunning) {
            // Run the executeQueue in a different thread to lighten the UI thread
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    executeQueue();
                }
            });

            t.start();
        }
    }

    /**
     * Show a notification while this service is running.
     */


    public void setContext(Context c) {
        ctx = c;
    }

    abstract protected void executeQueue();

    abstract public boolean startService();

    abstract public void stopService();

    public int getCurrentQueueSize() {
        synchronized (jobsQueue) {
            return jobsQueue.size();
        }
    }
}
