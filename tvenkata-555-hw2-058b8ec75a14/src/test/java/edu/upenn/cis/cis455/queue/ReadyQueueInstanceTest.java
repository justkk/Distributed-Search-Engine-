package edu.upenn.cis.cis455.queue;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class ReadyQueueInstanceTest extends TestCase {

    @Test
    public void testCheckEquality1() {

        URLInfo urlInfo1 = new URLInfo("http://www.google.com");
        URLInfo urlInfo2 = new URLInfo("http://www.google.com/");
        ReadyQueueInstance readyQueueInstance1 = new ReadyQueueInstance(urlInfo1);
        ReadyQueueInstance readyQueueInstance2 = new ReadyQueueInstance(urlInfo2);
        Assert.assertEquals(readyQueueInstance1, readyQueueInstance2);

    }

    @Test
    public void testCheckEquality2() {

        URLInfo urlInfo1 = new URLInfo("http://www.google.com");
        URLInfo urlInfo2 = new URLInfo("http://www.google.com/abc");
        ReadyQueueInstance readyQueueInstance1 = new ReadyQueueInstance(urlInfo1);
        ReadyQueueInstance readyQueueInstance2 = new ReadyQueueInstance(urlInfo2);
        Assert.assertNotEquals(readyQueueInstance1, readyQueueInstance2);

    }

}
