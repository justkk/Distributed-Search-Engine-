package edu.upenn.cis.cis455.service;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.service.URLDataExtractorService;
import edu.upenn.cis.cis455.crawler.service.URLInfoExtractorService;
import edu.upenn.cis.cis455.model.representationModels.URLMetaInformation;
import edu.upenn.cis.cis455.storage.managers.URLDataManager;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;

public class URLInfoExtractorServiceTest extends TestCase {

    // For Live Configuration
    private URLDataExtractorService urlDataExtractorService;
    // for data base;
    private URLDataManager urlDataManager;
    private URLInfoExtractorService urlInfoExtractorService;


    @Before
    public void setUp() {
        urlDataManager = Mockito.mock(URLDataManager.class);
        urlDataExtractorService = new URLDataExtractorService();
        urlInfoExtractorService = new URLInfoExtractorService(urlDataExtractorService, urlDataManager);
    }


    @Test
    public void testInfoExtractor1() {

        URLInfo urlInfo = new URLInfo("http://www.google.com");
        URLMetaInformation urlMetaInformation = urlInfoExtractorService.fetchGetInformationLive(urlInfo, new HashMap<>());
        Assert.assertEquals(urlMetaInformation.getStatusCode(), 200);
    }

    @Test
    public void testInfoExtractor2() {

        URLInfo urlInfo = new URLInfo("http://www.google.com");
        URLMetaInformation urlMetaInformation = urlInfoExtractorService.fetchHeadInformationLive(urlInfo, new HashMap<>());
        Assert.assertEquals(urlMetaInformation.getStatusCode(), 200);
    }
}
