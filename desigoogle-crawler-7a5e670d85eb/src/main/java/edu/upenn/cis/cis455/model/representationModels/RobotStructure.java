package edu.upenn.cis.cis455.model.representationModels;

import edu.upenn.cis.cis455.utils.RobotsFileParser;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RobotStructure implements Serializable {

    private Map<String, UserAgentLevelRecord> userAgentLevelRecordList;
    private List<String> hostList;
    private List<String> siteMap;


    public RobotStructure(String data) {
        hostList = new ArrayList<>();
        siteMap = new ArrayList<>();
        RobotsFileParser.enrichRobotsStructure(this, data);
    }

    public Map<String, UserAgentLevelRecord> getUserAgentLevelRecordMap() {
        return userAgentLevelRecordList;
    }

    public void setUserAgentLevelRecordMap(Map<String, UserAgentLevelRecord> userAgentLevelRecordList) {
        this.userAgentLevelRecordList = userAgentLevelRecordList;
    }


    public List<String> getHostList() {
        return hostList;
    }

    public void setHostList(List<String> hostList) {
        this.hostList = hostList;
    }

    public List<String> getSiteMap() {
        return siteMap;
    }

    public void setSiteMap(List<String> siteMap) {
        this.siteMap = siteMap;
    }
}
