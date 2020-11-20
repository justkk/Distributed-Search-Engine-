package edu.upenn.cis.cis455.storage.managers;

import edu.upenn.cis.cis455.model.hostCrawlDelayInfo.HostCrawlDelayInfo;

import java.io.Serializable;

public class CrawlDelayManagerOutput implements Serializable {
    private boolean success;
    private HostCrawlDelayInfo hostCrawlDelayInfo;

    public CrawlDelayManagerOutput(boolean success, HostCrawlDelayInfo hostCrawlDelayInfo) {
        this.success = success;
        this.hostCrawlDelayInfo = hostCrawlDelayInfo;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public HostCrawlDelayInfo getHostCrawlDelayInfo() {
        return hostCrawlDelayInfo;
    }

    public void setHostCrawlDelayInfo(HostCrawlDelayInfo hostCrawlDelayInfo) {
        this.hostCrawlDelayInfo = hostCrawlDelayInfo;
    }
}