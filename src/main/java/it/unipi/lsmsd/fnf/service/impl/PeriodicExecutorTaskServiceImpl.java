package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks.UpdateAverageRatingTask;
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import static java.util.concurrent.TimeUnit.*;

public class PeriodicExecutorTaskServiceImpl implements ExecutorTaskService {
    private static volatile ExecutorTaskService instance = null;
    private final ScheduledExecutorService scheduledExecutorService;

    private PeriodicExecutorTaskServiceImpl() {
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }


    /**
     * Returns the singleton instance of PeriodicExecutorTaskServiceImpl.
     * If the instance is null, it creates a new one.
     * @return The singleton instance of PeriodicExecutorTaskServiceImpl
     */
    public static ExecutorTaskService getInstance() {
        if (instance == null) {
            synchronized (PeriodicExecutorTaskServiceImpl.class) {
                if (instance == null) {
                    instance = new PeriodicExecutorTaskServiceImpl();
                }
            }
        }
        return instance;
    }

    @Override
    public void executeTask(Task task) {}

    /**
     * Starts the service by scheduling a periodic task.
     * In this implementation, it schedules the UpdateAverageRatingTask to execute every 12 hours.
     */
    @Override
    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                new UpdateAverageRatingTask().executeJob();

            } catch (BusinessException e) {
                throw new RuntimeException(e);
            }
        }, 0, 12, HOURS);
    }


    /**
     * Stops the service by shutting down the ScheduledExecutorService.
     * It waits for a maximum of 2 minutes for the scheduled tasks to complete.
     * If tasks do not complete within this time, they are forcefully shut down.
     * If the thread is interrupted while waiting, a RuntimeException is thrown.
     */
    @Override
    public void stop() {
        scheduledExecutorService.shutdown();
        try{
            if (!scheduledExecutorService.awaitTermination(2, MINUTES)) {
                scheduledExecutorService.shutdownNow();
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
