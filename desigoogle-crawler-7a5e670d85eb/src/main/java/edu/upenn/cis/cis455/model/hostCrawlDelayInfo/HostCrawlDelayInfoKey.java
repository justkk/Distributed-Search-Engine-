package edu.upenn.cis.cis455.model.hostCrawlDelayInfo;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;
import edu.upenn.cis.cis455.model.hostRobotInfo.HostRobotInfoKey;

import java.io.Serializable;

@Persistent
public class HostCrawlDelayInfoKey implements Serializable {

    @KeyField(1) private String host;
    @KeyField(2) private int port;
    @KeyField(3) private String protocol;


    public HostCrawlDelayInfoKey(String host, int port, String protocol) {
        this.host = host;
        this.port = port;
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

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public boolean equals(Object obj) {
        if(obj instanceof HostCrawlDelayInfoKey) {
            HostCrawlDelayInfoKey key = (HostCrawlDelayInfoKey) obj;
            if(this.host.equals(key.getHost()) && this.port == key.getPort() && this.protocol.equals(key.getProtocol())) {
                return true;
            }
        }
        return false;
    }

    public int hashCode() {
        String key =  host + ":" + String.valueOf(port);
        return key.hashCode();
    }
}
