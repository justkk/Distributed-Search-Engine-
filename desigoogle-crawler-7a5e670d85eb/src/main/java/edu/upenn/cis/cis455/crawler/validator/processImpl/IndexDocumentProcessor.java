package edu.upenn.cis.cis455.crawler.validator.processImpl;

import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.validator.IProcess;
import edu.upenn.cis.cis455.crawler.validator.ProcessContext;
import edu.upenn.cis.cis455.model.contentSeen.ContentSeenInfo;
import edu.upenn.cis.cis455.model.representationModels.URLMetaInformation;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfoKey;
import edu.upenn.cis.cis455.storage.managers.URLDataManager;
import edu.upenn.cis.cis455.utils.Md5HashGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class IndexDocumentProcessor implements IProcess {

    private Crawler crawler;
    private URLDataManager urlDataManager;

    private Logger logger = LogManager.getLogger(IndexDocumentProcessor.class);

    public IndexDocumentProcessor(Crawler crawler, URLDataManager urlDataManager) {
        this.crawler = crawler;
        this.urlDataManager = urlDataManager;
    }

    @Override
    public boolean validateRequest(ProcessContext processContext) {
        if (processContext.getGetInfo() != null && processContext.isValid()) {
            return true;
        }
        return false;
    }

    @Override
    public void processRequest(ProcessContext processContext) {
        if (!processContext.isValid()) {
            processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
            processContext.setMessage("IndexDocumentProcessor validation failed");
            return;
        }

        logger.debug("Index Checker");

        String md5Hash = Md5HashGenerator.getHash(processContext.getGetInfo().getData());
        processContext.setMd5Hash(md5Hash);

        if (processContext.getHeadInfo().getStatusCode() == 304) {
            System.out.println("No need to index this document, Not changed");
            return;
        }

        boolean shouldIInsert = false;

        if (processContext.getGetInfo().getData() == null || processContext.getGetInfo().getData().equals("")) {
            System.out.println("Empty Content Not Indexing");
            processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
            processContext.setMessage("IndexDocumentProcessor data is null");
            return;
        }


        ContentSeenInfo contentSeenInfo = urlDataManager.getContentSeenInfo(md5Hash,
                null);

        if (contentSeenInfo != null) {
            logger.debug("Already seen content not indexing");
            shouldIInsert = true;
        } else {
            synchronized (crawler) {
                try {
                    if (crawler.getCrawlDataStructure().canIndexNewDocument()) {
                        shouldIInsert = true;
                        System.out.println("Incrementing document indexed count");
                        crawler.getCrawlDataStructure().increamentCounter(1);
                    }
                } catch (Exception e) {
                    //logger.error(e.getMessage());
                } finally {
                    System.out.println("Did we index document " + shouldIInsert);
                }
            }
        }

        try {

            if (shouldIInsert) {
                URLMetaInformation urlMetaInformation = processContext.getGetInfo();
                URLDataInfoKey urlDataInfoKey = new URLDataInfoKey(getProtocol(processContext.getUrlInfo()),
                        processContext.getUrlInfo().getHostName(),
                        processContext.getUrlInfo().getPortNo(), processContext.getUrlInfo().getFilePath(),
                        urlMetaInformation.getRequestMethod());

                URLDataInfo urlDataInfo = new URLDataInfo(urlDataInfoKey);
                urlDataInfo.setContentLength(urlMetaInformation.getContentLength());
                urlDataInfo.setContentType(urlMetaInformation.getContentType());
                urlDataInfo.setData(urlMetaInformation.getData());
                urlDataInfo.setHeaders(urlMetaInformation.getHeaders());
                urlDataInfo.setStatusCode(urlMetaInformation.getStatusCode());
                urlDataInfo.setParentKeyString(processContext.getParentId());
                logger.debug("Adding url information to database");
                logger.debug("" + processContext.getUrlInfo().getUrl().toString());
                urlDataInfo = urlDataManager.insertURLDataInfo(urlDataInfo, null);
                processContext.setIndexed(true);
            }
        } catch (Exception e) {
            if(shouldIInsert){
                synchronized (crawler) {
                    //logger.error(e.getMessage());
                    logger.debug("Adjusting the counter index back");
                    crawler.getCrawlDataStructure().increamentCounter(-1);
                }
            }
            throw e;
        }
    }

    private String getProtocol(URLInfo urlInfo) {
        return urlInfo.isSecure() ? "https" : "http";
    }

    public boolean isIndexable(String content) {
        String md5Hash = Md5HashGenerator.getHash(content);
        ContentSeenInfo contentSeenInfo = urlDataManager.getContentSeenInfo(md5Hash, null);
        return contentSeenInfo==null;
    }

}
