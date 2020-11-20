package edu.upenn.cis.cis455.crawler.validator;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.service.HostInfoExtractorService;
import edu.upenn.cis.cis455.crawler.service.URLDataExtractorService;
import edu.upenn.cis.cis455.crawler.service.URLInfoExtractorService;
import edu.upenn.cis.cis455.crawler.validator.processImpl.*;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;
import edu.upenn.cis.cis455.storage.managers.HostCrawlDelayManager;
import edu.upenn.cis.cis455.storage.managers.HostRobotInfoManager;
import edu.upenn.cis.cis455.storage.managers.URLDataManager;
import edu.upenn.cis.cis455.utils.RobotPathMatcher;

import java.util.ArrayList;
import java.util.List;

public class ProcessManager {


    private List<IProcess> processList;

    public ProcessManager(List<IProcess> processList) {
        this.processList = processList;
    }


    public void processRequest(ProcessContext processContext) {

        for(IProcess process: processList) {
            if(processContext.isValid()) {
                process.processRequest(processContext);
            }
            if(processContext.skip) {
                break;
            }
        }
    }

    public static void main(String[] args) {
//        DataBaseConnectorConfig dataBaseConnectorConfig = new DataBaseConnectorConfig("/Users/nikhilt/Desktop/playground");
//        HostRobotInfoManager hostRobotInfoManager = new HostRobotInfoManager(dataBaseConnectorConfig);
//        HostCrawlDelayManager hostCrawlDelayManager = new HostCrawlDelayManager();
//        URLDataManager urlDataManager = new URLDataManager(dataBaseConnectorConfig);
//        URLDataExtractorService urlDataExtractorService = new URLDataExtractorService();
//        URLInfoExtractorService urlInfoExtractorService = new URLInfoExtractorService(urlDataExtractorService,
//                urlDataManager);
//
//        HostInfoExtractorService hostInfoExtractorService = new HostInfoExtractorService(hostRobotInfoManager,
//                urlDataExtractorService);
//
//        RobotPathMatcher robotPathMatcher = new RobotPathMatcher();
//        List<IProcess> processList = new ArrayList<>();
//        processList.add(new RobotPermissionChecker(hostInfoExtractorService, robotPathMatcher));
//        processList.add(new RobotDelayChecker(hostCrawlDelayManager));
//        processList.add(new DBDataEnricher(urlInfoExtractorService));
//        processList.add(new HeadDataEnricher(urlInfoExtractorService));
//        processList.add(new GetDataEnricher(urlInfoExtractorService));
//
//        URLInfo urlInfo = new URLInfo("https://dbappserv.cis.upenn.edu/crawltest.html");
//        ProcessContext context = new ProcessContext(urlInfo);
//        ProcessContext context2 = new ProcessContext(urlInfo);
//
//        ProcessManager processManager = new ProcessManager(processList);
//        processManager.processRequest(context);
//        processManager.processRequest(context2);
//        System.out.println(context2);

    }
}
