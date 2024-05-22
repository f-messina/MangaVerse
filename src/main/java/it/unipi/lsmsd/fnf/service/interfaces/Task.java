package it.unipi.lsmsd.fnf.service.interfaces;

import java.io.Serializable;

public abstract class Task implements Serializable {

   private static final int DEFAULT_PRIORITY = 5;
   private static final int MAX_RETRIES = 3;
   private int priority;
   private long timestamp;
   private int retries;

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

    public Task(int priority) {

       if(priority < 0 || priority > 10)
           this.priority = DEFAULT_PRIORITY;
       else
           this.priority = priority;

       this.retries = 0;
       this.timestamp = System.currentTimeMillis(); //timestamp in milliseconds
   }

   public synchronized void incrementRetries() {
      retries++;
   }

    public abstract void executeJob() throws Exception;

}
