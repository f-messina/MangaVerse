package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.Task;
import it.unipi.lsmsd.fnf.service.interfaces.TaskManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Task manager that processes tasks from the task queue in a separate thread.
 * It uses a single-threaded executor service to execute the tasks.
 * The tasks are executed in a separate thread, and the manager is started and stopped by the user.
 *
 * @see TaskManager
 * @see Task
 */
public class ErrorTaskManager extends TaskManager {
    private static final Logger logger = Logger.getLogger(ErrorTaskManager.class.getName());
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
     * @return      the singleton instance of ErrorTaskManager
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

    /**
     * Starts the task manager by executing a task that continuously processes tasks from the task queue.
     * The task manager runs in a separate thread, checking if the manager is still running.
     * For each task:
     * 1. The task is retrieved from the queue.
     * 2. The retry count is incremented.
     * 3. If the retry count exceeds the maximum allowed retries, the task is skipped.
     * 4. The task's job is executed.
     *
     * @throws RuntimeException     If an exception occurs while executing the task.
     *                              If the exception is a BusinessException with type RETRYABLE_ERROR,
     *                              the task is added back to the queue.
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
                        logger.warning("Task skipped: " + task.getClass());
                        continue;
                    }
                    task.executeJob();

                } catch (BusinessException e) {
                    if (e.getType().equals(BusinessExceptionType.RETRYABLE_ERROR))
                        addTask(task);
                    else
                        logger.severe("Cannot execute task: " + e.getMessage() + " " + task.getClass());
                } catch (Exception e) {
                    logger.severe("Error while executing task: " + e.getMessage());
                    throw new RuntimeException(e);
                }
            }
        });
    }

    /**
     * Stops the task manager by shutting down the executor service and setting the running flag to false.
     * The executor service is first instructed to shut down.
     * It then waits for a maximum of 1 second for the existing tasks to complete.
     * If the tasks do not complete within this time frame, the executor service is forcefully shut down.
     *
     * @throws RuntimeException     If an exception occurs while stopping the task manager.
     */
    @Override
    public void stop() {
        isRunning = false;
        executorService.shutdown();
        try{
            if (!executorService.awaitTermination(1, SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.severe("Error while waiting for termination: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
