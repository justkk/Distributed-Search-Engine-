package edu.upenn.cis.cis455.util;

import edu.upenn.cis.cis455.Constants;
import edu.upenn.cis.cis455.exceptions.PacketProcessingException;
import edu.upenn.cis.cis455.m1.server.implementations.Cookie;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import edu.upenn.cis.cis455.m2.server.interfaces.Session;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * This class include the helper utilities for response writing.
 */

public class HttpResponseBuilder {

    private static Map<String, Map<String, String>> httpResponseCodes = new HashMap<>();


    private static String[] PROTOCOLS = {"HTTP/1.1", "HTTP/1.0"};

    static {

        for(String s: PROTOCOLS) {
            httpResponseCodes.put(s, new HashMap<>());
        }

        Map http10Codes = httpResponseCodes.get("HTTP/1.0");

        http10Codes.put("200", "OK");
        http10Codes.put("201", "Created");
        http10Codes.put("202", "Accepted");
        http10Codes.put("204", "No Content");
        http10Codes.put("301", "Moved Permanently");
        http10Codes.put("302", "Moved Temporarily");
        http10Codes.put("304", "Not Modified");
        http10Codes.put("400", "Bad Request");
        http10Codes.put("401", "Unauthorized");
        http10Codes.put("403", "Forbidden");
        http10Codes.put("404", "Not Found");
        http10Codes.put("500", "Internal Server Error");
        http10Codes.put("501", "Not Implemented");
        http10Codes.put("502", "Bad Gateway");
        http10Codes.put("503", "Service Unavailable");

        Map http11Codes = httpResponseCodes.get("HTTP/1.1");

        http11Codes.put("100", "Continue");
        http11Codes.put("101", "Switching Protocols");
        http11Codes.put("200", "OK");
        http11Codes.put("201", "Created");
        http11Codes.put("202", "Accepted");
        http11Codes.put("203", "Non-Authoritative Information");
        http11Codes.put("204", "No Content");
        http11Codes.put("205", "Reset Content");
        http11Codes.put("206", "Partial Content");
        http11Codes.put("300", "Multiple Choices");
        http11Codes.put("301", "Moved Permanently");
        http11Codes.put("302", "Found");
        http11Codes.put("303", "See Other");
        http11Codes.put("304", "Not Modified");
        http11Codes.put("307", "Temporary Redirect");
        http11Codes.put("308", "Permanent Redirect");
        http11Codes.put("400", "Bad Request");
        http11Codes.put("401", "Unauthorized");
        http11Codes.put("403", "Forbidden");
        http11Codes.put("404", "Not Found");
        http11Codes.put("405", "Method Not Allowed");
        http11Codes.put("406", "Not Acceptable");
        http11Codes.put("407", "Proxy Authentication Required");
        http11Codes.put("408", "Request Timeout");
        http11Codes.put("409", "Conflict");
        http11Codes.put("410", "Gone");
        http11Codes.put("411", "Length Required");
        http11Codes.put("412", "Precondition Failed");
        http11Codes.put("413", "Payload Too Large");
        http11Codes.put("414", "URI Too Long");
        http11Codes.put("415", "Unsupported Media Type");
        http11Codes.put("416", "Range Not Satisfiable");
        http11Codes.put("417", "Expectation Failed");
        http11Codes.put("418", "I'm a teapot");
        http11Codes.put("422", "Unprocessable Entity");
        http11Codes.put("425", "Too Early");
        http11Codes.put("426", "Upgrade Required");
        http11Codes.put("428", "Precondition Required");
        http11Codes.put("429", "Too Many Requests");
        http11Codes.put("431", "Request Header Fields Too Large");
        http11Codes.put("451", "Unavailable For Legal Reasons");
        http11Codes.put("500", "Internal Server Error");
        http11Codes.put("501", "Not Implemented");
        http11Codes.put("502", "Bad Gateway");
        http11Codes.put("503", "Service Unavailable");
        http11Codes.put("504", "Gateway Timeout");
        http11Codes.put("505", "HTTP Version Not Supported");
        http11Codes.put("511", "Network Authentication Required");

    }

    private static SimpleDateFormat formatter1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");


    /**
     * Get the Http Status line.
     * @param request
     * @param response
     * @return
     */

    public static String getStautsLine(Request request, Response response) {

        String responseNumber = String.valueOf(response.status());

        if(!httpResponseCodes.containsKey(request.protocol())) {
            throw new PacketProcessingException("PROTOCOL NOT FOUND");
        }

        Map<String, String> responseCodes = httpResponseCodes.get(request.protocol());

        if(!responseCodes.containsKey(responseNumber)) {
            throw new PacketProcessingException("RESPONSE CODE NOT FOUND");
        }
        return request.protocol() + " " + responseNumber + " " + responseCodes.get(responseNumber) + "\r\n";

    }

    /**
     * Enrich response with headers.
     * @param request
     * @param response
     */

    public static void enrichResponse(Request request, Response response) {



        if(response.type()!=null)
            response.header("Content-Type", response.type());

        if(request.persistentConnection()) {
            response.header("Connection", "keep-alive");
        } else {
            //response.header("Connection", "keep-alive");
            response.header("Connection", "closed");
        }

        if(Constants.getInstance().isCHUNKED_ENCODING()) {
            response.header("Transfer-Encoding", "chunked");
        } else {
            if(response.bodyRaw() == null) {
                response.header("Content-Length", "0");
            } else {
                response.header("Content-Length", String.valueOf(response.bodyRaw().length));
            }
        }

        Date d = new Date();
        formatter1.setTimeZone(TimeZone.getTimeZone("GMT"));
        String date = formatter1.format(d);
        if(response.status() > 100)
            response.header("Date", date);

    }

    /**
     * Response enrichment. Function to handle session related cookies.
     * @param request
     * @param response
     */

    public static void checkSession(Request request, Response response) {

        /*
        Check the session is already known or need to be created.
         */

        if(request.session()!=null && request.cookie("JSESSIONID")!=null &&
                !request.session().id().equals(request.cookie("JSESSIONID"))) {
            // New session is created in the request. So session information has to be updated in the cookie.
            Session newSession = request.session();
            if(newSession.maxInactiveInterval() == -1) {
                /* means session is closed; mention it to client*/
                response.removeCookie("/", "JSESSIONID");
            } else {
                response.cookie("/", "JSESSIONID", newSession.id(),
                        Constants.getInstance().getSESSION_AGE());
            }
        }

        else if(request.cookie("JSESSIONID") == null && request.session()!=null) {
            response.cookie("/", "JSESSIONID", request.session().id(),
                    Constants.getInstance().getSESSION_AGE());
        }

        else if(request.cookie("JSESSIONID") != null && request.session() == null) {
            response.removeCookie("/", "JSESSIONID");
        }

        else if(request.cookie("JSESSIONID") != null && request.session().maxInactiveInterval() == -1) {
            response.removeCookie("/", "JSESSIONID");
        }


    }

    public static String getStatusAndHeaderContent(Request request, Response response) {
        checkSession(request, response);
        return getStautsLine(request, response) + response.getHeaders();
    }

    /**
     * Do direct transfer with out any chuncked encoding;
     * @param out
     * @param request
     * @param response
     * @throws IOException
     */

    public static void doDirectTransfer(DataOutputStream out, Request request, Response response) throws IOException {
        if (response.bodyRaw() != null)
            out.write(response.bodyRaw());
    }

    /**
     * Do chuncked Encoding. This feature can be toggled in Constants class.
     * @param out
     * @param request
     * @param response
     * @throws IOException
     */

    public static void doChunkedEncoding(DataOutputStream out, Request request, Response response) throws IOException {

        if(response.bodyRaw() == null) {
            out.write(Integer.toHexString(0).getBytes());
            out.write("\r\n".getBytes());
            out.write("\r\n".getBytes());
            return;
        }

        byte[] myBuffer = new byte[Constants.getInstance().getCHUNK_SIZE()];
        int bytesRead = 0;
        InputStream in = new ByteArrayInputStream(response.bodyRaw());
        while ((bytesRead = in.read(myBuffer,0,Constants.getInstance().getCHUNK_SIZE())) != -1) {

            byte[] writingData = myBuffer;
            if(bytesRead!=Constants.getInstance().getCHUNK_SIZE()) {
                writingData = Arrays.copyOfRange(myBuffer, 0 , bytesRead);
            }

            out.write( Integer.toHexString(bytesRead).getBytes());
            out.write("\r\n".getBytes());
            out.write(writingData);
            out.write("\r\n".getBytes());

        }

        out.write(Integer.toHexString(0).getBytes());
        out.write("\r\n".getBytes());
        out.write("\r\n".getBytes());

    }

}
