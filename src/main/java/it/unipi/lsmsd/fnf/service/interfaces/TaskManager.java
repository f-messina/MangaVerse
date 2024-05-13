package it.unipi.lsmsd.fnf.service.interfaces;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;

/*
     This class is used to manage tasks  
 */
public abstract class TaskManager {

    private final PriorityBlockingQueue<Task> taskQueue;

    /*
        Comparator used to compare tasks by priority
        The creation Tasks have the highest priority, then the update tasks and finally the delete tasks
        If two tasks have the same priority, the one that was created first is executed first

     */
    private static final Comparator<Task> taskComparator = (o1, o2) -> {
        if(o1.getPriority() > o2.getPriority())
            return 1;
        else if(o1.getPriority() < o2.getPriority())
            return -1;
        else
            return Long.compare(o1.getTimestamp(), o2.getTimestamp()); //it is very unlikely that two tasks have the same timestamp
    };

    public TaskManager() {
        this.taskQueue = new PriorityBlockingQueue<>(20, taskComparator);
    }

    public PriorityBlockingQueue<Task> getTaskQueue() {
        return taskQueue;
    }

    /*
            Starts the task manager
         */
    public abstract void start();

    /*
        Stops the task manager
     */
    public abstract void stop();

    /*
        Adds a task to the task queue
     */
    public synchronized void addTask(Task task) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        taskQueue.put(task);
    }

}
