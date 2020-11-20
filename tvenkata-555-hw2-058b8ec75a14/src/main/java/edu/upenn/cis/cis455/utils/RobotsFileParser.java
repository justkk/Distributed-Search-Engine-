package edu.upenn.cis.cis455.utils;

import edu.upenn.cis.cis455.model.representationModels.RobotStructure;
import edu.upenn.cis.cis455.model.representationModels.UserAgentLevelRecord;

import java.util.*;

public class RobotsFileParser {

    private static final String USER_AGENT = "^User-agent:.*";
    private static final String DISALLOW = "^Disallow:.*";
    private static final String ALLOW = "^Allow:.*";
    private static final String CRAWL_DELAY = "^Crawl-delay:.*";
    private static final String SITE_MAP = "^Sitemap:.*";
    private static final String HOST = "^HOST:.*";

    public static void enrichRobotsStructure(RobotStructure robotStructure, String data) {

        List<String> lines = getStringList(data);
        enrichRecords(robotStructure, lines);
    }

    private static List<String> getStringList(String data) {

        String tempStorage = data.replaceAll("\r\n", "\n");
        String[] lists = tempStorage.split("\n");
        return Arrays.asList(lists);
    }

    private static void enrichRecords(RobotStructure robotStructure, List<String> lines) {


        List<String> allowLines = null;
        List<String> disallowLines = null;
        List<String> agentList = new ArrayList<>();

        Map<String, UserAgentLevelRecord> userAgentLevelRecordList = new HashMap<>();
        List<String> siteMap = new ArrayList<>();
        List<String> hostList = new ArrayList<>();


        for (int i = 0; i < lines.size(); i++) {

            if (lines.get(i).startsWith("#")) {
                continue;
            }

            if (lines.get(i).matches(USER_AGENT)) {

                String agentName = getParam(lines.get(i));

                if(i>0 && lines.get(i-1).matches(USER_AGENT)) {
                    agentList.add(agentName);
                } else {

                    clearCurrentDirective(allowLines, disallowLines, agentList, userAgentLevelRecordList);
                    agentList = new ArrayList<>();
                    allowLines = new ArrayList<>();
                    disallowLines = new ArrayList<>();
                    agentList.add(agentName);
                }

            } else if (lines.get(i).matches(ALLOW) && allowLines != null) {
                allowLines.add(getParam(lines.get(i)));

            } else if (lines.get(i).matches(DISALLOW) && disallowLines != null) {
                disallowLines.add(getParam(lines.get(i)));
            } else if (lines.get(i).matches(CRAWL_DELAY)) {
                for(String agent: agentList) {
                    if (!userAgentLevelRecordList.containsKey(agent)) {
                        userAgentLevelRecordList.put(agent, new UserAgentLevelRecord(agent));
                    }
                    UserAgentLevelRecord userAgentLevelRecord = userAgentLevelRecordList.get(agent);
                    userAgentLevelRecord.setCrawlDelay(Integer.valueOf(getParam(lines.get(i))));
                }
            } else if (lines.get(i).matches(SITE_MAP) || lines.get(i).matches(HOST)) {

                if (lines.get(i).matches(SITE_MAP)) {
                    siteMap.add(getParam(lines.get(i)));
                } else if (lines.get(i).matches(HOST)) {
                    hostList.add(getParam(lines.get(i)));
                }

                clearCurrentDirective(allowLines, disallowLines, agentList, userAgentLevelRecordList);
                agentList = new ArrayList<>();
                allowLines = new ArrayList<>();
                disallowLines = new ArrayList<>();

            }

        }

        clearCurrentDirective(allowLines, disallowLines, agentList, userAgentLevelRecordList);

        robotStructure.setSiteMap(siteMap);
        robotStructure.setHostList(hostList);
        robotStructure.setUserAgentLevelRecordMap(userAgentLevelRecordList);
    }

    private static void clearCurrentDirective(List<String> allowLines, List<String> disallowLines, List<String> agentList, Map<String, UserAgentLevelRecord> userAgentLevelRecordList) {
        for (String agent : agentList) {

            if (!userAgentLevelRecordList.containsKey(agent)) {
                userAgentLevelRecordList.put(agent, new UserAgentLevelRecord(agent));
            }

            UserAgentLevelRecord userAgentLevelRecord = userAgentLevelRecordList.get(agent);
            userAgentLevelRecord.getAllowPatterns().addAll(allowLines);
            userAgentLevelRecord.getDisallowPatterns().addAll(disallowLines);
        }
    }

    private static String getParam(String record) {
        String[] list = record.split(":", 2);
        if (list.length == 1) {
            return "";
        }
        return list[1].trim();
    }

    public static void main(String[] args) {
        String data = "User-agent: *\n" +
                "Disallow: /\n" +
                "\n" +
                "\n" +
                "User-agent: googlebot\n" +
                "User-agent: Googlebot-Video\n" +
                "User-agent: bingbot\n" +
                "User-agent: Baiduspider\n" +
                "User-agent: Baiduspider-mobile\n" +
                "User-agent: Baiduspider-video\n" +
                "User-agent: Baiduspider-image\n" +
                "User-agent: NaverBot\n" +
                "User-agent: Yeti\n" +
                "User-agent: Yandex\n" +
                "User-agent: YandexBot\n" +
                "User-agent: YandexMobileBot\n" +
                "User-agent: YandexVideo\n" +
                "User-agent: YandexWebmaster\n" +
                "User-agent: YandexSitelinks\n" +
                "Allow: /\n" +
                "Disallow: /accountstatus\n" +
                "Disallow: /aui/inbound\n" +
                "Disallow: /authenticate\n" +
                "Disallow: /autologin\n" +
                "Disallow: /companies\n" +
                "Disallow: /dvdterms\n" +
                "Disallow: /editpayment\n" +
                "Disallow: /emailunsubscribe\n" +
                "Disallow: /error\n" +
                "Disallow: /eula\n" +
                "Disallow: /geooverride\n" +
                "Disallow: /help\n" +
                "Disallow: /imagelibrary\n" +
                "Disallow: /learnmorelayer\n" +
                "Disallow: /learnmorelayertv\n" +
                "Disallow: /login\n" +
                "Disallow: /loginhelp\n" +
                "Disallow: /loginhelp/lookup\n" +
                "Disallow: /loginhelpsucess\n" +
                "Disallow: /logout\n" +
                "Disallow: /mcd\n" +
                "Disallow: /modernizr\n" +
                "Disallow: /notamember\n" +
                "Disallow: /notfound\n" +
                "Disallow: /notices\n" +
                "Disallow: /nrdapp\n" +
                "Disallow: /optout\n" +
                "Disallow: /overviewblockseeother\n" +
                "Disallow: /popup/codewhatisthis\n" +
                "Disallow: /popupdetails\n" +
                "Disallow: /popupprivacypolicy\n" +
                "Disallow: /privacypolicychanges\n" +
                "Disallow: /registration\n" +
                "Disallow: /rememberme\n" +
                "Disallow: /signout\n" +
                "Disallow: /signurl\n" +
                "Disallow: /subscriptioncancel\n" +
                "Disallow: /tastesurvey\n" +
                "Disallow: /termsofusechanges\n" +
                "Disallow: /upcomingevents\n" +
                "Disallow: /whysecure\n" +
                "\n" +
                "Disallow: /arabic\n" +
                "Disallow: /chinese\n" +
                "Disallow: /korean\n" +
                "Disallow: /Arabic\n" +
                "Disallow: /Chinese\n" +
                "Disallow: /Korean\n" +
                "\n" +
                "Disallow: /airtel\n" +
                "Disallow: /anan\n" +
                "Disallow: /bouyguestelecom\n" +
                "Disallow: /britishairways\n" +
                "Disallow: /brutus\n" +
                "Disallow: /comhem\n" +
                "Disallow: /courts\n" +
                "Disallow: /csl\n" +
                "Disallow: /elisa\n" +
                "Disallow: /entertain\n" +
                "Disallow: /FireTV\n" +
                "Disallow: /firetv\n" +
                "Disallow: /freemonth\n" +
                "Disallow: /kpn\n" +
                "Disallow: /lg\n" +
                "Disallow: /maxis\n" +
                "Disallow: /Maxis\n" +
                "Disallow: /meo\n" +
                "Disallow: /Meo\n" +
                "Disallow: /orangefrance\n" +
                "Disallow: /Panasonic\n" +
                "Disallow: /panasonic\n" +
                "Disallow: /playstation\n" +
                "Disallow: /proximus\n" +
                "Disallow: /qantas\n" +
                "Disallow: /samsung\n" +
                "Disallow: /Sony\n" +
                "Disallow: /sony\n" +
                "Disallow: /talktalk\n" +
                "Disallow: /tdc\n" +
                "Disallow: /telenor\n" +
                "Disallow: /telfort\n" +
                "Disallow: /tim\n" +
                "Disallow: /virginaustralia\n" +
                "Disallow: /vodafone\n" +
                "Disallow: /vodafonedemobilelaunch\n" +
                "Disallow: /xboxone\n" +
                "Disallow: /xfinity\n" +
                "Disallow: /xs4all\n" +
                "Disallow: /ziggo\n" +
                "\n" +
                "Disallow: /search\n" +
                "Disallow: /search/*\n" +
                "Disallow: /browse\n" +
                "Disallow: /browse/*\n" +
                "Allow: /browse/genre/*\n" +
                "Disallow: /yourAccount\n" +
                "Disallow: /youraccount\n" +
                "Disallow: /accountaccess\n" +
                "Disallow: /BillingActivity\n" +
                "Disallow: /CancelPlan\n" +
                "Disallow: /ChangePlan\n" +
                "Disallow: /activate\n" +
                "Disallow: /viewingactivity\n" +
                "Disallow: /ManageDevices\n" +
                "Disallow: /EditProfiles\n" +
                "Disallow: /Activate\n" +
                "Disallow: /ManageProfiles\n" +
                "Disallow: /ProfilesGate\n" +
                "Disallow: /changeplan\n" +
                "Disallow: /LanguagePreferences\n" +
                "Disallow: /MoviesYouveSeen\n" +
                "Disallow: /phonenumber\n" +
                "Disallow: /accountaccess\n" +
                "Disallow: /MyListOrder\n" +
                "Disallow: /HdToggle\n" +
                "Disallow: /deviceManagement\n" +
                "Disallow: /profiles\n" +
                "Disallow: /profiles/*\n" +
                "Disallow: /DoNotTest\n" +
                "Disallow: /payment\n" +
                "Disallow: /PopupDetails\n" +
                "Disallow: /clearcookies\n" +
                "\n" +
                "User-agent: AdsBot-Google\n" +
                "User-agent: Twitterbot\n" +
                "User-agent: Adidxbot\n" +
                "Allow: /\n" +
                "\n" +
                "\n" +
                "User-agent: Yahoo Pipes 1.0\n" +
                "User-agent: Facebot\n" +
                "User-agent: externalfacebookhit\n" +
                "Disallow: /\n" +
                "\n" +
                "\n" +
                "Sitemap: https://www.netflix.com/api/sitemap-v2/index\n";

        RobotStructure robotStructure = new RobotStructure(data);
        //System.out.println(robotStructure.getUserAgentLevelRecordMap());
    }


}
