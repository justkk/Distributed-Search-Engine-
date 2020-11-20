package edu.upenn.cis.cis455.m1.server.http;

import edu.upenn.cis.cis455.m1.server.HttpServer;
import edu.upenn.cis.cis455.m1.server.genericImpl.interfaces.ServerHandler;
import edu.upenn.cis.cis455.m1.server.interfaces.HttpRequestHandler;

import java.net.Socket;

/***
 * Its a implementation of server handler.
 * Each server will have its own handler. gives flexibility to use the basic Server for different protocols.
 * Server listener, a generic listener will call this function to model request.
 */

public class HttpServerHandler implements ServerHandler {

    private HttpServer httpServer;
    private HttpRequestHandler httpRequestHandler;

    public HttpServerHandler(HttpServer httpServer, HttpRequestHandler httpRequestHandler) {
        this.httpServer = httpServer;
        this.httpRequestHandler = httpRequestHandler;
    }


    @Override
    public Runnable modelRequest(Socket socket) {

        return new HttpTaskWrapper(socket, httpRequestHandler, httpServer);
    }

}
