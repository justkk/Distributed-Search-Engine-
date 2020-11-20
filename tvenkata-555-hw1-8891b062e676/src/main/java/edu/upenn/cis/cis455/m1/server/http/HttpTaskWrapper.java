package edu.upenn.cis.cis455.m1.server.http;

import edu.upenn.cis.cis455.Constants;
import edu.upenn.cis.cis455.ServiceFactory;
import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.exceptions.IllegalResponseStateException;
import edu.upenn.cis.cis455.exceptions.PacketProcessingException;
import edu.upenn.cis.cis455.m1.server.HttpIoHandler;
import edu.upenn.cis.cis455.m1.server.HttpServer;
import edu.upenn.cis.cis455.m1.server.HttpTask;
import edu.upenn.cis.cis455.m1.server.http.models.RouteMapEntry;
import edu.upenn.cis.cis455.m1.server.implementations.HttpRequest;
import edu.upenn.cis.cis455.m1.server.implementations.HttpResponse;
import edu.upenn.cis.cis455.m1.server.implementations.HttpWebServiceImpl;
import edu.upenn.cis.cis455.m1.server.interfaces.HttpRequestHandler;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.helper.CookieAndSessionHandler;
import edu.upenn.cis.cis455.util.HttpRequestBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * Rapper for Http Task.
 * Its of type runnable. It has a HttpRequestHandler. This will handle the request.
 * This code will be run by server worker.
 *
 */

public class HttpTaskWrapper extends HttpTask implements Runnable {

    static final Logger logger = LogManager.getLogger(HttpTaskWrapper.class);


    private HttpRequestHandler httpRequestHandler;
    private Exception exception = null;
    private Date latestAccessDate;
    private boolean resubmitted;
    private HttpServer httpServer;
    private int idleConnectionTimeOut;
    private int maxRequests = Constants.getInstance().getMAX_REQUEST_COUNT_PER_SOCKET();

    /***
     *
     * Request info map, used for debugging and control page information.
     */

    private Map<String, String> requestInfo;

    public HttpTaskWrapper(Socket socket, HttpRequestHandler httpRequestHandler, HttpServer httpServer) {
        super(socket);
        this.httpRequestHandler = httpRequestHandler;
        this.requestInfo = new HashMap<>();
        this.resubmitted = false;
        this.latestAccessDate = new Date();
        this.httpServer = httpServer;
        this.idleConnectionTimeOut = Constants.getInstance().getIDLE_CONNECTION_TIME();
    }

    private void populateRequestInfo(Request request) {
        requestInfo.put("Method", request.headers("method"));
        requestInfo.put("URI", request.uri());
    }

    public Map<String, String> getRequestInfo() {
        return requestInfo;
    }

    public String getRequestInfoString() {
        StringBuilder stringBuilder = new StringBuilder();
        requestInfo.entrySet().forEach(stringStringEntry -> {
            stringBuilder.append(stringStringEntry.getKey());
            stringBuilder.append(":");
            stringBuilder.append(stringStringEntry.getValue());
        });
        return stringBuilder.toString();
    }

    public void access() {
        this.latestAccessDate = new Date();
    }

    /***
     *
     * Code to handle the request. it calls the corresponding handlers and enriches the response.
     */

    public void resubmitPacket() {
        this.resubmitted = true;
        this.httpServer.submitTask(this);
    }

    public int getTimeOut(HttpRequest httpRequest) {

        if(httpRequest.headers("Keep-Alive")!=null) {
            String value = httpRequest.headers("Keep-Alive");
            String[] tokens = value.split(",");
            for(String token: tokens) {
                String[]lhsRhs = token.split("=");
                if(lhsRhs.length == 2) {
                    String lhs = lhsRhs[0];
                    String rhs = lhsRhs[1];
                    if("timeout".equals(lhs.toLowerCase())) {
                        return Integer.valueOf(rhs);
                    }
                }
            }
        }
        return Constants.getInstance().getIDLE_CONNECTION_TIME();

    }

    public int getMaxRequestCount(HttpRequest httpRequest) {

        if(httpRequest.headers("Keep-Alive")!=null) {
            String value = httpRequest.headers("Keep-Alive");
            String[] tokens = value.split(",");
            for(String token: tokens) {
                String[]lhsRhs = token.split("=");
                if(lhsRhs.length == 2) {
                    String lhs = lhsRhs[0];
                    String rhs = lhsRhs[1];
                    if("max".equals(lhs.toLowerCase())) {
                        return Integer.valueOf(rhs);
                    }
                }
            }
        }
        return Constants.getInstance().getIDLE_CONNECTION_TIME();

    }

    @Override
    public void run() {

        Integer setPersistentFag = 0;

        if(this.resubmitted) {

            this.maxRequests -= 1;

            if(this.latestAccessDate.getTime() + Constants.getInstance().getIDLE_CONNECTION_TIME()*1000
                    <= new Date().getTime() || this.maxRequests == 0) {
                // connection expired.
//                try {
//                    getSocket().close();
//                } catch (IOException e) {
//                    System.out.println("unable to close the socket");
//                }
                setPersistentFag = -1;

            }

        }

        HttpRequest httpRequest = null;
        HttpResponse httpResponse = null;
        Socket socket = getSocket();
        logger.info("Got a new Task");



        boolean connectionAlive = false;
        try {
            this.getSocket().setSoTimeout(Constants.getInstance().getSOCKET_TIME_OUT()*1000);

            httpRequest =  this.createRequest(socket, null, Constants.getInstance().isCONNECTION_ALIVE_SUPPORT(), null, null);

            if(!this.resubmitted) {
                this.idleConnectionTimeOut = getTimeOut(httpRequest);
                this.maxRequests = getMaxRequestCount(httpRequest);
                this.getSocket().setSoTimeout(idleConnectionTimeOut*1000);
            }

            this.access();

            /*
                Enrich Request;
             */

            if(this.httpServer.getHttpWebService()!=null) {

                HttpWebServiceImpl webService = this.httpServer.getHttpWebService();
                if(webService!=null && webService.getRoutesHandlerConfiguration()!=null) {
                    RoutesHandlerConfiguration routesHandlerConfiguration = webService.getRoutesHandlerConfiguration();
                    RouteMapEntry routeMapEntry = routesHandlerConfiguration.getRouteHandlerMapEntry(httpRequest);
                    if(routeMapEntry!=null) {
                        Map<String, String> routeParams = routeMapEntry.getRouteParams(httpRequest.getPathInfo());
                        httpRequest.setRouteParams(routeParams);
                    }
                }
            }

            logger.info(httpRequest.uri() + " " + httpRequest.requestMethod());
            httpResponse = new HttpResponse();
            populateRequestInfo(httpRequest);
            httpResponse.setRequestUri(httpRequest.uri());

            if(setPersistentFag == -1) {
                httpRequest.persistentConnection(false);
            }

            logger.info("Handling Request");
            httpRequestHandler.handle(httpRequest, httpResponse);
            logger.info("sending response");
            connectionAlive = HttpIoHandler.sendResponse(socket, httpRequest, httpResponse);
        }
        catch (IllegalResponseStateException e) {

            if(httpResponse.isCommited()) {
                connectionAlive = HttpIoHandler.sendResponse(socket, httpRequest, httpResponse);
            }

        } catch (HaltException e) {
            logger.info("handler raised an halt exception, sending exception ...." + e.getMessage());
            exception = e;
            try{
                connectionAlive = HttpIoHandler.sendException(socket, httpRequest, e);
            } catch (Exception ee) {
                exception = ee;
            }
        } catch (Exception e) {
            logger.info(" unable to handle exception ...", e.getMessage());
            e.printStackTrace();
            connectionAlive = HttpIoHandler.sendException(socket, httpRequest,
                    new HaltException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage()));
            exception = e;
        } finally {
            if (!connectionAlive) {
                try {
                    logger.info("closing socket");
                    socket.close();
                } catch (IOException e) {
                    throw new PacketProcessingException("Unable to close packet");
                }
            } else {
                resubmitPacket();
            }

            if(exception!=null) {
                /**
                 * To update the server logs.
                 */
                if(exception  instanceof RuntimeException) {
                    throw (RuntimeException) exception;
                } else {
                    throw new PacketProcessingException(exception.getMessage());
                }
            }
        }

    }


    public HttpRequest createRequest(Socket socket,
                                        String uri,
                                        boolean keepAlive,
                                        Map<String, String> headers,
                                        Map<String, List<String>> parms) {

        try {
            HttpRequest request = HttpRequestBuilder.parseRequest(socket.getRemoteSocketAddress().toString(),
                    new BufferedInputStream(socket.getInputStream()));
            request.setServerInstance(this.httpServer);
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



}
