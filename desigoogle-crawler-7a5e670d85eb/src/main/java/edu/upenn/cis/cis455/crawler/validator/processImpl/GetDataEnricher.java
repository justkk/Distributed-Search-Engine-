package edu.upenn.cis.cis455.crawler.validator.processImpl;

import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.crawler.DocType;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.service.URLInfoExtractorService;
import edu.upenn.cis.cis455.crawler.validator.IProcess;
import edu.upenn.cis.cis455.crawler.validator.ProcessContext;
import edu.upenn.cis.cis455.model.representationModels.URLMetaInformation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class GetDataEnricher implements IProcess {

    private URLInfoExtractorService urlInfoExtractorService;

    private Logger logger = LogManager.getLogger(GetDataEnricher.class);

    public GetDataEnricher(URLInfoExtractorService urlInfoExtractorService) {
        this.urlInfoExtractorService = urlInfoExtractorService;
    }

    @Override
    public boolean validateRequest(ProcessContext processContext) {
        if(processContext.getHeadInfo() != null && (processContext.getHeadInfo().getStatusCode() == 200
                || processContext.getHeadInfo().getStatusCode() == 304)) {
            return true;
        }
        return false;
    }

    @Override
    public void processRequest(ProcessContext processContext) {
        if(!validateRequest(processContext)) {
            processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
            processContext.setMessage("GetDataEnricher validation failed");
            return;
        }

        logger.debug("Enriching get data information");
        if(processContext.getHeadInfo().getStatusCode() == 304) {
            logger.warn(processContext.getUrlInfo().getUrl().toString() + ": Not Modified");
            processContext.setGetInfo(processContext.getGetInfoFromDb());
        } else {
            Map<String, String> headers = new HashMap<>(ConstantsHW2.getInstance().getUrlInfoGetHeaders());
            logger.warn(processContext.getUrlInfo().getUrl().toString() + ": Downloading");
            URLMetaInformation urlMetaInformation = urlInfoExtractorService.fetchGetInformationLive(
                    processContext.getUrlInfo(), headers);

            if(urlMetaInformation == null) {
                processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
                processContext.setMessage("GetDataEnricher request Failed ");
                return;
            }

            if(urlMetaInformation.getStatusCode() == 301
                    || urlMetaInformation.getStatusCode() == 302 || urlMetaInformation.getStatusCode() == 303) {
                String newLocation = urlMetaInformation.getHeaders().get("Location");
                if(newLocation.startsWith("/")) {
                    newLocation = processContext.getUrlInfo().getUrl().getProtocol() + "://" +
                            processContext.getUrlInfo().getUrl().getHost() + ":" +
                            processContext.getUrlInfo().getUrl().getPort() +
                            newLocation;
                }
                URLInfo newLocationUrlInfo = new URLInfo(newLocation);
                processContext.setRedirectUrlInfo(newLocationUrlInfo);
                processContext.setRedirect(true);
                return;
            }

            if(urlMetaInformation.getStatusCode() < 200 ||
                    urlMetaInformation.getStatusCode() > 300) {
                processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
                processContext.setMessage("GetDataEnricher request Failed ");
                return;
            }

            DocType docType = DocType.getTypeFromContentType(urlMetaInformation.getContentType());
            if(docType == null || docType == DocType.OTHER) {
                processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
                processContext.setMessage("GetDataEnricher Document Type Failed ");
                logger.debug("Document Type Failed");
                logger.debug(urlMetaInformation.getStatusCode());
                logger.debug(urlMetaInformation.getContentType());
                logger.debug(urlMetaInformation.getData());
                return;
            }

            int contentLength = urlMetaInformation.getContentLength();
            if(contentLength > processContext.getMaxDocumentSize()) {
                processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
                processContext.setMessage("GetDataEnricher Document Size Failed ");
                logger.debug("Document Size Failed");
                return;
            }


            processContext.setGetInfo(urlMetaInformation);
        }
    }

    public Boolean isOkToParse(URLInfo urlInfo, int maxDocumentSize) {

        if(!urlInfo.isValid()) return false;

        Map<String, String> headers = new HashMap<>(ConstantsHW2.getInstance().getUrlInfoGetHeaders());

        URLMetaInformation urlMetaInformation = urlInfoExtractorService.fetchGetInformationLive(
                urlInfo, headers);

        if(urlMetaInformation == null || urlMetaInformation.getStatusCode() < 200 ||
                urlMetaInformation.getStatusCode() > 300) {
            return false;
        }

        DocType docType = DocType.getTypeFromContentType(urlMetaInformation.getContentType());

        if(docType == null || docType == DocType.OTHER) {
            return false;
        }

        int contentLength = urlMetaInformation.getContentLength();

        if(contentLength > maxDocumentSize) {
            return false;
        }

        return true;
    }


}
