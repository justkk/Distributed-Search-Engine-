package edu.upenn.cis.cis455.m1.server.genericImpl;

import edu.upenn.cis.cis455.exceptions.ServerHaltException;
import edu.upenn.cis.cis455.m1.server.genericImpl.interfaces.ServerHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

/***
 * Generic listener.
 * Listen to the socket and uses handler to model
 * enqueue the task to queue.
 */

public class ServerListener implements Runnable {

    static final Logger logger = LogManager.getLogger(ServerListener.class);

    private final Server server;
    private ServerHandler serverHandler;
    private ServerSocketChannel sock;

    public ServerListener(Server server, ServerHandler serverHandler, ServerSocketChannel sock) {
        this.server = server;
        this.serverHandler = serverHandler;
        this.sock = sock;
    }

    public void listenRequests() throws ServerHaltException {

        try {

            while (true) {
                synchronized (server) {
                    if (!server.getServerStatus().isActive()) {
                        logger.info("Closing the socket channel");
                        logger.info("Closed the socket channel");
                        logger.info("Closing  Worker Threads");
                        server.closeWorkers();
                        logger.info("Closed Worker Threads");
                        sock.socket().close();
                        return;
                    }
                }

                SocketChannel sc = sock.accept();
                if (sc != null) {
                    logger.info("Received a Request");
                    Socket socket = sc.socket();
                    Runnable task = serverHandler.modelRequest(socket);
                    server.submitTask(task);
                }
            }

        } catch (IOException e) {
            logger.error(" Server start failed", e);
            throw new ServerHaltException(e.getMessage());
        }

    }

    @Override
    public void run() {
        try {
            listenRequests();
        } catch (ServerHaltException e) {
            server.stop();
            server.closeWorkers();
        }
    }
}
