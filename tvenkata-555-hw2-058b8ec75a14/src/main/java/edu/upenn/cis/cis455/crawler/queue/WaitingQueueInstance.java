package edu.upenn.cis.cis455.crawler.queue;

import java.util.Date;

public class WaitingQueueInstance {

    private ReadyQueueInstance readyQueueInstance;
    private Date insertedTime;
    private long waitingTime;

    public WaitingQueueInstance(ReadyQueueInstance readyQueueInstance, Date insertedTime, long waitingTime) {
        this.readyQueueInstance = readyQueueInstance;
        this.insertedTime = insertedTime;
        this.waitingTime = waitingTime;
    }

    public ReadyQueueInstance getReadyQueueInstance() {
        return readyQueueInstance;
    }

    public Date getInsertedTime() {
        return insertedTime;
    }

    public long getWaitingTime() {
        return waitingTime;
    }

    public boolean isValid() {
        return readyQueueInstance.isValid();
    }
}
