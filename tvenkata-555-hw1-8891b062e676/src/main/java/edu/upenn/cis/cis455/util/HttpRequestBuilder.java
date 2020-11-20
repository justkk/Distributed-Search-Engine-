package edu.upenn.cis.cis455.util;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.server.enums.AcceptTypeEnum;
import edu.upenn.cis.cis455.m1.server.http.RequestTypeEnum;
import edu.upenn.cis.cis455.m1.server.implementations.HttpRequest;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class include the helper utilities for request building.
 */

public class HttpRequestBuilder {

    private static final String NEW_LINE_ENTRY = "\r\n";

    private static final String PROTOCOL_VERSION = "protocolversion";
    private static final String CONTENT_LENGTH = "content-length";
    private static final String HOST = "host";
    private static final String USER_AGENT = "user-agent";
    private static final String CONTENT_TYPE = "content-type";
    private static final String METHOD = "method";
    private static final int DEFAULT_PORT = 80;
    private static final String HOST_DELIMITER = ":";
    private static final int DEFAULT_CONTENT_LENGTH = 0;

    public static HttpRequest parseRequest(String remoteIp, InputStream inputStream) {

        try {

            String body = "";
            Map<String, String> headers = new HashMap<>();
            Map<String, List<String>> queryParams = new HashMap<>();
            String uriReturned = HttpParsing.parseRequest(remoteIp, inputStream, headers, queryParams);
            headers = cleanHeaders(headers);

            if (headers.containsKey(CONTENT_LENGTH)) {
                Integer length = Integer.valueOf(headers.get(CONTENT_LENGTH));
                byte[] buf = new byte[length];
                inputStream.read(buf, 0, length);
                body = new String(buf);
            }

            String uri = uriReturned.split("\\?")[0];
            HttpRequest httpRequest = new HttpRequest();
            httpRequest.setBody(body);
            httpRequest.setHeaders(headers);
            httpRequest.setProtocol(headers.getOrDefault(PROTOCOL_VERSION, null));
            httpRequest.setUri(uri);
            httpRequest.setContentLength(Integer.valueOf(headers.getOrDefault(CONTENT_LENGTH, String.valueOf(DEFAULT_CONTENT_LENGTH))));
            httpRequest.setContentType(headers.getOrDefault(CONTENT_TYPE, null));
            httpRequest.setHost(headers.getOrDefault(HOST, null));
            httpRequest.setPort(getPortFromHost(headers.getOrDefault(HOST, null)));
            httpRequest.setQueryString(getQueryStringFromUri(uriReturned));
            httpRequest.setSession(null);
            httpRequest.setUserAgent(headers.getOrDefault(USER_AGENT, null));
            httpRequest.setRequestType(RequestTypeEnum.getEnumFromString(headers.getOrDefault(METHOD, null)));
            httpRequest.setPathInfo(uri);
            httpRequest.setQueryParams(queryParams);
            httpRequest.setIp(getIpFromHost(headers.getOrDefault(HOST, null)));
            httpRequest.setCookies(getCookies(httpRequest));

            if("HTTP/1.1".equals(httpRequest.getProtocol())) {
                if(httpRequest.headers("Connection")==null) {
                    httpRequest.getHeaders().put("Connection".toLowerCase(), "keep-alive");
                }
            }

            httpRequest.setUrl(null);
            if (httpRequest.getHost() != null) {
                URL url = new URL("http://" + httpRequest.getHost() + uri);
                httpRequest.setUrl(url.toString());
            }

            return httpRequest;

        } catch (IOException e) {
            throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (Exception e) {

            if(e instanceof HaltException) {
                throw e;
            }

            throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
//        catch (URISyntaxException e) {
//            throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
//        }
    }

    public static RawRequest getHeadersAndBody(InputStream inputStream) {

        StringBuffer headerBuffer = new StringBuffer();
        StringBuffer bodyBuffer = new StringBuffer();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));

        try {

            String header = bufferedReader.readLine();
            while (header.length() > 0) {
                headerBuffer.append(header).append(NEW_LINE_ENTRY);
                header = bufferedReader.readLine();
            }

//            String bodyLine = bufferedReader.readLine();
//            while (bodyLine != null) {
//                bodyBuffer.append(bodyLine).append(NEW_LINE_ENTRY);
//                bodyLine = bufferedReader.readLine();
//            }

            return new RawRequest(headerBuffer.toString(), bodyBuffer.toString());

        } catch (IOException e) {
            throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }

    }

    public static int getPortFromHost(String host) {
        if (host == null || host.equals("")) {
            return -1;
            //throw new HaltException(HttpServletResponse.SC_BAD_REQUEST , "MalFormed Packed: Empty host");
        }
        String[] hostStrings = host.split(HOST_DELIMITER);
        if (hostStrings.length == 1) {
            return DEFAULT_PORT;
        }
        String portString = hostStrings[1];
        return Integer.valueOf(portString);
    }

    public static String getIpFromHost(String host) {
        if (host == null || host.equals("")) {
            return null;
            //throw new HaltException(HttpServletResponse.SC_BAD_REQUEST , "MalFormed Packed: Empty host");
        }
        String[] hostStrings = host.split(HOST_DELIMITER);
        return hostStrings[0];
    }

    public static String getQueryStringFromUri(String uri) {
        if (uri == null || uri.equals("")) {
            throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, "MalFormed Packed: Empty uri");
        }
        String[] tokens = uri.split("\\?");
        if (tokens.length == 1) {
            return "";
        }
        return tokens[1];
    }

    private static Map<String, String> getCookies(HttpRequest httpRequest) {

        Map<String, String> cookies = new HashMap<>();
        String cookieHeader = httpRequest.headers("cookie");
        if(cookieHeader == null) {
            return cookies;
        }
        String[] pairs = cookieHeader.split(";");
        for(String entry: pairs) {
            String[] keyValues = entry.split("=");
            if(keyValues.length == 2) {
                cookies.put(keyValues[0].trim(), keyValues[1].trim());
            }
        }
        return cookies;
    }


    public static void main(String[] args) {
        String request = "POST /helloWord?a=b HTTP/1.1\n" +
                "Host: 127.0.0.1:8080\n" +
                "Cache-Control: no-cache\n" +
                "Postman-Token: 25a70349-11c0-fe85-0ee8-728d076eb503\n" +
                "\n" +
                "TEAFADSFA\n" +
                "FDAFADFASDFA\n" +
                "fasdFSDFASDFSADFADS\n" +
                "FSADFADSFASDFADSFASDFAS";

    }

    private static Map<String, String> cleanHeaders(Map<String, String> headers) {

        Set<String> keySet = headers.keySet();

        Map<String, String> newHeaders = new HashMap<>();

        for(String key: keySet) {
            String value = headers.get(key);
            newHeaders.put(key.toLowerCase(), value);
        }
        return newHeaders;
    }


}
