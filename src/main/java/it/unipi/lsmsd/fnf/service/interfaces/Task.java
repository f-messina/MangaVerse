package it.unipi.lsmsd.fnf.service.interfaces;

import java.io.Serializable;

/**
 * This abstract class defines the structure of a task.
 * A task is a unit of work that can be executed by the ExecutorTaskService.
 * It has a priority, a timestamp, and a number of retries.
 * The priority is an integer between 0 and 10, where 0 is the lowest priority and 10 is the highest.
 * The timestamp is the time when the task was created, in milliseconds.
 * The number of retries is the number of times the task has been executed.
 * @see ExecutorTaskService
 * @see TaskManager
 */
public abstract class Task implements Serializable {

    private static final int DEFAULT_PRIORITY = 5;
    private static final int MAX_RETRIES = 3;
    private int priority;
    private long timestamp;
    private int retries;

    public Task(int priority) {

        if(priority < 0 || priority > 10)
            this.priority = DEFAULT_PRIORITY;
        else
            this.priority = priority;

        this.retries = 0;
        this.timestamp = System.currentTimeMillis(); //timestamp in milliseconds
    }

    public static int getMaxRetries() {
       return MAX_RETRIES;
    }
    public int getPriority() {
       return priority;
    }
    public long getTimestamp() {
       return timestamp;
    }
    public int getRetries() {
       return retries;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public void setRetries(int retries) {
        this.retries = retries;
    }
    public synchronized void incrementRetries() {
      retries++;
    }
    public abstract void executeJob() throws Exception;
}
