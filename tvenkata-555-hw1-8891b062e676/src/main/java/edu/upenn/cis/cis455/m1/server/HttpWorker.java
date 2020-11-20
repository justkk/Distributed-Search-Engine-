package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.m1.server.genericImpl.InHouseThreadExecutor;
import edu.upenn.cis.cis455.m1.server.http.HttpTaskWrapper;

/**
 * Stub class for a thread worker for
 * handling Web requests
 */
public class HttpWorker {

    /**
     * Worker instance of generic worker.
     */
    private InHouseThreadExecutor.Worker worker;
    private Throwable error;


    public HttpWorker(InHouseThreadExecutor.Worker worker) {
        this.worker = worker;
    }

    public HttpTaskWrapper getHttpTask() {
        return (HttpTaskWrapper) worker.getCurrentTask();
    }

    public InHouseThreadExecutor.Worker getWorker() {
        return worker;
    }

    public String getId() {
        return worker.getName();
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}
