package it.unipi.lsmsd.fnf.service.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;

import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.impl.asinc_media_tasks.UpdateAverageRatingTask;
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.Task;

/**
 * Implementation of ExecutorTaskService that executes tasks in a separate thread with a fixed schedule.
 * It uses a ScheduledExecutorService to execute the tasks periodically.
 * In this implementation, it schedules only the UpdateAverageRatingTask to execute every 12 hours.
 * @see ScheduledExecutorService
 * @see ExecutorTaskService
 * @see Task
 * @see UpdateAverageRatingTask
 */
public class PeriodicExecutorTaskServiceImpl implements ExecutorTaskService {
    private final ScheduledExecutorService scheduledExecutorService;

    private PeriodicExecutorTaskServiceImpl() {
        this.scheduledExecutorService = Executors.newScheduledThreadPool(1);
    }

    /**
     * Method to get the singleton instance of PeriodicExecutorTaskServiceImpl.
     * This method is thread-safe.
     *
     * @return          The singleton instance of PeriodicExecutorTaskServiceImpl
     */
    public static ExecutorTaskService getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder {
        private static final ExecutorTaskService INSTANCE = new PeriodicExecutorTaskServiceImpl();
    }
    

    /**
     * Executes the given task.
     * In this implementation, the method does nothing as the tasks are executed periodically.
     *
     * @param task      The task to be executed
     */
    @Override
    public void executeTask(Task task) {}

    /**
     * Starts the service by scheduling the UpdateAverageRatingTask to execute every 12 hours.
     * The task is executed immediately and then every 12 hours.
     * If the task throws a BusinessException, a RuntimeException is thrown.
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
     * Stops the service by shutting down the ScheduledExecutorService and waiting for a maximum of 10 minutes
     * for the tasks to complete. If the tasks do not complete within the timeout, the ScheduledExecutorService
     * is forcefully shut down.
     * If the thread is interrupted while waiting for termination, a RuntimeException is thrown.
     */
    @Override
    public void stop() {
        scheduledExecutorService.shutdown();
        try{
            if (!scheduledExecutorService.awaitTermination(10, MINUTES)) {
                scheduledExecutorService.shutdownNow();
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }
}
