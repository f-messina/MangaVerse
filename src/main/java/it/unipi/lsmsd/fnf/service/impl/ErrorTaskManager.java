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
