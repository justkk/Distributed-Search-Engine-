package edu.upenn.cis.cis455.crawler.service;

import com.sleepycat.je.Transaction;
import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.model.representationModels.URLMetaInformation;
import edu.upenn.cis.cis455.model.representationModels.URLResponse;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfoKey;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;
import edu.upenn.cis.cis455.storage.managers.URLDataManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class URLInfoExtractorService {

    // For Live Configuration
    private URLDataExtractorService urlDataExtractorService;
    // for data base;
    private URLDataManager urlDataManager;

    private final String GET_METHOD = "GET";
    private final String HEAD_METHOD = "HEAD";

    private Logger logger = LogManager.getLogger(URLInfoExtractorService.class);


    public URLInfoExtractorService(URLDataExtractorService urlDataExtractorService, URLDataManager urlDataManager) {
        this.urlDataExtractorService = urlDataExtractorService;
        this.urlDataManager = urlDataManager;
    }

    public URLMetaInformation fetchHeadInformationLive(URLInfo urlInfo, Map<String, String> headers) {
        logger.debug("Head Request for: " + urlInfo.getUrl().toString());
        URLResponse urlResponse = urlDataExtractorService.getUrlInfoData(urlInfo,
                "HEAD", headers);
        if(urlResponse == null) {
            return null;
        }
        return new URLMetaInformation(urlInfo, urlResponse, HEAD_METHOD, headers);
    }

    public URLMetaInformation fetchGetInformationLive(URLInfo urlInfo, Map<String, String> headers) {
        logger.debug("Get Request for: " + urlInfo.getUrl().toString());
        URLResponse urlResponse = urlDataExtractorService.getUrlInfoData(urlInfo,
                "GET", ConstantsHW2.getInstance().getUrlInfoHeadHeaders());
        if(urlResponse == null || urlResponse.getStatusCode() < 200 || urlResponse.getStatusCode() >= 300) {
            return null;
        }
        return new URLMetaInformation(urlInfo, urlResponse, GET_METHOD, headers);
    }


    public URLMetaInformation fetchGetInformationFromDb(URLInfo urlInfo, Transaction txn) {
        logger.debug("Get Database Request for: " + urlInfo.getUrl().toString());
        URLDataInfoKey urlDataInfoKey = new URLDataInfoKey(getProtocol(urlInfo), urlInfo.getHostName(),
                urlInfo.getPortNo(), urlInfo.getFilePath(), GET_METHOD);
        URLDataInfo urlDataInfo = urlDataManager.getURLDataInfo(urlDataInfoKey, txn);
        if(urlDataInfo == null) {
            return null;
        }
        return new URLMetaInformation(urlInfo, urlDataInfo);
    }


    private String getProtocol(URLInfo urlInfo) {
        return urlInfo.isSecure()?"https":"http";
    }

    public static void main(String[] args) {
//        DataBaseConnectorConfig dataBaseConnectorConfig = new DataBaseConnectorConfig("/Users/nikhilt/Desktop/playground");
//        URLDataManager urlDataManager = new URLDataManager(dataBaseConnectorConfig);
//        URLDataExtractorService urlDataExtractorService = new URLDataExtractorService();
//        URLInfoExtractorService urlInfoExtractorService = new URLInfoExtractorService(urlDataExtractorService, urlDataManager);
//        urlInfoExtractorService.fetchGetInformationLive(new URLInfo("www.google.com", 80, "/")
//                , new HashMap<>());

    }

}
