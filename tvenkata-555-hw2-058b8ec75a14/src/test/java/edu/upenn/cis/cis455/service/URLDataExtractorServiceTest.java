package edu.upenn.cis.cis455.service;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.service.URLDataExtractorService;
import edu.upenn.cis.cis455.model.representationModels.URLResponse;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;

public class URLDataExtractorServiceTest extends TestCase {

    private URLDataExtractorService urlDataExtractorService = new URLDataExtractorService();

    @Test
    public void testUrlHitTest1() {

        URLInfo urlInfo = new URLInfo("http://www.google.com");
        URLResponse urlResponse = urlDataExtractorService.getUrlInfoData(urlInfo, "GET", new HashMap<>());
        Assert.assertEquals(urlResponse.getStatusCode(), 200);
    }

    @Test
    public void testurlHitTest2() {

        URLInfo urlInfo = new URLInfo("https://www.google.com");
        URLResponse urlResponse = urlDataExtractorService.getUrlInfoData(urlInfo, "GET", new HashMap<>());
        Assert.assertEquals(urlResponse.getStatusCode(), 200);
    }

    @Test
    public void testurlHitTest3() {

        URLInfo urlInfo = new URLInfo("http://www.google.com");
        urlInfo.setHostName("http2");
        URLResponse urlResponse = urlDataExtractorService.getUrlInfoData(urlInfo, "GET", new HashMap<>());
        Assert.assertNull(urlResponse);
    }

}
