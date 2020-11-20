package edu.upenn.cis.cis455.m1.server.genericImpl.models;

import edu.upenn.cis.cis455.Constants;

public class ServerConfig {

    private String ipAddress = "127.0.0.1";
    private Integer port = 8088;
    private Integer threadCount = Constants.getInstance().getTHREAD_COUNT();

    public ServerConfig() {
    }

    public ServerConfig(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }
}
