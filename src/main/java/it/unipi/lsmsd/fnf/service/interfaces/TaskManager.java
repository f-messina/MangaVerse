package it.unipi.lsmsd.fnf.service.interfaces;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * This abstract class provides the methods to manage tasks.
 * It uses a PriorityBlockingQueue to store the tasks.
 * The tasks are ordered based on their priority and, in case of equal priority, on their timestamp.
 * @see PriorityBlockingQueue
 * @see Task
 */
public abstract class TaskManager {

    private final PriorityBlockingQueue<Task> taskQueue;

    public TaskManager() {
        this.taskQueue = new PriorityBlockingQueue<>(30, taskComparator);
    }

    /**
     * Comparator to compare tasks based on their priority and,
     * in case of equal priority, on their timestamp.
     */
    private static final Comparator<Task> taskComparator = (o1, o2) -> {
        if(o1.getPriority() > o2.getPriority())
            return 1;
        else if(o1.getPriority() < o2.getPriority())
            return -1;
        else
            return Long.compare(o1.getTimestamp(), o2.getTimestamp());
    };

    public PriorityBlockingQueue<Task> getTaskQueue() {
        return taskQueue;
    }

    /**
     * Starts the task manager.
     */
    public abstract void start();

    /**
     * Stops the task manager.
     */
    public abstract void stop();

    /**
     * Adds a task to the task queue.
     * The task is added to the queue based on its priority.
     *
     * @param task      The task to be added
     */
    public synchronized void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        taskQueue.put(task);
    }
}
