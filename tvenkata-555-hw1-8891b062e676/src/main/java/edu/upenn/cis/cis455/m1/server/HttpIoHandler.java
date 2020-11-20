package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.Constants;
import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.exceptions.IllegalResponseStateException;
import edu.upenn.cis.cis455.exceptions.PacketProcessingException;
import edu.upenn.cis.cis455.m1.server.http.MatchConfiguration;
import edu.upenn.cis.cis455.m1.server.implementations.HttpResponse;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import edu.upenn.cis.cis455.util.HttpResponseBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Handles marshalling between HTTP Requests and Responses
 */
public class HttpIoHandler {

    final static Logger logger = LogManager.getLogger(HttpIoHandler.class);

    private MatchConfiguration matchConfiguration;

    public static final String CONNECTION_ALIVE = "Connection";

    public HttpIoHandler(MatchConfiguration matchConfiguration) {
        this.matchConfiguration = matchConfiguration;
    }

    /**
     * Sends an exception back, in the form of an HTTP response code and message.  Returns true
     * if we are supposed to keep the connection open (for persistent connections).
     */


    public static boolean sendException(Socket socket, Request request, HaltException except) {

        HttpResponse response = new HttpResponse();
        response.status(except.statusCode());
        response.body(except.body());
        if(except.body()!=null) return sendResponse(socket, request, response);

        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());

            if (request == null) {
                out.write("\r\n".getBytes());
                out.flush();
                return false;
            }
            try {
                HttpResponseBuilder.enrichResponse(request, response);
            } catch (IllegalResponseStateException e) {
                logger.error(e);
            }

            String statusAndHeaders = HttpResponseBuilder.getStatusAndHeaderContent(request, response);
            out.write(statusAndHeaders.getBytes());
            out.write("\r\n".getBytes());
            if (Constants.getInstance().isCHUNKED_ENCODING()) {

                out.write(Integer.toHexString(0).getBytes());
                out.write("\r\n".getBytes());
                out.write("\r\n".getBytes());

            } else {
                out.write("\r\n".getBytes());
            }
            out.flush();
        } catch (IOException e) {
            throw new PacketProcessingException(e);
        }
        return request.persistentConnection();
    }

    /**
     * Sends data back.   Returns true if we are supposed to keep the connection open (for
     */
    public static boolean sendResponse(Socket socket, Request request, Response response) {

        boolean keepConnectionAlive = false;
        if (response != null && socket != null) {
            try {
                try {
                    HttpResponseBuilder.enrichResponse(request, response);
                } catch (IllegalResponseStateException e) {
                    logger.error(e);
                }

                String statusAndHeaders = HttpResponseBuilder.getStatusAndHeaderContent(request, response);
                DataOutputStream out = new DataOutputStream(socket.getOutputStream());

                out.write(statusAndHeaders.getBytes());
                out.write("\r\n".getBytes());

                if(Constants.getInstance().isCHUNKED_ENCODING()) {
                    HttpResponseBuilder.doChunkedEncoding(out, request, response);
                } else {
                    HttpResponseBuilder.doDirectTransfer(out, request, response);
                }

                out.flush();
            } catch (IOException e) {
                throw new PacketProcessingException(e);
            } catch (HaltException haltException) {
                throw haltException;
            } catch (Exception e) {
                throw new HaltException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            }

        } else {
            throw new HaltException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unknown Cause");
        }

        return request.persistentConnection();
    }


    public void processRequest(Request request, Response response) {

//        HaltException staticFileError = null;
//        try {
//            matchConfiguration.getStaticFileConfiguration().applyFileConfiguration(request, response);
//        } catch (HaltException h) {
//            staticFileError = h;
//        }
//
//
//        if(staticFileError == null) {
//            try {
//                Thread.sleep(20000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            return;
//        }
//
//        // else: -- get Path and solve shit.
//        RouteContext routeContext = new RouteContext(request, response);
//        matchConfiguration.getRoutesHandlerConfiguration().handleRoute(routeContext);

    }


}
