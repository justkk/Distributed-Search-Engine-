package edu.upenn.cis.cis455.crawler.service;

import com.sleepycat.je.Transaction;
import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.model.hostRobotInfo.HostRobotInfo;
import edu.upenn.cis.cis455.model.hostRobotInfo.HostRobotInfoKey;
import edu.upenn.cis.cis455.model.representationModels.URLResponse;
import edu.upenn.cis.cis455.storage.managers.HostRobotInfoManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class HostInfoExtractorService {

    private final String DEFAULT_PROTOCOL = "http";

    private HostRobotInfoManager hostRobotInfoManager;
    private URLDataExtractorService urlDataExtractorService;

    private Logger logger = LogManager.getLogger(CrawlerService.class);


    public HostInfoExtractorService(HostRobotInfoManager hostRobotInfoManager,
                                    URLDataExtractorService urlDataExtractorService) {
        this.hostRobotInfoManager = hostRobotInfoManager;
        this.urlDataExtractorService = urlDataExtractorService;
    }

    public HostRobotInfo getHostInfo(String protocol, String host, int port, Transaction txn) {

        logger.debug("Fetching host robot info");
        HostRobotInfo fromDatabase = getHostInfoFromDB(protocol, host, port, txn);
        logger.debug("Robot info from DB " + fromDatabase);
        Date currentDate = new Date();
        if (fromDatabase == null || currentDate.getTime() - fromDatabase.getLastUpdatedDate().getTime()
                > ConstantsHW2.getInstance().getHostRobotRefreshTime()) {
            logger.debug("Fetching robot information from live");
            HostRobotInfo hostRobotInfo = getHostInfoLive(protocol, host, port);
            if (hostRobotInfo == null) {
                if (fromDatabase != null) {
                    logger.debug("deleting information as current request failed");
                    hostRobotInfoManager.deleteHostRobotInfo(hostRobotInfo);
                }
                return null;
            }
            if (fromDatabase == null) {
                logger.debug("inserting entry in database");
                return hostRobotInfoManager.insertHostRobotInfo(hostRobotInfo, txn);
            } else {
                logger.debug("updating entry in database");
                fromDatabase.setRobotStructure(hostRobotInfo.getRobotStructure());
                fromDatabase.setRobotFilePath(hostRobotInfo.getRobotFilePath());
                return hostRobotInfoManager.insertHostRobotInfo(fromDatabase, txn);
            }

        }
        return fromDatabase;
    }

    private HostRobotInfo getHostInfoFromDB(String protocol, String host, int port, Transaction txn) {

        HostRobotInfoKey hostRobotInfoKey = new HostRobotInfoKey(protocol, host, port);
        return hostRobotInfoManager.getHostRobotInfo(hostRobotInfoKey, txn);
    }

    private HostRobotInfo getHostInfoLive(String protocol, String host, int port) {

        for (String path : ConstantsHW2.getInstance().getROBOT_PATH()) {
            try {
                URL url = new URL(protocol, host, port, path);
                URLResponse urlResponse = urlDataExtractorService.getUrlData(url, "GET",
                        ConstantsHW2.getInstance().getHostRobotHeaders());
                if (urlResponse.getData() == null) {
                    continue;
                }
                HostRobotInfo hostRobotInfo = new HostRobotInfo(protocol, host, port);
                hostRobotInfo.setRobotFilePath(path);
                //RobotStructure robotStructure = new RobotStructure(urlResponse.getData());
                hostRobotInfo.setRobotStructure(urlResponse.getData());
                return hostRobotInfo;
            } catch (MalformedURLException e) {
                //System.out.println("Url Formation issue. Trying next one");
            } catch (Exception e) {
                //System.out.println("unknown issue. Trying next one");
            }

        }
        return null;
    }

    public static void main(String[] args) {
//        DataBaseConnectorConfig dataBaseConnectorConfig = new DataBaseConnectorConfig("/Users/nikhilt/Desktop/playground");
//        HostRobotInfoManager hostRobotInfoManager = new HostRobotInfoManager(dataBaseConnectorConfig);
//        URLDataExtractorService urlDataExtractorService = new URLDataExtractorService();
//        HostInfoExtractorService hostInfoExtractorService = new HostInfoExtractorService(hostRobotInfoManager, urlDataExtractorService);
//        System.out.println(hostInfoExtractorService.getHostInfoLive("http", "www.facebook.com", 80));
//        System.out.println(hostInfoExtractorService.getHostInfo("http", "www.facebook.com", 80));
//        System.out.println(hostInfoExtractorService.getHostInfoFromDB("http", "www.facebook.com", 80));


    }


}
