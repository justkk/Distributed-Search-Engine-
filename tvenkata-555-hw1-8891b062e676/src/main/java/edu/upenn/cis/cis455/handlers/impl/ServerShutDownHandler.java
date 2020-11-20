package edu.upenn.cis.cis455.handlers.impl;

import edu.upenn.cis.cis455.handlers.Route;
import edu.upenn.cis.cis455.m1.server.HttpServer;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;

import javax.servlet.http.HttpServletResponse;

/*
 ***
 * ServerShutDownHandler is the route handler for /shutdown
 * It has server instance as a parameter.
 * It calls server shutdown call.
 */

public class ServerShutDownHandler implements Route {

    private HttpServer httpServer;

    public ServerShutDownHandler(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {
        httpServer.stopServer();
        response.status(HttpServletResponse.SC_OK);
        return null;
    }
}
