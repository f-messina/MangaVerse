package it.unipi.lsmsd.fnf.service.interfaces;

public interface ExecutorTaskService {

    public void executeTask(Task task);

    public void start();

    public void stop();

}
