package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.Constants;
import edu.upenn.cis.cis455.ServiceFactory;
import edu.upenn.cis.cis455.WebServer;
import edu.upenn.cis.cis455.exceptions.ServerHaltException;
import edu.upenn.cis.cis455.m1.server.genericImpl.Server;
import edu.upenn.cis.cis455.m1.server.genericImpl.models.ServerConfig;
import edu.upenn.cis.cis455.m1.server.genericImpl.models.ServerStatus;
import edu.upenn.cis.cis455.m1.server.handlers.DefaultRequestHandler;
import edu.upenn.cis.cis455.m1.server.handlers.HttpGetRequestHandler;
import edu.upenn.cis.cis455.m1.server.handlers.HttpHeadRequestHandler;
import edu.upenn.cis.cis455.m1.server.http.*;
import edu.upenn.cis.cis455.m1.server.http.models.HttpServerConfig;
import edu.upenn.cis.cis455.m1.server.http.models.HttpWorkerInfo;
import edu.upenn.cis.cis455.m1.server.implementations.HttpSession;
import edu.upenn.cis.cis455.m1.server.implementations.HttpWebServiceImpl;
import edu.upenn.cis.cis455.m1.server.interfaces.HttpRequestHandler;
import edu.upenn.cis.cis455.m2.server.interfaces.Session;
import edu.upenn.cis.cis455.util.FixedSizeArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Stub for your HTTP server, which
 * listens on a ServerSocket and handles
 * requests
 */
public class HttpServer implements ThreadManager {

    final static Logger logger = LogManager.getLogger(HttpServer.class);

    /***
     * A request queue for server.
     */

    private HttpTaskQueue<HttpTaskWrapper> requestQueue;
    /***
     * Worker map. This is useful for control page. It fetches info from this map.
     */

    private Map<String, HttpTaskWrapper> workerTaskMap;

    /***
     * Config for server.
     */

    private ServerConfig serverConfig;


    /***
     * Configuration repository. It has filer and routes information
     */

    private MatchConfiguration matchConfiguration;

    /***
     * This is a handler for listener thread. It will make a instance of HttpTaskWrapper and enqueues it.
     */

    private HttpServerHandler httpServerHandler;
    /***
     *
     * A generic server, Instance of a inhouse server. Provides the flexibility to extend to different protocols.
     */
    private Server server;

    /***
     * Every server impl will have to define a ThreadExecutorManager.
     * This encodes the call backs before starting work and after completing work.
     */
    private HttpServerThreadExecutorManager httpServerThreadExecutorManager;

    private final Map<String, HttpSession> sessionMap;


    private FixedSizeArray<LogEntry> errorLog;

    private Thread sessionCleaner;

    private HttpWebServiceImpl httpWebService;

    public HttpRequestHandler createRequestHandlerInstance(HttpWebServiceImpl webService, Path serverRoot) {

        try {
            MatchConfiguration matchConfiguration = new MatchConfiguration(webService.getRoutesFilterConfiguration(),
                    webService.getRoutesHandlerConfiguration(), new StaticFileConfiguration(serverRoot.toRealPath().toString()));

            MethodHandlerExecutor methodHandlerExecutor = new MethodHandlerExecutor(new DefaultRequestHandler(matchConfiguration), matchConfiguration);
            methodHandlerExecutor.setRequestHandler(RequestTypeEnum.GET, new HttpGetRequestHandler(matchConfiguration));
            methodHandlerExecutor.setRequestHandler(RequestTypeEnum.HEAD, new HttpHeadRequestHandler(matchConfiguration));
            return methodHandlerExecutor;

        } catch (IOException e) {
            logger.info("unable to create request handler");
            throw new ServerHaltException(e);
        }
    }


    public HttpServer(HttpServerConfig serverConfig, HttpWebServiceImpl httpWebService) {
        this.requestQueue = new HttpTaskQueue<>();
        this.workerTaskMap = new HashMap<>();
        this.serverConfig = serverConfig;
        this.httpServerHandler = new HttpServerHandler(this, this.createRequestHandlerInstance(httpWebService,
                Paths.get(serverConfig.getStaticFolderLocation())));
        this.httpServerThreadExecutorManager = new HttpServerThreadExecutorManager(this);
        this.server = new Server(serverConfig, httpServerThreadExecutorManager, httpServerHandler, requestQueue);
        this.errorLog = new FixedSizeArray<LogEntry>(Constants.getInstance().getERROR_LOG_SIZE());
        this.sessionMap = new HashMap<>();
        this.httpWebService =  httpWebService;
    }

    public void startServer() {
        server.start();
        sessionCleaner = new Thread(new HttpSessionCleaner(this));
        sessionCleaner.start();
        System.out.println("Started Listner");
    }

    public void stopServer() {
        server.stop();
    }

    public void stopAndWait() {
        server.stop();
        server.closeWorkers();
        stopCleaner();

    }

    private void stopCleaner() {
        while (true) {
            try {
                sessionCleaner.join();
                break;
            } catch (InterruptedException e) {
                System.out.println(e);
            }
        }
    }


    @Override
    public HttpTaskQueue getRequestQueue() {
        // TODO Auto-generated method stub
        return requestQueue;
    }

    @Override
    public boolean isActive() {
        // TODO Auto-generated method stub
        return server.getServerStatus().isActive();
    }

    @Override
    public void start(HttpWorker worker) {
        // TODO Auto-generated method stub
        HttpTaskWrapper currentTask = worker.getHttpTask();
        workerTaskMap.put(worker.getId(), currentTask);
    }

    @Override
    public void done(HttpWorker worker) {
        // TODO Auto-generated method stub
        workerTaskMap.remove(worker.getId());
    }

    @Override
    public void error(HttpWorker worker) {
        // TODO Auto-generated method stub
        HttpTaskWrapper currentTask = worker.getHttpTask();
        LogEntry logEntry = new LogEntry(currentTask, worker.getError());
        errorLog.addElement(logEntry);

    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }


    public ServerStatus getServerStatus() {
        return server.getServerStatus();
    }

    public void setRequestQueue(HttpTaskQueue<HttpTaskWrapper> requestQueue) {
        this.requestQueue = requestQueue;
    }

    public Map<String, HttpTaskWrapper> getWorkerTaskMap() {
        return workerTaskMap;
    }

    public void setWorkerTaskMap(Map<String, HttpTaskWrapper> workerTaskMap) {
        this.workerTaskMap = workerTaskMap;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public MatchConfiguration getMatchConfiguration() {
        return matchConfiguration;
    }

    public void setMatchConfiguration(MatchConfiguration matchConfiguration) {
        this.matchConfiguration = matchConfiguration;
    }

    public HttpServerHandler getHttpServerHandler() {
        return httpServerHandler;
    }

    public void setHttpServerHandler(HttpServerHandler httpServerHandler) {
        this.httpServerHandler = httpServerHandler;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server server) {
        this.server = server;
    }

    public void submitTask(Runnable task) {
        this.server.submitTask(task);
    }

    /***
     * Used by control page handler.
     * Enriches the information into Map<String, HttpWorkerInfo> format.
     *
     * @return Map<String   ,       HttpWorkerInfo>
     */


    public Map<String, HttpWorkerInfo> getServerStats() {

        Map<String, String> workers = server.getWorkerStats();

        Map<String, HttpWorkerInfo> workerStats = new HashMap<>();

        for (Map.Entry<String, String> entry : workers.entrySet()) {
            HttpWorkerInfo workerInfo = new HttpWorkerInfo(entry.getKey());
            workerInfo.setState(entry.getValue());
            if (workerTaskMap.containsKey(entry.getKey())) {
                workerInfo.setRequestInfo(workerTaskMap.get(entry.getKey()).getRequestInfo());
            } else {
                workerInfo.setRequestInfo(new HashMap<>());
            }
            workerStats.put(entry.getKey(), workerInfo);
        }
        return workerStats;
    }

    public List<LogEntry> getErrorLog() {
        return errorLog.getFixedSizeList();
    }

    public class LogEntry {

        private HttpTaskWrapper httpTaskWrapper;
        private Throwable errorMessage;

        public LogEntry(HttpTaskWrapper httpTaskWrapper, Throwable errorMessage) {
            this.httpTaskWrapper = httpTaskWrapper;
            this.errorMessage = errorMessage;
        }

        public String toString() {
            if (errorMessage == null) {
                return httpTaskWrapper.getRequestInfoString();
            }
            return httpTaskWrapper.getRequestInfoString() + "; Error : " + errorMessage.getMessage();
        }

        public HttpTaskWrapper getHttpTaskWrapper() {
            return httpTaskWrapper;
        }

        public void setHttpTaskWrapper(HttpTaskWrapper httpTaskWrapper) {
            this.httpTaskWrapper = httpTaskWrapper;
        }

        public Throwable getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(Throwable errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    public String createSession() {

        synchronized (sessionMap) {
            HttpSession s = HttpSession.createHttpSession(true);
            sessionMap.put(s.id(), s);
            return s.id();
        }
    }

    /**
     * Looks up a session by ID and updates / returns it
     */
    public Session getSession(String id) {

        if (sessionMap.containsKey(id)) {
            HttpSession s = sessionMap.get(id);
            s.access();
            if (!s.isValid()) {
                return null;
            }
            Date date = new Date();
            if (date.getTime() > s.maxInactiveInterval() * 1000 + s.lastAccessedTime()) {
                s.invalidate();
                return null;
            }
            return s;
        }
        return null;
    }

    public void sessionCleaner() {

        synchronized (sessionMap) {
            List<String> deleteKeys = new ArrayList<>();
            Date date = new Date();
            for (Map.Entry<String, HttpSession> entry : sessionMap.entrySet()) {
                HttpSession session = entry.getValue();
                String key = entry.getKey();
                if (!session.isValid()) {
                    deleteKeys.add(key);
                    continue;
                }
                if (date.getTime() > session.lastAccessedTime() + session.maxInactiveInterval() * 1000) {
                    session.invalidate();
                    deleteKeys.add(key);
                }
            }
            deleteKeys.stream().forEach(key -> {
                System.out.println("removing Key");
                System.out.println(key);
                sessionMap.remove(key);
            });
        }

    }

    public HttpWebServiceImpl getHttpWebService() {
        return httpWebService;
    }
}
