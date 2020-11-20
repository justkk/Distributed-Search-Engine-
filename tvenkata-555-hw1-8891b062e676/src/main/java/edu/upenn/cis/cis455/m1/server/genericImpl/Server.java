package edu.upenn.cis.cis455.m1.server.genericImpl;

import edu.upenn.cis.cis455.exceptions.ServerHaltException;
import edu.upenn.cis.cis455.m1.server.*;
import edu.upenn.cis.cis455.m1.server.genericImpl.interfaces.ServerHandler;
import edu.upenn.cis.cis455.m1.server.genericImpl.models.ServerConfig;
import edu.upenn.cis.cis455.m1.server.genericImpl.models.ServerStatus;
import edu.upenn.cis.cis455.m1.server.genericImpl.interfaces.ThreadExecutorManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Map;

/***
 *
 * Generic Server.
 *
 */

public class Server {

    static final Logger logger = LogManager.getLogger(Server.class);

    /**
     * Server config.
     */

    private ServerConfig serverConfig;

    /**
     * Thread Executor
     */

    private InHouseThreadExecutor inHouseThreadExecutor;

    /**
     * To model the request.
     */

    private ServerHandler serverHandler;

    /**
     * Call backs Manager.
     */

    private ThreadExecutorManager threadExecutorManager;

    /**
     * Generic Listener.
     */
    private ServerListener serverListener;
    /***
     * Server Status
     */
    private final ServerStatus serverStatus;

    /**
     * listener thread
     */
    private Thread listnerThread;

    /***
     * Server Queue. shared with executor
     */
    private ThreadSafeQueue<Runnable> threadSafeQueue;

    public Server(ServerConfig serverConfig, ThreadExecutorManager threadExecutorManager, ServerHandler serverHandler, ThreadSafeQueue<Runnable> threadSafeQueue) {
        this.serverConfig = serverConfig;
        this.threadExecutorManager = threadExecutorManager;
        this.serverHandler = serverHandler;
        this.inHouseThreadExecutor = new InHouseThreadExecutor(serverConfig.getThreadCount(), threadExecutorManager, threadSafeQueue);
        serverStatus = new ServerStatus();
    }

    public ThreadSafeQueue<Runnable> getThreadSafeQueue() {
        return threadSafeQueue;
    }

    public ServerStatus getServerStatus() {
        return serverStatus;
    }

    public ServerConfig getServerConfig() {
        return serverConfig;
    }

    public void setServerConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    public InHouseThreadExecutor getInHouseThreadExecutor() {
        return inHouseThreadExecutor;
    }

    public void setInHouseThreadExecutor(InHouseThreadExecutor inHouseThreadExecutor) {
        this.inHouseThreadExecutor = inHouseThreadExecutor;
    }

    public ServerHandler getServerHandler() {
        return serverHandler;
    }

    public void setServerHandler(ServerHandler serverHandler) {
        this.serverHandler = serverHandler;
    }

    /***
     * Start the server : bind socket, start listener thread and executor.
     */

    public void start() {
        ServerSocketChannel sock = null;
        try {
            sock = ServerSocketChannel.open();
            logger.info("Socket init");
            sock.socket().bind(new InetSocketAddress(InetAddress.getByName(this.getServerConfig().getIpAddress()),
                    this.getServerConfig().getPort()));
            sock.configureBlocking(false);
            serverListener = new ServerListener(this, serverHandler, sock);

        } catch (IOException e) {
            throw new ServerHaltException(e.getMessage());
        }

        synchronized (this) {
            serverStatus.markActive();
        }
        this.inHouseThreadExecutor.start();
        listnerThread = new Thread(serverListener);
        listnerThread.start();
    }

    /**
     * Stop the server.
     */

    public void stop() {

        synchronized (this) {
            logger.info("Marking server as inactive");
            serverStatus.markInActive();
        }
    }

    /**
     * Wait till executors die.
     */

    public void closeWorkers() {

        logger.info("Closing thread executors");
        inHouseThreadExecutor.shutdown();
        logger.info("Waiting the server for termination");
        inHouseThreadExecutor.waitForTermination();
    }

    /**
     * submit task to the queue.
     * @param task
     */

    public void submitTask(Runnable task) {
        logger.info("Submitting a task to the queue");
        inHouseThreadExecutor.addTask(task);
    }

    /**
     * Fetch worker stats.
     * @return
     */
    public Map<String, String> getWorkerStats() {
        logger.info("Fetching the worker stats from the executor");
        return inHouseThreadExecutor.getWorkerStats();
    }

}
