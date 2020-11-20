package edu.upenn.cis.cis455.m1.server.implementations;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.exceptions.IllegalResponseStateException;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;

import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse extends Response {

    Map<String, String> headers;

    //Map<String, Cookie> cookies;

    Map<String, Map<String, Cookie>> pathCookies;

    private boolean commited = false;

    private String requestUri = null;

    private void checkRequestState(){
        if(commited) {
            throw new IllegalResponseStateException("Response already commited");
        }
    }


    public HttpResponse() {
        this.headers = new HashMap<>();
        this.pathCookies = new HashMap<>();
    }


    public void status(int statusCode) {
        checkRequestState();
        this.statusCode = statusCode;
    }


    public void bodyRaw(byte[] b) {
        checkRequestState();
        body = b;
    }

    public void body(String body) {
        checkRequestState();
        this.body = body == null ? null : body.getBytes();
    }


    public void type(String contentType) {
        checkRequestState();
        this.contentType = contentType;
    }







    @Override
    public void header(String header, String value) {
        checkRequestState();
        headers.put(header, value);
    }

    @Override
    public void redirect(String location) {
        checkRequestState();
        this.status(HttpServletResponse.SC_FOUND);
        headers.put("Location", getUrl(getRequestUri(), location));
        commited = true;
    }

    @Override
    public void redirect(String location, int httpStatusCode) {
        checkRequestState();
        this.status(httpStatusCode);
        headers.put("Location", getUrl(getRequestUri(), location));
        commited = false;
    }

    @Override
    public void cookie(String name, String value) {
//        Cookie c = new Cookie(name, value);
//        cookies.put(name, c);
        cookie("/", name, value);
    }

    @Override
    public void cookie(String name, String value, int maxAge) {
//        Cookie c = new Cookie(name, value, maxAge);
//        cookies.put(name, c);
        cookie("/", name, value, maxAge);
    }

    @Override
    public void cookie(String name, String value, int maxAge, boolean secured) {
//        Cookie c = new Cookie(name, value, secured, maxAge);
//        cookies.put(name, c);
        cookie("/", name, value, maxAge, secured);
    }

    @Override
    public void cookie(String name, String value, int maxAge, boolean secured, boolean httpOnly) {
//        Cookie c = new Cookie(name, value, secured, httpOnly, maxAge);
//        cookies.put(name, c);
        cookie("/", name, value, maxAge, secured, httpOnly);
    }

    @Override
    public void cookie(String path, String name, String value) {
        Cookie c = new Cookie(name, value, path);
        insertPathCookie(path, c);
    }

    @Override
    public void cookie(String path, String name, String value, int maxAge) {
        Cookie c = new Cookie(name, value, path, maxAge);
        insertPathCookie(path, c);
    }

    @Override
    public void cookie(String path, String name, String value, int maxAge, boolean secured) {
        Cookie c = new Cookie(name, value, path, secured, maxAge);
        insertPathCookie(path, c);
    }

    @Override
    public void cookie(String path, String name, String value, int maxAge, boolean secured, boolean httpOnly) {
        Cookie c = new Cookie(name, value, path, secured, httpOnly, maxAge);
        insertPathCookie(path, c);
    }

    @Override
    public void removeCookie(String name) {
        removeCookie("/", name);
    }

    @Override
    public void removeCookie(String path, String name) {
        if(pathCookies.containsKey(path) && pathCookies.get(path).containsKey(name)) {
            Cookie c = pathCookies.get(path).get(name);
            c.inValidate();
            return;
        }
        Cookie deletedCookie = new Cookie(name, "", path);
        deletedCookie.inValidate();
        if(!pathCookies.containsKey(path)) {
            pathCookies.put(path, new HashMap<>());
        }
        pathCookies.get(path).put(name, deletedCookie);
    }

    @Override
    public String getHeaders() {

        StringBuilder stringBuilder = new StringBuilder();
        for (String key : headers.keySet()) {
            stringBuilder.append(key);
            stringBuilder.append(":");
            stringBuilder.append(" ");
            stringBuilder.append(headers.get(key));
            stringBuilder.append("\r\n");
        }

        // Add Cookie information in headers

        pathCookies.entrySet().forEach(stringMapEntry -> {
            Map<String, Cookie> cookieMap = stringMapEntry.getValue();
            cookieMap.entrySet().forEach(cookieEntry -> {
                stringBuilder.append(cookieEntry.getValue().getSetCookieString());
                stringBuilder.append("\r\n");
            });
        });

        return stringBuilder.toString();
    }

    private void insertPathCookie(String path, Cookie c) {
        if(!pathCookies.containsKey(path)) {
            pathCookies.put(path, new HashMap<>());
        }
        pathCookies.get(path).put(c.getName(), c);
    }

    public boolean isCommited() {
        return commited;
    }

    public void setCommited(boolean commited) {
        this.commited = commited;
    }


    private String getUrl(String requestUri, String location) {


        if (location.startsWith("/")) {
            return location;
        }

        else {
            String rootPath = requestUri.endsWith("/") ? requestUri : getParentPath(requestUri);
            location = joinPaths(rootPath, location);
            if (location == null) {
                throw new HaltException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Bad Route Configuration");
            }

           return location;
        }

    }

    private String joinPaths(String path1, String path2) {

        if(path1 == null) {
            return path2;
        }

        if(path2 == null) {
            return null;
        }

        return path1 + path2;
    }


    private String getParentPath(String path) {

        if(path == null) {
            return null;
        }
        if("/".equals(path)) {
            return path;
        }

        int splitIndex = path.lastIndexOf(47, path.length() - 2);
        return splitIndex >= 0 ? path.substring(0, splitIndex + 1) : null;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }
}
