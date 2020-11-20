package edu.upenn.cis.cis455.m1.server.http;

import edu.upenn.cis.cis455.m1.server.HttpServer;
import edu.upenn.cis.cis455.m1.server.HttpWorker;
import edu.upenn.cis.cis455.m1.server.genericImpl.InHouseThreadExecutor;
import edu.upenn.cis.cis455.m1.server.genericImpl.interfaces.ThreadExecutorManager;

import java.util.HashMap;
import java.util.Map;

/***
 *
 * ThreadExecutorManager is a part of generic server.
 * This is used for call backs on start and end of processing by workers.
 *
 */

public class HttpServerThreadExecutorManager implements ThreadExecutorManager {

    private HttpServer httpServer;

    /**
     * We are having a map to store the server workers -> http worker mapping.
     * This is used as http server hooks need httpWorker object.
     */

    private Map<InHouseThreadExecutor.Worker, HttpWorker> httpWorkerMap;

    public HttpServerThreadExecutorManager(HttpServer httpServer) {

        this.httpServer = httpServer;
        this.httpWorkerMap = new HashMap<>();
    }

    @Override
    public void start(InHouseThreadExecutor.Worker worker, Runnable task) {
        HttpWorker httpWorker = getHttpWorker(worker);
        httpServer.start(httpWorker);
    }

    @Override
    public void done(InHouseThreadExecutor.Worker worker, Runnable task) {
        HttpWorker httpWorker = getHttpWorker(worker);
        httpServer.done(httpWorker);
    }

    @Override
    public void error(InHouseThreadExecutor.Worker worker, Runnable task, Throwable e) {
        HttpWorker httpWorker = getHttpWorker(worker);
        httpWorker.setError(e);
        httpServer.error(httpWorker);

    }

    private HttpWorker getHttpWorker(InHouseThreadExecutor.Worker worker) {
        if(httpWorkerMap.containsKey(worker)) {
            return httpWorkerMap.get(worker);
        }
        HttpWorker httpWorker = new HttpWorker(worker);
        httpWorkerMap.put(worker, httpWorker);
        return httpWorker;
    }

}
