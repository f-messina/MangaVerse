package it.unipi.lsmsd.fnf.service.interfaces;

/**
 * This interface provides the methods to execute a task and to start and stop the executor.
 * The tasks are executed in a separate thread.
 *
 * @see Task
 */
public interface ExecutorTaskService {
    void executeTask(Task task);
    void start();
    void stop();

}
