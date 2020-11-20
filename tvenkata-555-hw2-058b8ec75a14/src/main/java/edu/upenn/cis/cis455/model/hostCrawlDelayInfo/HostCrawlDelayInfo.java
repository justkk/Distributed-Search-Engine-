package edu.upenn.cis.cis455.model.hostCrawlDelayInfo;

import com.sleepycat.persist.model.PrimaryKey;

import java.util.Date;

public class HostCrawlDelayInfo {

    @PrimaryKey
    private HostCrawlDelayInfoKey hostCrawlDelayInfoKey;
    private Date lastAccessedTime;
    private String host;
    private int port;

    public HostCrawlDelayInfo(HostCrawlDelayInfoKey hostCrawlDelayInfoKey, Date lastAccessedTime) {
        this.hostCrawlDelayInfoKey = hostCrawlDelayInfoKey;
        this.lastAccessedTime = lastAccessedTime;
        this.host = hostCrawlDelayInfoKey.getHost();
        this.port = hostCrawlDelayInfoKey.getPort();
    }

    public HostCrawlDelayInfoKey getHostCrawlDelayInfoKey() {
        return hostCrawlDelayInfoKey;
    }

    public Date getLastAccessedTime() {
        return lastAccessedTime;
    }

    public void setLastAccessedTime(Date lastAccessedTime) {
        this.lastAccessedTime = lastAccessedTime;
    }
}
