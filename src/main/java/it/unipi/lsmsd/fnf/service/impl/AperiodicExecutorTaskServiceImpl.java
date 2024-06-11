package it.unipi.lsmsd.fnf.service.impl;

import it.unipi.lsmsd.fnf.service.exception.BusinessException;
import it.unipi.lsmsd.fnf.service.exception.enums.BusinessExceptionType;
import it.unipi.lsmsd.fnf.service.interfaces.ExecutorTaskService;
import it.unipi.lsmsd.fnf.service.interfaces.Task;
import it.unipi.lsmsd.fnf.service.interfaces.TaskManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.util.concurrent.TimeUnit.MINUTES;

public class AperiodicExecutorTaskServiceImpl implements ExecutorTaskService {

    private static volatile ExecutorTaskService instance = null;
    private ExecutorService executorService;
    private TaskManager taskManager;

    private AperiodicExecutorTaskServiceImpl(TaskManager taskManager) {
        executorService = Executors.newFixedThreadPool(10);
        this.taskManager = taskManager;
    }
    @Override
    public synchronized void start() {}

    /**
     * Gracefully stops the execution service, ensuring that all running tasks are completed
     * or forcefully terminated if they do not finish within a specified timeout.
     *
     * This method first initiates an orderly shutdown in which previously submitted tasks are executed,
     * but no new tasks will be accepted. It then waits for up to 2 minutes for the completion of all
     * tasks. If the service fails to terminate within this time, it forcefully shuts down by interrupting
     * the running tasks.
     *
     * This method is synchronized to ensure that it is thread-safe and that only one thread can stop the
     * service at a time.
     *
     * @throws RuntimeException if the thread is interrupted while waiting for termination
     */
    @Override
    public synchronized void stop() {
        executorService.shutdown();
        try{
            if (!executorService.awaitTermination(2, MINUTES)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Executes a given task asynchronously using the executor service.
     * If the task execution fails with a retryable error, the task is re-added
     * to the task manager for a future retry. For non-retryable errors, an error
     * message is logged to the standard error stream. Any other unexpected exceptions
     * will result in a RuntimeException being thrown.
     *
     * This method is synchronized to ensure thread safety, preventing multiple threads
     * from executing tasks simultaneously.
     *
     * @param task The task to be executed.
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
                  System.err.println("Cannot execute task: " + e.getMessage() + " " + task.getClass());
               }
           } catch (Exception e) {
               throw new RuntimeException(e);
           }
       });
    }

    /**
     * Provides a thread-safe singleton instance of the ExecutorTaskService.
     * This method ensures that only one instance of AperiodicExecutorTaskServiceImpl
     * is created, even in a multithreaded environment.
     *
     * Double-checked locking is used to ensure that the instance is only created
     * once. The method first checks if the instance is null, and if so, synchronizes
     * on the class object to create a new instance. This synchronization block ensures
     * that only one thread can create the instance, even if multiple threads enter
     * the method simultaneously.
     *
     * @param taskManager The TaskManager to be used by the ExecutorTaskService.
     * @return The singleton instance of ExecutorTaskService.
     */
    public static ExecutorTaskService getInstance(TaskManager taskManager) {
        if (instance == null) {
            synchronized (AperiodicExecutorTaskServiceImpl.class) {
                if (instance == null) {
                    instance = new AperiodicExecutorTaskServiceImpl(taskManager);
                }
            }
        }
        return instance;
    }
}
