package edu.upenn.cis.cis455.m1.server.http.models;

import java.util.Map;

/**
 * Worker info POJO.
 */

public class HttpWorkerInfo {

    private String name;
    private String state;
    private Map<String, String> requestInfo;

    public HttpWorkerInfo(String name) {
        this.name = name;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Map<String, String> getRequestInfo() {
        return requestInfo;
    }

    public void setRequestInfo(Map<String, String> requestInfo) {
        this.requestInfo = requestInfo;
    }
}
