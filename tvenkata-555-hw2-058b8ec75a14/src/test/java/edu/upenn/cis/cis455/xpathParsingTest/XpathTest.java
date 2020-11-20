package edu.upenn.cis.cis455.xpathParsingTest;

import edu.upenn.cis.cis455.xpathengine.models.XPathQuery;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

public class XpathTest extends TestCase {

    @Test
    public void testpositiveTest1() {
        String channel = "/a/b/c//d";
        XPathQuery xPathQuery = XPathQuery.getXPathQuery("1", channel);
        Assert.assertEquals(xPathQuery.getxPathQueryNodeList().size(), 4);

    }

    @Test
    public void testpositiveTest2() {
        String channel = "/a/b/c/*/d";
        XPathQuery xPathQuery = XPathQuery.getXPathQuery("1", channel);
        Assert.assertEquals(xPathQuery.getxPathQueryNodeList().size(), 4);

    }

    @Test
    public void testpositiveTest3() {
        String channel = "/a/b/c/*/d";
        XPathQuery xPathQuery = XPathQuery.getXPathQueryV2("1", channel);
        Assert.assertEquals(xPathQuery.getxPathQueryNodeList().size(), 5);

    }

    @Test
    public void positiveTest4() {
        String channel = "/a/b:K/c//d";
        XPathQuery xPathQuery = XPathQuery.getXPathQuery("1", channel);
        Assert.assertEquals(xPathQuery.getxPathQueryNodeList().size(), 4);

    }

    @Test
    public void testpositiveTest5() {
        String channel = "/a/b-K/c//d";
        XPathQuery xPathQuery = XPathQuery.getXPathQuery("1", channel);
        Assert.assertEquals(xPathQuery.getxPathQueryNodeList().size(), 4);

    }

    @Test
    public void positiveTest6() {
        String channel = "/a/7/c//d";
        XPathQuery xPathQuery = XPathQuery.getXPathQuery("1", channel);
        Assert.assertNull(xPathQuery);

    }

    @Test
    public void testpositiveTest7() {
        String channel = "/a/b7/c//d";
        XPathQuery xPathQuery = XPathQuery.getXPathQuery("1", channel);
        Assert.assertNotNull(xPathQuery);

    }
}
