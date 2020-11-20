package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import edu.upenn.cis.cis455.crawler.queue.WaitingQueueInstance;
import edu.upenn.cis.cis455.crawler.validator.ProcessContext;

import java.util.ArrayList;
import java.util.List;

public class CrawlServiceResponse {
    private List<ReadyQueueInstance> readyQueueInstanceList = new ArrayList<>();
    private List<WaitingQueueInstance> waitingQueueInstanceList = new ArrayList<>();
    private ProcessContext processContext;
    private boolean success;
    private boolean processed = false;
    private String message = "";


    public CrawlServiceResponse(List<ReadyQueueInstance> readyQueueInstanceList,
                                List<WaitingQueueInstance> waitingQueueInstanceList, ProcessContext processContext, boolean processed) {
        this.success = true;
        this.readyQueueInstanceList = readyQueueInstanceList;
        this.waitingQueueInstanceList = waitingQueueInstanceList;
        this.processContext = processContext;
        this.processed = processed;
    }

    public CrawlServiceResponse(boolean success, String message, ProcessContext processContext) {
        this.success = success;
        this.processContext = processContext;
        this.message = message;
        this.processed = true;
    }

    public List<ReadyQueueInstance> getReadyQueueInstanceList() {
        return readyQueueInstanceList;
    }

    public void setReadyQueueInstanceList(List<ReadyQueueInstance> readyQueueInstanceList) {
        this.readyQueueInstanceList = readyQueueInstanceList;
    }

    public List<WaitingQueueInstance> getWaitingQueueInstanceList() {
        return waitingQueueInstanceList;
    }

    public void setWaitingQueueInstanceList(List<WaitingQueueInstance> waitingQueueInstanceList) {
        this.waitingQueueInstanceList = waitingQueueInstanceList;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ProcessContext getProcessContext() {
        return processContext;
    }

    public void setProcessContext(ProcessContext processContext) {
        this.processContext = processContext;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }
}
