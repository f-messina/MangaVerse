package it.unipi.lsmsd.fnf.service.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import static java.util.concurrent.TimeUnit.MINUTES;
import java.util.logging.Logger;

import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.Task;
import it.unipi.lsmsd.fnf.service.interfaces.TaskManager;

/**
 * Implementation of ExecutorTaskService that executes tasks in a separate thread without a fixed schedule.
 * It uses an ExecutorService to execute the tasks.
 * If a task throws a BusinessException with type RETRYABLE_ERROR, the task is added back to the task manager
 * to be retried later. If the BusinessException has any other type, the task is not retried and an error message is logged.
 * @see ExecutorTaskService
 * @see Task
 * @see TaskManager
 */
public class AperiodicExecutorTaskServiceImpl implements ExecutorTaskService {
    private final ExecutorService executorService;
    private final TaskManager taskManager;
    private static final Logger logger = Logger.getLogger(AperiodicExecutorTaskServiceImpl.class.getName());

    private AperiodicExecutorTaskServiceImpl() {
        executorService = Executors.newFixedThreadPool(10);
        this.taskManager = ErrorTaskManager.getInstance();
    }

    /**
     * Returns the singleton instance of AperiodicExecutorTaskServiceImpl.
     * This method is thread-safe.
     *
     * @param taskManager       The TaskManager to be used by the ExecutorTaskService.
     * @return                  The singleton instance of ExecutorTaskService.
     */
    public static ExecutorTaskService getInstance(TaskManager taskManager) {
        return InstanceHolder.INSTANCE;
    }

    private static class InstanceHolder {
        private static final ExecutorTaskService INSTANCE = new AperiodicExecutorTaskServiceImpl();
    }
    

    /**
     * Starts the executor service.
     * This method is synchronized to ensure thread safety.
     */
    @Override
    public synchronized void start() {}

    /**
     * Stops the executor service by shutting it down and waiting for a maximum of 2 minutes
     * for the tasks to complete. If the tasks do not complete within the timeout, the executor
     * service is forcefully shut down.
     * If the thread is interrupted while waiting for termination, a RuntimeException is thrown.
     * This method is synchronized to ensure thread safety.
     *
     * @throws RuntimeException     if the thread is interrupted while waiting for termination
     */
    @Override
    public synchronized void stop() {
        executorService.shutdown();
        try{
            if (!executorService.awaitTermination(2, MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            logger.severe("Error while waiting for termination: " + e.getMessage());
            throw new RuntimeException(e);
        }

    }

    /**
     * Executes the given task by submitting it to the executor service.
     * If the task throws a BusinessException with type RETRYABLE_ERROR, the task is added
     * back to the task manager to be retried later. If the BusinessException has any other type,
     * the task is not retried and an error message is logged.
     *
     * @param task          The task to be executed.
     */
    @Override
    public synchronized void executeTask(Task task) {
       executorService.execute(() -> {
           try {
               task.executeJob();
           } catch (BusinessException e) {
               if (e.getType().equals(BusinessExceptionType.RETRYABLE_ERROR)) {
                   taskManager.addTask(task);
               }
               else {
                     logger.severe("Cannot execute task: " + e.getMessage() + " " + task.getClass());
               }
           } catch (Exception e) {
                logger.severe("Error while executing task: " + e.getMessage());
               throw new RuntimeException(e);
           }
       });
    }
}
