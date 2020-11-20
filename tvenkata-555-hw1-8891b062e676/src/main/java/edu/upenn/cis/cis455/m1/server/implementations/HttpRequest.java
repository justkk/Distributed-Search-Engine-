package edu.upenn.cis.cis455.m1.server.implementations;

import edu.upenn.cis.cis455.ServiceFactory;
import edu.upenn.cis.cis455.m1.server.HttpServer;
import edu.upenn.cis.cis455.m1.server.enums.AcceptTypeEnum;
import edu.upenn.cis.cis455.m1.server.http.RequestTypeEnum;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HttpRequest extends Request {


    private Session session;

    private Map<String, String> routeParams;

    private String queryString;

    private Map<String, List<String>> queryParams;

    private RequestTypeEnum requestType;

    private String host;
    private String userAgent;
    private int port;
    private String pathInfo;

    private String url;
    private String uri;

    private String protocol;
    private String contentType;
    private String ip;
    private String body;
    private int contentLength;

    private HttpServer serverInstance;

    private Map<String, Object> attributes = new HashMap<>();

    private Map<String, String> headers;
    private Map<String, String> cookies = new HashMap<>();

    public Map<String, String> getCookies() {
        return cookies;
    }

    public HttpRequest(HttpServer httpServer) {
        this.serverInstance = httpServer;
    }

    public HttpRequest() {
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }


    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    public RequestTypeEnum getRequestType() {
        return requestType;
    }

    public void setRequestType(RequestTypeEnum requestType) {
        this.requestType = requestType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public Map<String, String> getRouteParams() {
        return routeParams;
    }

    public void setRouteParams(Map<String, String> routeParams) {
        this.routeParams = routeParams;
    }

    public Map<String, List<String>> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, List<String>> queryParams) {
        this.queryParams = queryParams;
    }


    @Override
    public Session session() {
        if(this.session!=null) {
            this.session.access();
        }
        return this.session;
    }

    @Override
    public Session session(boolean create) {

        if(serverInstance == null) {
            return null;
        }

        if(create && session == null) {
            String sessionId  = serverInstance.createSession();
            this.session = serverInstance.getSession(sessionId);
        }
        return session();
    }

    @Override
    public Map<String, String> params() {
        return routeParams;
    }

    @Override
    public String queryParams(String param) {
        List<String> values = queryParams.get(param);
        if (values.size() == 1) {
            return values.get(0);
        }
        // TODO: How to handle list param return type.
        return null;
    }

    @Override
    public List<String> queryParamsValues(String param) {
        return queryParams.get(param);
    }

    @Override
    public Set<String> queryParams() {
        return queryParams.keySet();
    }

    @Override
    public String queryString() {
        return queryString;
    }

    @Override
    public void attribute(String attrib, Object val) {
        attributes.put(attrib, val);
    }

    @Override
    public Object attribute(String attrib) {
        return attributes.getOrDefault(attrib, null);
    }

    @Override
    public Set<String> attributes() {
        return attributes.keySet();
    }

    @Override
    public Map<String, String> cookies() {
        return cookies;
    }

    @Override
    public String requestMethod() {
        return getRequestType().getType();
    }

    @Override
    public String host() {
        return getHost();
    }

    @Override
    public String userAgent() {
        return getUserAgent();
    }

    @Override
    public int port() {
        return getPort();
    }

    @Override
    public String pathInfo() {
        return getPathInfo();
    }

    @Override
    public String url() {
        return getUrl();
    }

    @Override
    public String uri() {
        return getUri();
    }

    @Override
    public String protocol() {
        return getProtocol();
    }

    @Override
    public String contentType() {
        return getContentType();
    }

    @Override
    public String ip() {
        return getIp();
    }

    @Override
    public String body() {
        return getBody();
    }

    @Override
    public int contentLength() {
        return getContentLength();
    }

    @Override
    public String headers(String name) {
        if (headers.containsKey(name.toLowerCase())) {
            return headers.get(name.toLowerCase());
        }
        return null;
    }

    @Override
    public Set<String> headers() {
        return headers.keySet();
    }

    public HttpServer getServerInstance() {
        return serverInstance;
    }

    public void setServerInstance(HttpServer serverInstance) {
        this.serverInstance = serverInstance;
    }


}
