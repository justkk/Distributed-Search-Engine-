package edu.upenn.cis.cis455.storage.managers;

import edu.upenn.cis.cis455.model.hostCrawlDelayInfo.HostCrawlDelayInfo;
import edu.upenn.cis.cis455.model.hostCrawlDelayInfo.HostCrawlDelayInfoKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class HostCrawlDelayManager {

    private ConcurrentHashMap<HostCrawlDelayInfoKey, HostCrawlDelayInfo> crawlDelayInfoMap = new ConcurrentHashMap();
    private Logger logger = LogManager.getLogger(HostCrawlDelayManager.class);

    public CrawlDelayManagerOutput getHostCrawlDelayInfoSOU(HostCrawlDelayInfoKey hostCrawlDelayInfoKey, Date newDate,
                                                                                  long delay) {

        final CrawlDelayManagerOutput[] output = new CrawlDelayManagerOutput[1];
        output[0] = new CrawlDelayManagerOutput(false, null);
        crawlDelayInfoMap.compute(hostCrawlDelayInfoKey, (key, value) -> {
            if (value == null) {
                value = new HostCrawlDelayInfo(hostCrawlDelayInfoKey, newDate);
                output[0].setSuccess(true);
                logger.debug("last accessed time" + value.getLastAccessedTime().toString());

            } else {
                if (newDate.getTime() > value.getLastAccessedTime().getTime() + delay) {
                    value.setLastAccessedTime(newDate);
                    output[0].setSuccess(true);
                    logger.debug("last accessed time" + value.getLastAccessedTime().toString());
                }
            }
            output[0].setHostCrawlDelayInfo(value);
            return value;
        });
        return output[0];
    }
}
