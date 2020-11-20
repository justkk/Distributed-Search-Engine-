package edu.upenn.cis.cis455.crawler.validator.processImpl;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.service.URLInfoExtractorService;
import edu.upenn.cis.cis455.crawler.validator.IProcess;
import edu.upenn.cis.cis455.crawler.validator.ProcessContext;
import edu.upenn.cis.cis455.model.representationModels.URLMetaInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DBDataEnricher implements IProcess {

    private URLInfoExtractorService urlInfoExtractorService;

    private Logger logger = LogManager.getLogger(DBDataEnricher.class);

    public DBDataEnricher(URLInfoExtractorService urlInfoExtractorService) {
        this.urlInfoExtractorService = urlInfoExtractorService;
    }

    @Override
    public boolean validateRequest(ProcessContext processContext) {
        if(processContext.isValid() && processContext.getCrawlDelayManagerOutput()!=null
                && processContext.getCrawlDelayManagerOutput().isSuccess()) {
            return true;
        }
        return false;
    }

    @Override
    public void processRequest(ProcessContext processContext) {

        if(!validateRequest(processContext)) {
            processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
            processContext.setMessage("HeadRequestChecker validation failed");
            return;
        }

        logger.debug("Enriching process content with get information from database");

        URLMetaInformation urlMetaInformation = urlInfoExtractorService.fetchGetInformationFromDb(
                processContext.getUrlInfo(), processContext.getTxn());
        processContext.setGetInfoFromDb(urlMetaInformation);
    }

    public URLMetaInformation getDBInformation(URLInfo urlInfo) {
        if(!urlInfo.isValid()) return null;
        URLMetaInformation urlMetaInformation = urlInfoExtractorService.fetchGetInformationFromDb(
                urlInfo, null);
        return urlMetaInformation;
    }
}
