package edu.upenn.cis.cis455.crawler.validator.processImpl;

import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.crawler.DocType;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.service.URLInfoExtractorService;
import edu.upenn.cis.cis455.crawler.validator.IProcess;
import edu.upenn.cis.cis455.crawler.validator.ProcessContext;
import edu.upenn.cis.cis455.model.representationModels.URLMetaInformation;
import edu.upenn.cis.cis455.model.representationModels.URLResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeadDataEnricher implements IProcess {

    private URLInfoExtractorService urlInfoExtractorService;
    private final SimpleDateFormat formatter1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");

    private Logger logger = LogManager.getLogger(HeadDataEnricher.class);

    public HeadDataEnricher(URLInfoExtractorService urlInfoExtractorService) {
        this.urlInfoExtractorService = urlInfoExtractorService;
    }

    @Override
    public boolean validateRequest(ProcessContext processContext) {
        if (processContext.isValid() && processContext.getCrawlDelayManagerOutput() != null
                && processContext.getCrawlDelayManagerOutput().isSuccess()) {
            return true;
        }
        return false;
    }

    @Override
    public void processRequest(ProcessContext processContext) {
        if (!validateRequest(processContext)) {
            processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
            processContext.setMessage("HeadDataEnricher validation failed");
            return;
        }

        logger.debug("Enriching process context with head data");

        Map<String, String> headers = new HashMap<>(ConstantsHW2.getInstance().getUrlInfoHeadHeaders());
        if (processContext.getGetInfoFromDb() != null) {
            Date lastModifiedTime = processContext.getGetInfoFromDb().getLastModifiedTime();
            logger.debug("Adding If-Modified-Since header " + formatter1.format(lastModifiedTime));
            headers.put("If-Modified-Since", formatter1.format(lastModifiedTime));
        }
        URLMetaInformation urlMetaInformation = urlInfoExtractorService.fetchHeadInformationLive(
                processContext.getUrlInfo(), headers);

        if (urlMetaInformation == null) {
            // to handle null head requests.
            processContext.setHeadInfo(new URLMetaInformation(processContext.getUrlInfo(),
                    new URLResponse(200, "", "", -1, 0, new HashMap<>()),
                    "HEAD", new HashMap<>()));
            return;
        }

        if (urlMetaInformation.getStatusCode() >= 200 && urlMetaInformation.getStatusCode() < 300) {
            DocType docType = DocType.getTypeFromContentType(urlMetaInformation.getContentType());
            if (docType == null || docType == DocType.OTHER) {
                processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
                processContext.setMessage("HeadDataEnricher Document Type Failed ");
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

        }

        if(urlMetaInformation.getStatusCode() == 301
                || urlMetaInformation.getStatusCode() == 302 || urlMetaInformation.getStatusCode() == 303) {
            List<String> newLocationList = urlMetaInformation.getResponseHeaders().get("location");
            if(newLocationList == null || newLocationList.size() == 0) {
                processContext.setStatus(ProcessContext.ProcessContextStatus.FAILURE);
                processContext.setMessage("GetDataEnricher redirection Failed ");
                logger.debug("redirection Failed");
                return;
            }
            String newLocation = newLocationList.get(0);
            if(newLocation.startsWith("/")) {
                newLocation = processContext.getUrlInfo().getUrl().getProtocol() + "://" +
                        processContext.getUrlInfo().getUrl().getHost() + ":" +
                        processContext.getUrlInfo().getUrl().getPort() +
                        newLocation;
            }
            URLInfo newLocationUrlInfo = new URLInfo(newLocation);
            processContext.setRedirectUrlInfo(newLocationUrlInfo);
            processContext.setRedirect(true);
            processContext.setSkip(true);
            return;
        }


        processContext.setHeadInfo(urlMetaInformation);
    }

    public Boolean isOKtoParse(URLInfo urlInfo, URLMetaInformation getDBInformation, int maxDocumentSize) {
        if(!urlInfo.isValid()) return false;

        Map<String, String> headers = new HashMap<>(ConstantsHW2.getInstance().getUrlInfoHeadHeaders());
        if (getDBInformation != null) {
            Date lastModifiedTime = getDBInformation.getLastModifiedTime();
            headers.put("If-Modified-Since", formatter1.format(lastModifiedTime));
        }
        URLMetaInformation urlMetaInformation = urlInfoExtractorService.fetchHeadInformationLive(
                urlInfo, headers);

        if (urlMetaInformation == null) {
            return false;
        }

        if (urlMetaInformation.getStatusCode() >= 200 && urlMetaInformation.getStatusCode() < 300) {
            DocType docType = DocType.getTypeFromContentType(urlMetaInformation.getContentType());
            if (docType == null || docType == DocType.OTHER) {
                return false;
            }
            int contentLength = urlMetaInformation.getContentLength();
            if(contentLength > maxDocumentSize) {
                return false;
            }
            if(contentLength == -1) {
                return null;
            }
            return true;
        }

        if(urlMetaInformation.getStatusCode() == 304) {
            return true;
        }
        return false;
    }
}
