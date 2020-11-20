package edu.upenn.cis.cis455.crawler.validator.processImpl;

import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.service.HostInfoExtractorService;
import edu.upenn.cis.cis455.crawler.validator.IProcess;
import edu.upenn.cis.cis455.crawler.validator.ProcessContext;
import edu.upenn.cis.cis455.model.hostRobotInfo.HostRobotInfo;
import edu.upenn.cis.cis455.model.representationModels.RobotStructure;
import edu.upenn.cis.cis455.utils.RobotPathMatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RobotPermissionChecker implements IProcess {

    private HostInfoExtractorService hostInfoExtractorService;
    private RobotPathMatcher robotPathMatcher;

    private Logger logger = LogManager.getLogger(RobotPermissionChecker.class);

    public RobotPermissionChecker(HostInfoExtractorService hostInfoExtractorService, RobotPathMatcher robotPathMatcher) {
        this.hostInfoExtractorService = hostInfoExtractorService;
        this.robotPathMatcher = robotPathMatcher;
    }

    @Override
    public boolean validateRequest(ProcessContext processContext) {
        return processContext.isValid() && processContext.getUrlInfo() != null;
    }

    @Override
    public void processRequest(ProcessContext processContext) {

        if (!validateRequest(processContext)) {
            processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
            processContext.setMessage("RobotPermissionChecker validation failed");
            return;
        }

        String protocol = processContext.getUrlInfo().isSecure() ? "https" : "http";

        logger.debug("Extracting robot information");

        HostRobotInfo hostRobotInfo = hostInfoExtractorService.getHostInfo(protocol, processContext.getUrlInfo().getHostName(),
                processContext.getUrlInfo().getPortNo(), processContext.getTxn());

        if (hostRobotInfo == null) {
            processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
            logger.debug("RobotPermissionChecker: HostRobotInfo is null");
            processContext.setMessage("HostRobotInfo is null");
            return;
        }

        RobotStructure robotStructure = new RobotStructure(hostRobotInfo.getRobotStructure());

        if (robotStructure == null) {
            processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
            logger.debug("RobotStructure is null");
            processContext.setMessage("RobotPermissionChecker: RobotStructure is null");
            return;
        }
        logger.debug("Invoking path matcher");
        RobotPathMatcher.MatchingOutput matchingOutput = robotPathMatcher.pathMatch(processContext.getUrlInfo(), robotStructure,
                ConstantsHW2.getInstance().getAllowedUserAgents());

        if (!matchingOutput.isMatch()) {
            processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
            logger.debug("matching failure, permission denied");
            processContext.setMessage("RobotPermissionChecker: matching failure, permission denied");
            processContext.setMatchingOutput(matchingOutput);
            return;
        }

        logger.debug("Path matching successful");

        processContext.setRobotStructure(robotStructure);
        processContext.setMatchingOutput(matchingOutput);

    }

    public RobotStructure getRobotStructure(URLInfo urlInfo) {
        if (!urlInfo.isValid()) return null;
        String protocol = urlInfo.isSecure() ? "https" : "http";

        HostRobotInfo hostRobotInfo = hostInfoExtractorService.getHostInfo(protocol, urlInfo.getHostName(),
                urlInfo.getPortNo(), null);

        if (hostRobotInfo == null) {
            return null;
        }
        RobotStructure robotStructure = new RobotStructure(hostRobotInfo.getRobotStructure());
        return robotStructure;
    }

    public RobotPathMatcher.MatchingOutput getMatchingOutput(URLInfo urlInfo, RobotStructure robotStructure) {
        RobotPathMatcher.MatchingOutput matchingOutput = robotPathMatcher.pathMatch(urlInfo, robotStructure,
                ConstantsHW2.getInstance().getAllowedUserAgents());
        return matchingOutput;
    }
}
