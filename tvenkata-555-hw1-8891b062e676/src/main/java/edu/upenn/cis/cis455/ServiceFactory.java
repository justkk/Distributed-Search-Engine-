package edu.upenn.cis.cis455;

import edu.upenn.cis.cis455.exceptions.ServerHaltException;
import edu.upenn.cis.cis455.m1.server.handlers.DefaultRequestHandler;
import edu.upenn.cis.cis455.m1.server.handlers.HttpGetRequestHandler;
import edu.upenn.cis.cis455.m1.server.handlers.HttpHeadRequestHandler;
import edu.upenn.cis.cis455.m1.server.http.*;
import edu.upenn.cis.cis455.m1.server.http.models.RouteMapEntry;
import edu.upenn.cis.cis455.m1.server.implementations.HttpRequest;
import edu.upenn.cis.cis455.m1.server.implementations.HttpResponse;
import edu.upenn.cis.cis455.m1.server.implementations.HttpSession;
import edu.upenn.cis.cis455.m1.server.implementations.HttpWebServiceImpl;
import edu.upenn.cis.cis455.m1.server.interfaces.HttpRequestHandler;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import edu.upenn.cis.cis455.m2.server.interfaces.Session;
import edu.upenn.cis.cis455.m2.server.interfaces.WebService;
import edu.upenn.cis.cis455.m2.server.interfaces.helper.CookieAndSessionHandler;
import edu.upenn.cis.cis455.util.HttpRequestBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * Don't use Service Factory if we need to instantiate multiple servers.
 *
 */
public class ServiceFactory {


    static final Logger logger = LogManager.getLogger(ServiceFactory.class);


    private static HttpWebServiceImpl webService = null;

    /**
     * Get the HTTP server associated with port 8080
     */
    public static synchronized WebService getServerInstance() {
        if (webService == null) {
            logger.info("Creating Http WebService");
            webService = new HttpWebServiceImpl();
        }
        return webService;
    }

    /**
     * Create an HTTP request given an incoming socket
     */
    public static Request createRequest(Socket socket,
                                        String uri,
                                        boolean keepAlive,
                                        Map<String, String> headers,
                                        Map<String, List<String>> parms) {

        try {
            HttpRequest request = HttpRequestBuilder.parseRequest(socket.getRemoteSocketAddress().toString(),
                    new BufferedInputStream(socket.getInputStream()));

            HttpWebServiceImpl httpWebService = (HttpWebServiceImpl) getServerInstance();

            request.setServerInstance(httpWebService.getHttpServer());

            CookieAndSessionHandler.enrichRequestWithSession(request);
            if(request.headers("Connection")!=null && !"closed".equals(
                    request.headers("Connection").toLowerCase())) {
                request.persistentConnection(keepAlive);
            } else {
                request.persistentConnection(false);
            }

            return request;
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * Gets a request handler for files (i.e., static content) or dynamic content
     */
    public static HttpRequestHandler createRequestHandlerInstance(Path serverRoot) {

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

    /**
     * Gets a new HTTP Response object
     */
    public static Response createResponse() {
        return new HttpResponse();
    }

    /**
     * Creates a blank session ID and registers a Session object for the request
     */
    public static String createSession() {
        if(webService == null)
            return null;
        return webService.createSession();
    }

    /**
     * Looks up a session by ID and updates / returns it
     */
    public static Session getSession(String id) {
        if(webService == null)
            return null;
        return webService.getSession(id);
    }
}