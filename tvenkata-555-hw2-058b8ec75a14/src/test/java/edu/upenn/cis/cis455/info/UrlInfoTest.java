package edu.upenn.cis.cis455.info;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class UrlInfoTest extends TestCase {

    @Test
    public void test1() {
        URLInfo urlInfo = new URLInfo("www.google.com");
        Assert.assertFalse(urlInfo.isValid());
    }

    @Test
    public void test2() {
        URLInfo urlInfo = new URLInfo("http://www.google.com");
        Assert.assertTrue(urlInfo.isValid());
        Assert.assertEquals(urlInfo.getFilePath(), "/");
        Assert.assertEquals(urlInfo.getHostName(), "www.google.com");
        Assert.assertFalse(urlInfo.isSecure());
    }

    @Test
    public void test3() {
        URLInfo urlInfo = new URLInfo("https://www.google.com/abc/");
        Assert.assertTrue(urlInfo.isValid());
        Assert.assertEquals(urlInfo.getFilePath(), "/abc/");
        Assert.assertEquals(urlInfo.getHostName(), "www.google.com");
        Assert.assertTrue(urlInfo.isSecure());
    }

    @Test
    public void test4() {
        URLInfo urlInfo = new URLInfo("http://www.google.com/");
        Assert.assertTrue(urlInfo.isValid());
        Assert.assertEquals(urlInfo.getFilePath(), "/");
        Assert.assertEquals(urlInfo.getHostName(), "www.google.com");
        Assert.assertFalse(urlInfo.isSecure());
    }

}
