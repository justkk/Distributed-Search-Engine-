package edu.upenn.cis.cis455.xmlMatching;

import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.xpathengine.XMLTraversorHandler;
import edu.upenn.cis.cis455.xpathengine.XPathEngineStateMachineImpl;
import edu.upenn.cis.cis455.xpathengine.models.XPathEvent;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class XmlMatchingTest extends TestCase {

    @Test
    public void testpositiveTest1() throws ParserConfigurationException, SAXException, IOException {

        Map<String, String> channels = new HashMap<>();
        String gg = "/rss/channel/item/title[contains(text(),\">\")]";
        channels.put("unsus", gg);
        String data = "<rss version=\"2.0\">\n" +
                "<channel>\n" +
                "<item>\n" +
                "<title>Infertility &gt; link in iceman's DNA</title>\n" +
                "<description>\n" +
                "Oetzi, the \"unsustainable\" man frozen in a glacier for 5,000 years, may have been infertile, research suggests.\n" +
                "</description>\n" +
                "<link>\n" +
                "http://news.bbc.co.uk/go/rss/-/2/hi/science/nature/4674866.stm\n" +
                "</link>\n" +
                "<guid isPermaLink=\"false\">http://news.bbc.co.uk/1/hi/sci/tech/4674866.stm</guid>\n" +
                "<pubDate>Fri, 03 Feb 2006 09:17:39 GMT</pubDate>\n" +
                "<category>Science/Nature</category>\n" +
                "</item>\n" +
                "</channel>\n" +
                "</rss>";
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLTraversorHandler handler = new XMLTraversorHandler();
        saxParser.parse(new InputSource(new StringReader(data)), handler);
        XPathEngineStateMachineImpl xPathEngine = new XPathEngineStateMachineImpl();
        xPathEngine.setXPaths(channels.values().toArray(new String[0]));
        boolean testResult = false;

        for (XPathEvent xPathEvent : handler.getxPathEventList()) {
            System.out.println(xPathEvent.getEventNode().getNodeName() + " : " + xPathEvent.getEventType().toString());
            boolean[] result = xPathEngine.evaluateEvent(new OccurrenceEvent(xPathEvent));
            testResult = result[0];
        }
        Assert.assertTrue(testResult);

    }

    @Test
    public void testpositiveTest2() throws ParserConfigurationException, SAXException, IOException {

        Map<String, String> channels = new HashMap<>();
        String gg = "/rss/channel/item/title[text() = \"Infertility > link in iceman's DNA\"]";
        channels.put("unsus", gg);
        String data = "<rss version=\"2.0\">\n" +
                "<channel><item></item>\n" +
                "<item>\n" +
                "<title>Infertility &gt; link in iceman's DNA</title>\n" +
                "<description>\n" +
                "Oetzi, the \"unsustainable\" man frozen in a glacier for 5,000 years, may have been infertile, research suggests.\n" +
                "</description>\n" +
                "<link>\n" +
                "http://news.bbc.co.uk/go/rss/-/2/hi/science/nature/4674866.stm\n" +
                "</link>\n" +
                "<guid isPermaLink=\"false\">http://news.bbc.co.uk/1/hi/sci/tech/4674866.stm</guid>\n" +
                "<pubDate>Fri, 03 Feb 2006 09:17:39 GMT</pubDate>\n" +
                "<category>Science/Nature</category>\n" +
                "</item>\n" +
                "</channel>\n" +
                "</rss>";
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLTraversorHandler handler = new XMLTraversorHandler();
        saxParser.parse(new InputSource(new StringReader(data)), handler);
        XPathEngineStateMachineImpl xPathEngine = new XPathEngineStateMachineImpl();
        xPathEngine.setXPaths(channels.values().toArray(new String[0]));
        boolean testResult = false;

        for (XPathEvent xPathEvent : handler.getxPathEventList()) {
            System.out.println(xPathEvent.getEventNode().getNodeName() + " : " + xPathEvent.getEventType().toString());
            boolean[] result = xPathEngine.evaluateEvent(new OccurrenceEvent(xPathEvent));
            testResult = result[0];
        }
        Assert.assertTrue(testResult);

    }


    @Test
    public void testpositiveTest3() throws ParserConfigurationException, SAXException, IOException {

        Map<String, String> channels = new HashMap<>();
        String gg = "/rss/channel/item/title[text() = \"Infertility > link in iceman's DNA\"]";
        channels.put("unsus", gg);
        String data = "<rss version=\"2.0\">\n" +
                "<channel>" +
                "<item>\n" +
                "<title>" +
                "<description>\n" +
                "Oetzi, the \"unsustainable\" man frozen in a glacier for 5,000 years, may have been infertile, research suggests.\n" +
                "</description>" +
                "Infertility &gt; link in iceman's DNA\n</title>\n" +
                "</item>\n" +
                "</channel>\n" +
                "</rss>";
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLTraversorHandler handler = new XMLTraversorHandler();
        saxParser.parse(new InputSource(new StringReader(data)), handler);
        XPathEngineStateMachineImpl xPathEngine = new XPathEngineStateMachineImpl();
        xPathEngine.setXPaths(channels.values().toArray(new String[0]));
        boolean testResult = false;

        for (XPathEvent xPathEvent : handler.getxPathEventList()) {
            System.out.println(xPathEvent.getEventNode().getNodeName() + " : " + xPathEvent.getEventType().toString());
            boolean[] result = xPathEngine.evaluateEvent(new OccurrenceEvent(xPathEvent));
            testResult = result[0];
        }
        Assert.assertTrue(testResult);

    }

    @Test
    public void testpositiveTest4() throws ParserConfigurationException, SAXException, IOException {

        Map<String, String> channels = new HashMap<>();
        String gg = "/rss/channel/item/title[text() = \"Infertility > link in iceman's DNA\"]/temp[contains(text(), \"hello\")]";
        channels.put("unsus", gg);
        String data = "<rss version=\"2.0\">\n" +
                "<channel>" +
                "<item>\n" +
                "<title>" +
                "<description>\n" +
                "Oetzi, the \"unsustainable\" man frozen in a glacier for 5,000 years, may have been infertile, research suggests.\n" +
                "</description><temp>hello</temp>" +
                "Infertility &gt; link in iceman's DNA\n</title>\n" +
                "</item>\n" +
                "</channel>\n" +
                "</rss>";
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLTraversorHandler handler = new XMLTraversorHandler();
        saxParser.parse(new InputSource(new StringReader(data)), handler);
        XPathEngineStateMachineImpl xPathEngine = new XPathEngineStateMachineImpl();
        xPathEngine.setXPaths(channels.values().toArray(new String[0]));
        boolean testResult = false;

        for (XPathEvent xPathEvent : handler.getxPathEventList()) {
            System.out.println(xPathEvent.getEventNode().getNodeName() + " : " + xPathEvent.getEventType().toString());
            boolean[] result = xPathEngine.evaluateEvent(new OccurrenceEvent(xPathEvent));
            testResult = result[0];
        }
        Assert.assertTrue(testResult);

    }
}
