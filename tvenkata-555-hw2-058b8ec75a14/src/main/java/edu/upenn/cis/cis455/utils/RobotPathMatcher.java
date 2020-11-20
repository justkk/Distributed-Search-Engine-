package edu.upenn.cis.cis455.utils;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.model.representationModels.RobotStructure;
import edu.upenn.cis.cis455.model.representationModels.UserAgentLevelRecord;

import java.util.List;
import java.util.Map;


public class RobotPathMatcher {


    /**
     * @param urlInfo
     * @param robotStructure
     * @param allowedUserAgents : Decreasing order of priority
     * @return
     */
    public MatchingOutput pathMatch(URLInfo urlInfo, RobotStructure robotStructure, List<String> allowedUserAgents) {
        for (String userAgent : allowedUserAgents) {
            if (!robotStructure.getUserAgentLevelRecordMap().containsKey(userAgent)) {
                continue;
            }
            boolean allowed = pathMatch(urlInfo, robotStructure, userAgent);
            if (allowed) {
                return new MatchingOutput(true, userAgent);
            } else if (robotStructure.getUserAgentLevelRecordMap().containsKey(userAgent)) {
                return new MatchingOutput(false, null);
            }
        }
        return new MatchingOutput(false, null);
    }


    private boolean pathMatch(URLInfo urlInfo, RobotStructure robotStructure, String allowedUserAgents) {
        Map<String, UserAgentLevelRecord> recordMap = robotStructure.getUserAgentLevelRecordMap();

        UserAgentLevelRecord userAgentLevelRecord = recordMap.get(allowedUserAgents);
        for (String pattern : userAgentLevelRecord.getAllowPatterns()) {
            if (StringMatcher.wildCardMatcher(urlInfo.getFilePath(), pattern)) {
                return true;
            }
        }

        for (String pattern : userAgentLevelRecord.getDisallowPatterns()) {
            if (StringMatcher.wildCardMatcher(urlInfo.getFilePath(), pattern)) {
                return false;
            }
        }
        return true;
    }

    public class MatchingOutput {

        private boolean match;
        private String userAgent;

        public MatchingOutput(boolean match, String userAgent) {
            this.match = match;
            this.userAgent = userAgent;
        }

        public boolean isMatch() {
            return match;
        }

        public String getUserAgent() {
            return userAgent;
        }
    }
}
