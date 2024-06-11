package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;
import it.unipi.lsmsd.fnf.service.interfaces.TaskManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ErrorTaskManager extends TaskManager {
    private static volatile ErrorTaskManager instance = null;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Boolean isRunning;
    private ErrorTaskManager() {
        super();
        isRunning = true;
    }

    /**
     * Returns the singleton instance of ErrorTaskManager.
     * If the instance is null, it is created in a thread-safe manner using double-checked locking.
     *
     * @return the singleton instance of ErrorTaskManager
     */
    public static ErrorTaskManager getInstance() {
        if (instance == null) {
            synchronized (ErrorTaskManager.class) {
                if (instance == null) {
                    instance = new ErrorTaskManager();
                }
            }
        }
        return instance;
    }

    /*

    Start the task manager: start the executor service and schedule a priodic task to execute the tasks in the queue.
    The periodic task is scheduled to run every 8 seconds, and it will start if and only if the queue is not empty.
    It will execute all the tasks in the queue, and then it will stop.

     */


    /**
     * Starts the task manager by executing a task that continuously processes tasks from the task queue.
     * The task manager runs in a separate thread, checking if the manager is still running.
     *
     * For each task:
     * 1. The task is retrieved from the queue.
     * 2. The retry count is incremented.
     * 3. If the retry count exceeds the maximum allowed retries, the task is skipped.
     * 4. The task's job is executed.
     *
     * If a BusinessException occurs:
     * - If the exception is of type RETRYABLE_ERROR, the task is re-added to the queue.
     * - Otherwise, an error message is printed.
     *
     * Any other exceptions will cause a RuntimeException to be thrown.
     */
    @Override
    public void start() {
        executorService.execute(() -> {
            while (isRunning) {
                Task task = null;
                try {
                    task = getTaskQueue().take();
                    task.incrementRetries();
                    if(task.getRetries() > Task.getMaxRetries()) {
                        // add logger
                        continue;
                    }
                    task.executeJob();

                } catch (BusinessException e) {
                    if (e.getType().equals(BusinessExceptionType.RETRYABLE_ERROR)) {
                        addTask(task);
                    }
                    else {
                        System.err.println("Cannot execute task: " + e.getMessage() + " " + task.getClass());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Stops the task manager by shutting down the executor service and setting the running flag to false.
     * The executor service is first instructed to shut down.
     *
     * It then waits for a maximum of 800 milliseconds for the existing tasks to complete.
     * If the tasks do not complete within this time frame, the executor service is forcefully shut down.
     *
     * If the thread is interrupted while waiting, a RuntimeException is thrown.
     */
    @Override
    public void stop() {
        isRunning = false;
        executorService.shutdown();
        try{
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
