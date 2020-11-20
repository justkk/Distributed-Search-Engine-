package edu.upenn.cis.cis455;

import java.util.*;

public class ConstantsHW2 {

//    private final String URL_META_DATABASE = "urlMeta";
//
//    private final String URL_DOC_DATABASE = "docStorage";
//
//    private final String USER_RECORD_DATABASE = "recordDataBase";
//
//    private final String CONTENT_SEEN_DATABASE = "contentSeen";
//
//    private final String HOST_ROBOT_DATABASE = "hostRobotDataBase";
//
//    private final String USER_CHANNEL_DATABASE = "userChannelDataBase";
//
//    private final String INDEX_DATABASE = "index";


    private final String URL_META_DATABASE = "temp";

    private final String URL_DOC_DATABASE = "temp";

    private final String USER_RECORD_DATABASE = "temp";

    private final String CONTENT_SEEN_DATABASE = "temp";

    private final String HOST_ROBOT_DATABASE = "temp";

    private final String USER_CHANNEL_DATABASE = "temp";

    private final String INDEX_DATABASE = "temp";

    private final String DOC_ONLY_DATABASE = "temp";

    private final String URL_ONLY_DATABASE = "temp";

    private final String SUBSCRIPTION_DATABASE = "temp";


    private final List<String> ROBOT_PATH = Arrays.asList("/robots.txt");

    private final int httpConnectionTimeOut = 5000;// 2 sec

    private final int httpsConnectionTimeOut = 5000;// 2 sec

    private final int hostRobotRefreshTime = 200000; // 2sec

    private final Map<String, String> hostRobotHeaders = new HashMap<String, String>() {{
        put("User-Agent", "cis455crawler");
        put("####RedirectFlag####", "");
    }};

    private final Map<String, String> urlInfoGetHeaders = new HashMap<String, String>() {{
        put("User-Agent", "cis455crawler");
    }};

    private final Map<String, String> urlInfoHeadHeaders = new HashMap<String, String>() {{
        put("User-Agent", "cis455crawler");
    }};

    private final List<String> allowedUserAgents = Arrays.asList("cis455crawler", "*");

    private static ConstantsHW2 constant;

    public static ConstantsHW2 getInstance() {
        if(constant == null) {
            constant = new ConstantsHW2();
        }
        return constant;
    }

    public String getDOC_ONLY_DATABASE() {
        return DOC_ONLY_DATABASE;
    }

    public String getURL_ONLY_DATABASE() {
        return URL_ONLY_DATABASE;
    }

    public String getUSER_CHANNEL_DATABASE() {
        return USER_CHANNEL_DATABASE;
    }

    public String getURL_META_DATABASE() {
        return URL_META_DATABASE;
    }

    public String getURL_DOC_DATABASE() {
        return URL_DOC_DATABASE;
    }

    public String getUSER_RECORD_DATABASE() {
        return USER_RECORD_DATABASE;
    }

    public List<String> getROBOT_PATH() {
        return ROBOT_PATH;
    }

    public int getHttpConnectionTimeOut() {
        return httpConnectionTimeOut;
    }

    public int getHttpsConnectionTimeOut() {
        return httpsConnectionTimeOut;
    }

    public String getHOST_ROBOT_DATABASE() {
        return HOST_ROBOT_DATABASE;
    }

    public String getSUBSCRIPTION_DATABASE() {
        return SUBSCRIPTION_DATABASE;
    }

    public String getINDEX_DATABASE() {
        return INDEX_DATABASE;
    }

    public int getHostRobotRefreshTime() {
        return hostRobotRefreshTime;
    }

    public Map<String, String> getHostRobotHeaders() {
        return hostRobotHeaders;
    }

    public Map<String, String> getUrlInfoGetHeaders() {
        return urlInfoGetHeaders;
    }

    public Map<String, String> getUrlInfoHeadHeaders() {
        return urlInfoHeadHeaders;
    }

    public List<String> getAllowedUserAgents() {
        return allowedUserAgents;
    }

    public String getCONTENT_SEEN_DATABASE() {
        return CONTENT_SEEN_DATABASE;
    }


}
