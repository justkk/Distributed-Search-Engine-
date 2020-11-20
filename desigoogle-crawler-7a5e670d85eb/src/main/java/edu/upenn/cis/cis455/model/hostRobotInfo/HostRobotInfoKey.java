package edu.upenn.cis.cis455.model.hostRobotInfo;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

import java.io.Serializable;

@Persistent
public class HostRobotInfoKey implements Serializable {

    @KeyField(1)
    private String protocol;
    @KeyField(2)
    private String host;
    @KeyField(3)
    private int port;

    public HostRobotInfoKey() {
    }

    public HostRobotInfoKey(String protocol, String host, int port) {
        this.protocol = protocol;
        this.host = host;
        this.port = port;
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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean equals(Object obj) {
        if (obj instanceof HostRobotInfoKey) {
            HostRobotInfoKey key = (HostRobotInfoKey) obj;
            if (this.host.equals(key.getHost()) && this.port == key.getPort() && this.protocol.equals(key.getProtocol())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        String key = host + ":" + String.valueOf(port);
        return key.hashCode();
    }
}
