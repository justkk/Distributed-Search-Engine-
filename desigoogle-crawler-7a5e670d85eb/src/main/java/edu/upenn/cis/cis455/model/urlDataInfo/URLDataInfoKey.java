package edu.upenn.cis.cis455.model.urlDataInfo;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

import java.io.Serializable;

@Persistent
public class URLDataInfoKey implements Serializable {

    @KeyField(1) private String protocol;
    @KeyField(2) private String host;
    @KeyField(3) private int port;
    @KeyField(4) private String filePath;
    @KeyField(5) private String requestMethod;

    public URLDataInfoKey() {
    }

    public URLDataInfoKey(String protocol, String host, int port, String filePath, String requestMethod) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.filePath = filePath;
        this.requestMethod = requestMethod;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getLocation() {
        return protocol + "://" + host + ":" + port + filePath;
    }

    public String getKeyString() {
        return requestMethod + ":" + protocol + "://" + host + ":" + port + filePath;
    }
}
