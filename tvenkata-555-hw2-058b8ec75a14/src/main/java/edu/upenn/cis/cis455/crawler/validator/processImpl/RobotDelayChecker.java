package edu.upenn.cis.cis455.crawler.validator.processImpl;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import edu.upenn.cis.cis455.crawler.queue.WaitingQueueInstance;
import edu.upenn.cis.cis455.crawler.validator.IProcess;
import edu.upenn.cis.cis455.crawler.validator.ProcessContext;
import edu.upenn.cis.cis455.model.hostCrawlDelayInfo.HostCrawlDelayInfoKey;
import edu.upenn.cis.cis455.model.representationModels.RobotStructure;
import edu.upenn.cis.cis455.storage.managers.HostCrawlDelayManager;
import edu.upenn.cis.cis455.utils.RobotPathMatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

public class RobotDelayChecker implements IProcess {

    private HostCrawlDelayManager hostCrawlDelayManager;

    private Logger logger = LogManager.getLogger(RobotDelayChecker.class);

    public RobotDelayChecker(HostCrawlDelayManager hostCrawlDelayManager) {
        this.hostCrawlDelayManager = hostCrawlDelayManager;
    }


    @Override
    public boolean validateRequest(ProcessContext processContext) {
        if(processContext.isValid() && processContext.getMatchingOutput()!=null && processContext.getMatchingOutput().isMatch()) {
            return true;
        }
        return false;
    }

    @Override
    public void processRequest(ProcessContext processContext) {
        if(!validateRequest(processContext)) {
            processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
            processContext.setMessage("RobotDelayChecker validation failed");
            return;
        }

        Date currentDate = new Date();
        long delay = processContext.getRobotStructure().getUserAgentLevelRecordMap()
                .get(processContext.getMatchingOutput().getUserAgent()).getCrawlDelay()*1000;

        logger.debug("Getting robot delay information");

        String protocol = processContext.getUrlInfo().isSecure()? "https": "http";

        HostCrawlDelayInfoKey hostCrawlDelayInfoKey = new HostCrawlDelayInfoKey(processContext.getUrlInfo()
                .getHostName(), processContext.getUrlInfo().getPortNo(), protocol);

        HostCrawlDelayManager.CrawlDelayManagerOutput output = hostCrawlDelayManager.getHostCrawlDelayInfoSOU(
                hostCrawlDelayInfoKey, currentDate, delay);
        processContext.setCrawlDelayManagerOutput(output);
        if(output.isSuccess()) {
            logger.debug("delay not needed");
            processContext.setStatus(ProcessContext.ProcessContextStatus.SUCCESS);
        } else {
            logger.debug("should delay request");
            processContext.setStatus(ProcessContext.ProcessContextStatus.WAIT);
            processContext.setMessage("RobotDelayChecker should delay request" + processContext.getUrlInfo().getUrl().toString());
            processContext.setWaitingQueueInstance(new WaitingQueueInstance(new ReadyQueueInstance(processContext.getUrlInfo()),
                    output.getHostCrawlDelayInfo().getLastAccessedTime(), delay));
        }
    }

    public boolean deferCrawl(URLInfo urlInfo, RobotStructure robotStructure, RobotPathMatcher.MatchingOutput matchingOutput) {
        if(!urlInfo.isValid()) {
            return false;
        }
        Date currentDate = new Date();
        long delay = robotStructure.getUserAgentLevelRecordMap()
                .get(matchingOutput.getUserAgent()).getCrawlDelay()*1000;

        String protocol = urlInfo.isSecure()? "https": "http";

        HostCrawlDelayInfoKey hostCrawlDelayInfoKey = new HostCrawlDelayInfoKey(urlInfo
                .getHostName(), urlInfo.getPortNo(), protocol);
        HostCrawlDelayManager.CrawlDelayManagerOutput output = hostCrawlDelayManager.getHostCrawlDelayInfoSOU(
                hostCrawlDelayInfoKey, currentDate, delay);
        return !output.isSuccess();

    }
}
