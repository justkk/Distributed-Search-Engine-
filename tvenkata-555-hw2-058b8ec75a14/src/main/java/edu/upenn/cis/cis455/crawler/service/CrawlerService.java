package edu.upenn.cis.cis455.crawler.service;


import com.sleepycat.je.Transaction;
import edu.upenn.cis.cis455.crawler.CrawlServiceResponse;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.DocType;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import edu.upenn.cis.cis455.crawler.validator.IProcess;
import edu.upenn.cis.cis455.crawler.validator.ProcessContext;
import edu.upenn.cis.cis455.crawler.validator.ProcessManager;
import edu.upenn.cis.cis455.crawler.validator.processImpl.*;
import edu.upenn.cis.cis455.model.representationModels.RobotStructure;
import edu.upenn.cis.cis455.model.representationModels.URLMetaInformation;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfoKey;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import edu.upenn.cis.cis455.storage.managers.HostCrawlDelayManager;
import edu.upenn.cis.cis455.storage.managers.HostRobotInfoManager;
import edu.upenn.cis.cis455.storage.managers.URLDataManager;
import edu.upenn.cis.cis455.utils.DocUrlParser;
import edu.upenn.cis.cis455.utils.RobotPathMatcher;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CrawlerService {


    private ProcessManager processManager;
    private Crawler crawler;
    private DocUrlParser documentParser;
    private StorageInterfaceImpl storageInterface;

    private RobotPermissionChecker robotPermissionChecker;
    private RobotDelayChecker robotDelayChecker;
    private DBDataEnricher dbDataEnricher;
    private HeadDataEnricher headDataEnricher;
    private GetDataEnricher getDataEnricher;
    private IndexDocumentProcessor indexDocumentProcessor;

    private Logger logger = LogManager.getLogger(CrawlerService.class);

    public CrawlerService(StorageInterfaceImpl storageInterface, Crawler crawler) {
        this.crawler = crawler;
        HostRobotInfoManager hostRobotInfoManager = storageInterface.getHostRobotInfoManager();
        HostCrawlDelayManager hostCrawlDelayManager = storageInterface.getHostCrawlDelayManager();
        URLDataManager urlDataManager = storageInterface.getUrlDataManager();
        this.storageInterface = storageInterface;

        URLDataExtractorService urlDataExtractorService = new URLDataExtractorService(crawler.getMaxDocumentSize());
        URLInfoExtractorService urlInfoExtractorService = new URLInfoExtractorService(urlDataExtractorService,
                urlDataManager);

        HostInfoExtractorService hostInfoExtractorService = new HostInfoExtractorService(hostRobotInfoManager,
                urlDataExtractorService);

        RobotPathMatcher robotPathMatcher = new RobotPathMatcher();
        List<IProcess> processList = new ArrayList<>();

        robotPermissionChecker = new RobotPermissionChecker(hostInfoExtractorService, robotPathMatcher);
        robotDelayChecker = new RobotDelayChecker(hostCrawlDelayManager);
        dbDataEnricher = new DBDataEnricher(urlInfoExtractorService);
        headDataEnricher = new HeadDataEnricher(urlInfoExtractorService);
        getDataEnricher = new GetDataEnricher(urlInfoExtractorService);
        indexDocumentProcessor = new IndexDocumentProcessor(crawler, urlDataManager);

        processList.add(robotPermissionChecker);
        processList.add(robotDelayChecker);
        processList.add(dbDataEnricher);
        processList.add(headDataEnricher);
        processList.add(getDataEnricher);
        processList.add(indexDocumentProcessor);
        processManager = new ProcessManager(processList);
        documentParser = new DocUrlParser();
    }

    public CrawlServiceResponse processRequest(ReadyQueueInstance readyQueueInstance) {

        if (!readyQueueInstance.isValid()) {
            logger.debug("Invalid readyQueue instance");
            return new CrawlServiceResponse(false, "invalid instance", null);
        }
        Transaction txn = this.storageInterface.getDataBaseConnectorConfig().getDatabaseEnvironment().beginTransaction(null,
                null);

        ProcessContext processContext = new ProcessContext(readyQueueInstance.getUrlInfo(), txn, crawler.getMaxDocumentSize());
        try {
            logger.debug("processing request");
            processManager.processRequest(processContext);
        } catch (Exception e) {
            logger.debug("process execution failed " + e.getMessage());
            processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
            processContext.setMessage(e.getMessage());
        }

        URLDataInfoKey urlDataInfoKey = new URLDataInfoKey(processContext.getUrlInfo().getUrl().getProtocol(),
                processContext.getUrlInfo().getHostName(), processContext.getUrlInfo().getPortNo(), processContext.getUrlInfo().getFilePath(),
                "GET");
        URLDataInfo urlDataInfo = storageInterface.getUrlDataManager().getURLDataInfo(urlDataInfoKey, txn);

        if (processContext.isFailure()) {
            logger.debug("process execution failed " + processContext.getMessage());
            logger.debug("aborting transaction");
            txn.abort();
            return new CrawlServiceResponse(false, processContext.getMessage(), processContext);
        } else {
            txn.commit();
        }

        //System.out.println(processContext.getMessage());

        if (processContext.isWait()) {
            logger.debug("Request has to wait");
            return new CrawlServiceResponse(new ArrayList<>(), Arrays.asList(processContext.getWaitingQueueInstance()),
                    processContext, true);
        }

        if(processContext.isRedirect()) {
            return new CrawlServiceResponse(
                    Arrays.asList(new ReadyQueueInstance(processContext.getRedirectUrlInfo())),
                    new ArrayList<>(), processContext, true);
        }
        if (!processContext.isValid()) {
            logger.debug("Request processing lead to unknown state");
            return new CrawlServiceResponse(false, processContext.getMessage(), processContext);
        }

        logger.debug("Processing content of the request");

        if(!shouldIProcessDocument(processContext)) {
            return new CrawlServiceResponse(new ArrayList<>(), new ArrayList<>(), processContext, true);
        }
        processContext.setUrlDataInfo(urlDataInfo);
        return new CrawlServiceResponse(new ArrayList<>(), new ArrayList<>(), processContext, false);

        //return processDocument(readyQueueInstance, processContext);
    }

    private boolean shouldIProcessDocument(ProcessContext processContext) {

        if(processContext.getMd5Hash() == null) {
            logger.debug("Bad Hash Value");
            return false;
        }

        synchronized (this.crawler) {
            if(crawler.getCrawlDataStructure().getVisitedHashDocumentSet().contains(processContext.getMd5Hash())){
                logger.debug(processContext.getUrlInfo().getUrl().toString() + " content already processed in this life cycle");
                logger.debug("Similar url " + crawler.getCrawlDataStructure()
                        .getVisitedHashDocumentUrlMap().get(processContext.getMd5Hash()));
                logger.debug(processContext.getGetInfo().getData());
                logger.debug("Hash " + processContext.getMd5Hash());
                logger.debug("Not processing the content");
                return false;
            }
            logger.debug("Content not processed already");
            crawler.getCrawlDataStructure().getVisitedHashDocumentSet().add(processContext.getMd5Hash());
            crawler.getCrawlDataStructure().getVisitedHashDocumentUrlMap().put(processContext.getMd5Hash(),
                    processContext.getUrlInfo().getUrl().toString());
            return true;
        }
    }

    public CrawlServiceResponse processDocument(ReadyQueueInstance readyQueueInstance, ProcessContext processContext) {

        DocType docType = DocType.getTypeFromContentType(processContext.getGetInfo().getContentType());

        logger.debug("Document Content type " + docType.toString());

        //System.out.println(docType);

        if (docType == DocType.HTML) {
            // extract urls.
            String protocol = readyQueueInstance.getUrlInfo().isSecure() ? "https" : "http";
            try {
                String baseUrl = new URL(protocol, readyQueueInstance.getUrlInfo().getHostName(),
                        readyQueueInstance.getUrlInfo().getPortNo(), readyQueueInstance.getUrlInfo().getFilePath()).toString();
                logger.debug("parsing the html content");
                List<URLInfo> urlInfoList = documentParser.enrichLinks(baseUrl, processContext.getGetInfo().getData());
                logger.debug("number of links in the document: " + urlInfoList.size());
                List<ReadyQueueInstance> readyQueueInstances = urlInfoList.stream().map(urlInfo
                        -> new ReadyQueueInstance(urlInfo)).collect(Collectors.toList());
                //System.out.println(readyQueueInstances);
                return new CrawlServiceResponse(readyQueueInstances, new ArrayList<>(), processContext, true);
            } catch (MalformedURLException e) {
                return new CrawlServiceResponse(false, e.getMessage(), processContext);
            }
            //return readyQueueInstance;
        } else if (docType == DocType.XML) {
            // process documents. Like subscriptions.
            logger.debug("XML document");
            logger.debug("No additional processing right now");
            return new CrawlServiceResponse(new ArrayList<>(), new ArrayList<>(), processContext, true);
        }
        return new CrawlServiceResponse(false, "UNKNOWN Error, Bad Document", processContext);
    }


    public boolean isOKtoCrawl(String site, int port, boolean isSecure) {

        URLInfo urlInfo = new URLInfo(site);
        if (!urlInfo.isValid()) {
            return false;
        }
        boolean canICrawl = !deferCrawl(site);
        if (canICrawl) {
            return isOKtoParse(urlInfo);
        } else {
            return false;
        }
    }

    public boolean isIndexable(String content) {
        return indexDocumentProcessor.isIndexable(content);
    }

    public boolean deferCrawl(String site) {
        URLInfo urlInfo = new URLInfo(site);
        if (!urlInfo.isValid()) return false;
        RobotStructure robotStructure = robotPermissionChecker.getRobotStructure(urlInfo);
        if (robotStructure == null) return false;
        RobotPathMatcher.MatchingOutput matchingOutput = robotPermissionChecker.getMatchingOutput(urlInfo, robotStructure);
        if (matchingOutput == null) return false;
        return robotDelayChecker.deferCrawl(urlInfo, robotStructure, matchingOutput);
    }

    public boolean isOKtoParse(URLInfo url) {
        if (!url.isValid()) return false;
        URLMetaInformation getDbMessage = dbDataEnricher.getDBInformation(url);
        Boolean headValidation = headDataEnricher.isOKtoParse(url, getDbMessage, crawler.getMaxDocumentSize());
        if (headValidation == null) {
            Boolean getValidation = getDataEnricher.isOkToParse(url, crawler.getMaxDocumentSize());
            return getValidation;
        } else {
            return headValidation;
        }
    }



    public void setProcessManager(ProcessManager processManager) {
        this.processManager = processManager;
    }

    public void setCrawler(Crawler crawler) {
        this.crawler = crawler;
    }

    public void setDocumentParser(DocUrlParser documentParser) {
        this.documentParser = documentParser;
    }

    public void setStorageInterface(StorageInterfaceImpl storageInterface) {
        this.storageInterface = storageInterface;
    }

    public void setRobotPermissionChecker(RobotPermissionChecker robotPermissionChecker) {
        this.robotPermissionChecker = robotPermissionChecker;
    }

    public void setRobotDelayChecker(RobotDelayChecker robotDelayChecker) {
        this.robotDelayChecker = robotDelayChecker;
    }

    public void setDbDataEnricher(DBDataEnricher dbDataEnricher) {
        this.dbDataEnricher = dbDataEnricher;
    }

    public void setHeadDataEnricher(HeadDataEnricher headDataEnricher) {
        this.headDataEnricher = headDataEnricher;
    }

    public void setGetDataEnricher(GetDataEnricher getDataEnricher) {
        this.getDataEnricher = getDataEnricher;
    }

    public void setIndexDocumentProcessor(IndexDocumentProcessor indexDocumentProcessor) {
        this.indexDocumentProcessor = indexDocumentProcessor;
    }

    public static void main(String[] args) {

    }
}
