package edu.upenn.cis.cis455.utils;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class DocParserTest extends TestCase {

    private DocUrlParser docUrlParser = new DocUrlParser();

    String data = "<HTML>\n" +
            "    <HEAD>\n" +
            "        <TITLE>CSE455/CIS555 HW2 Sample Data</TITLE>\n" +
            "    </HEAD>\n" +
            "    <BODY>\n" +
            "        <H2 ALIGN=center>CSE455/CIS555 HW2 Sample Data</H2>\n" +
            "        <P>This page contains some sample data for your second homework \n" +
            "assignment. The HTML pages do not contain external links, so you shouldn't \n" +
            "have to worry about your crawler &ldquo;escaping&rdquo; to the outside \n" +
            "web. The XML files do, however, contain links to external URLs, so \n" +
            "you'll need to make sure your crawler does not follow links in XML \n" +
            "documents.</P>\n" +
            "        <H3>RSS Feeds</H3>\n" +
            "        <UL>\n" +
            "            <LI>\n" +
            "                <A HREF=\"crawltest/nytimes/\">The New York Times</A>\n" +
            "            </LI>\n" +
            "            <LI>\n" +
            "                <A HREF=\"crawltest/bbc/\">BBC News</A>\n" +
            "            </LI>\n" +
            "            <LI>\n" +
            "                <A HREF=\"crawltest/cnn/\">CNN</A>\n" +
            "            </LI>\n" +
            "            <LI>\n" +
            "                <A HREF=\"crawltest/international/\">News in foreign \n" +
            "languages</A>\n" +
            "            </LI>\n" +
            "        </UL>\n" +
            "        <H3>Other XML data</H3>\n" +
            "        <UL>\n" +
            "            <LI>\n" +
            "                <A HREF=\"crawltest/misc/weather.xml\">Weather data</A>\n" +
            "            </LI>\n" +
            "            <LI>\n" +
            "                <A HREF=\"crawltest/misc/eurofxref-daily.xml\">Current Euro exchange \n" +
            "rate data</A>\n" +
            "            </LI>\n" +
            "            <LI>\n" +
            "                <A HREF=\"crawltest/misc/eurofxref-hist.xml\">Historical Euro exchange \n" +
            "rate data</A>\n" +
            "            </LI>\n" +
            "        </UL>\n" +
            "        <H3>Marie's XML data</H3>\n" +
            "        <UL>\n" +
            "            <LI>\n" +
            "                <A HREF=\"crawltest/marie/\">More data</A>\n" +
            "            </LI>\n" +
            "            <LI>\n" +
            "                <A HREF=\"crawltest/marie/private\">Private</A>\n" +
            "            </LI>\n" +
            "        </UL>\n" +
            "    </BODY>\n" +
            "</HTML>";

    @Test
    public void testUrls() {

        List<URLInfo> urlInfoList = docUrlParser.enrichLinks("http://www.temp.com", data);
        Assert.assertEquals(urlInfoList.size(), 9);
        Assert.assertEquals("http://www.temp.com/crawltest/nytimes/", urlInfoList.get(0).getUrl().toString());
    }
}
