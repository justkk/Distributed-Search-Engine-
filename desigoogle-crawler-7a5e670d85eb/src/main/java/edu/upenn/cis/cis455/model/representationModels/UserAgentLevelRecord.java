package edu.upenn.cis.cis455.model.representationModels;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class UserAgentLevelRecord implements Serializable {

    private String userAgentAccess;
    private List<String> allowPatterns;
    private List<String> disallowPatterns;
    private int crawlDelay = 1;


    public UserAgentLevelRecord(String userAgentAccess) {
        if ("".equals(userAgentAccess)) {
            userAgentAccess = "*";
        }
        this.userAgentAccess = userAgentAccess;
        this.allowPatterns = new ArrayList<>();
        this.disallowPatterns = new ArrayList<>();
    }

    public UserAgentLevelRecord(String userAgentAccess, List<String> allowPatterns, List<String> disallowPatterns) {
        if ("".equals(userAgentAccess)) {
            userAgentAccess = "*";
        }
        this.userAgentAccess = userAgentAccess;
        this.allowPatterns = allowPatterns;
        this.disallowPatterns = disallowPatterns;

    }

    public static void main(String[] args) {

    }

    public String getUserAgentAccess() {
        return userAgentAccess;
    }

    public void setUserAgentAccess(String userAgentAccess) {
        this.userAgentAccess = userAgentAccess;
    }

    public List<String> getAllowPatterns() {
        return allowPatterns;
    }

    public void setAllowPatterns(List<String> allowPatterns) {
        this.allowPatterns = allowPatterns;
    }

    public List<String> getDisallowPatterns() {
        return disallowPatterns;
    }

    public void setDisallowPatterns(List<String> disallowPatterns) {
        this.disallowPatterns = disallowPatterns;
    }

    public int getCrawlDelay() {
        return crawlDelay;
    }

    public void setCrawlDelay(int crawlDelay) {
        this.crawlDelay = crawlDelay;
    }
}
