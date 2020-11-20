package edu.upenn.cis.cis455.xpathengine;

import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.ms2.DomParserBolt;
import edu.upenn.cis.cis455.xpathengine.dom.DomTree;
import edu.upenn.cis.cis455.xpathengine.models.XPathEvent;
import edu.upenn.cis.cis455.xpathengine.models.XPathEventNode;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;

public class XMLTraversorHandler extends DefaultHandler {

    private Stack<XPathEventNode> pathTracking = new Stack<>();
    List<XPathEvent> xPathEventList = new ArrayList<>();
    private DomParserBolt domParserBolt = null;
    private String docId;
    private StringBuilder stringBuilderState;
    private XPathEventNode pendingNode;

    public void setDomParserBolt(DomParserBolt domParserBolt) {
        this.domParserBolt = domParserBolt;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    @Override
    public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {
        //System.out.println("Start: " + qName);
        handlePendingNode();
        int level = 1;
        XPathEventNode parentNode = null;
        if(!pathTracking.empty()) {
            level = 1 + pathTracking.peek().getLevel();
            parentNode = pathTracking.peek();
        }
        XPathEventNode xPathEventNode = new XPathEventNode(qName, level);
        pathTracking.push(xPathEventNode);
        xPathEventNode.setParentNode(parentNode);
        XPathEvent xPathEvent = new XPathEvent(XPathEvent.EventType.START, xPathEventNode);
        xPathEventList.add(xPathEvent);
        callBack(xPathEvent);
    }
    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        handlePendingNode();
        // no op
        //System.out.println("Close: " + qName);
        XPathEventNode xPathEventNode = pathTracking.pop();
        XPathEvent xPathEvent = new XPathEvent(XPathEvent.EventType.END, xPathEventNode);
        xPathEventList.add(xPathEvent);
        callBack(xPathEvent);
    }
    @Override
    public void characters(char ch[], int start, int length)
            throws SAXException {

        if(pendingNode == null) {
            pendingNode = pathTracking.peek().copy();;
        }

        //System.out.println("Text Node from " + pathTracking.peek() + " : " + new String(ch, start, length));
//        XPathEventNode xPathEventNode = pathTracking.peek().copy();
        String s = new String(ch, start, length);
        stringBuilderState.append(s);
//        xPathEventNode.addTextNode(stringBuilderState.toString());
//        XPathEvent xPathEvent = new XPathEvent(XPathEvent.EventType.START, xPathEventNode);
//       xPathEventList.add(xPathEvent);
//       callBack(xPathEvent);
    }

    public void handlePendingNode() {

        if(pendingNode == null) {
            stringBuilderState = new StringBuilder();
            pendingNode = null;
            return;
        }
        pendingNode.addTextNode(stringBuilderState.toString().trim());
        XPathEvent xPathEvent = new XPathEvent(XPathEvent.EventType.START, pendingNode);
        xPathEventList.add(xPathEvent);
        callBack(xPathEvent);
        stringBuilderState = new StringBuilder();
        pendingNode = null;
    }

    public void callBack(XPathEvent xPathEvent) {

        if(domParserBolt == null || docId == null) {
            return;
        }
        OccurrenceEvent occurrenceEvent = new OccurrenceEvent(xPathEvent);
        domParserBolt.emitEvent(docId, occurrenceEvent);

    }

    public Stack<XPathEventNode> getPathTracking() {
        return pathTracking;
    }

    public void setPathTracking(Stack<XPathEventNode> pathTracking) {
        this.pathTracking = pathTracking;
    }

    public List<XPathEvent> getxPathEventList() {
        return xPathEventList;
    }

    public void setxPathEventList(List<XPathEvent> xPathEventList) {
        this.xPathEventList = xPathEventList;
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        String data = "<rss version=\"2.0\">\n" +
                "<channel>\n" +
                "<title>NYT &gt; Africa</title>\n" +
                "<link>\n" +
                "http://www.nytimes.com/pages/international/africa/index.html?partner=rssnyt\n" +

                "<hello>name</hello>nameTemp</link>\n" +
                "<description>\n" +
                "Find breaking news, world news, multimedia and opinion on Africa from South Africa, Egypt, Ethiopia, Libya, Rwanda, Kenya, Morocco, Zimbabwe, Sudan and Algeria.\n" +
                "</description>\n" +
                "<copyright>Copyright 2006 The New York Times Company</copyright>\n" +
                "<language>en-us</language>\n" +
                "<lastBuildDate>dfa</lastBuildDate>\n" +
                "<image>\n" +
                "<url>\n" +
                "http://graphics.nytimes.com/images/section/NytSectionHeader.gif\n" +
                "</url>\n" +
                "<title>NYT > Africa</title>\n" +
                "<link>\n" +
                "http://www.nytimes.com/pages/international/africa/index.html\n" +
                "</link>\n" +
                "</image>\n" +
                "<item>\n" +
                "<title>Egypt Insists That Hamas Stop Violence</title>\n" +
                "<link>\n" +
                "http://www.nytimes.com/2006/02/02/international/middleeast/02egypt.html?ex=1296536400&#38;en=06debaf57bef24dc&#38;ei=5088&#38;partner=rssnyt&#38;emc=rss\n" +
                "</link>\n" +
                "<description>\n" +
                "Egypt insisted that Hamas confirm existing agreements between Israel and the Palestinians and recognize Israel's legitimacy.\n" +
                "</description>\n" +
                "<author>STEVEN ERLANGER</author>\n" +
                "<pubDate>Thu, 02 Feb 2006 00:00:00 EDT</pubDate>\n" +
                "<guid isPermaLink=\"false\">\n" +
                "http://www.nytimes.com/2006/02/02/international/middleeast/02egypt.html\n" +
                "</guid>\n" +
                "</item>\n" +
                "<item>\n" +
                "<title>\n" +
                "World Briefings: Africa, Asia, Middle East, United Nations, Europe\n" +
                "</title>\n" +
                "<link>\n" +
                "http://www.nytimes.com/2006/02/03/international/03briefs.html?ex=1296622800&#38;en=17c287b37f35e7d9&#38;ei=5088&#38;partner=rssnyt&#38;emc=rss\n" +
                "</link>\n" +
                "fdfdafadsfads\n" +
                "<description>AFRICA.</description>\n" +
                "<author>(AP)</author>\n" +
                "<pubDate>Fri, 03 Feb 2006 00:00:00 EDT</pubDate>\n" +
                "<guid isPermaLink=\"false\">\n" +
                "http://www.nytimes.com/2006/02/03/international/03briefs.html\n" +
                "</guid>\n" +
                "</item>\n" +
                "<item>\n" +
                "<title>World Briefing: Africa, Americas, Europe and Asia</title>\n" +
                "<link>\n" +
                "http://www.nytimes.com/2006/02/02/international/02briefs.html?ex=1296536400&#38;en=cd101688c565f27f\\&#38;ei=5088&#38;partner=rssnyt&#38;emc=rss\n" +
                "</link>\n" +
                "<description>AFRICA.</description>\n" +
                "<pubDate>Thu, 02 Feb 2006 00:00:00 EDT</pubDate>\n" +
                "<guid isPermaLink=\"false\">\n" +
                "http://www.nytimes.com/2006/02/02/international/02briefs.html\n" +
                "</guid>\n" +
                "</item>\n" +
                "<item>\n" +
                "<title>Loan for Foreign Mining in Ghana Approved</title>\n" +
                "<link>\n" +
                "http://www.nytimes.com/2006/02/01/international/africa/01africa.html?ex=1296450000\\&#38;en=6ef2e47c65509682&#38;ei=5088\\&#38;partner=rssnyt\\&#38;emc=rss\n" +
                "</link>\n" +
                "<description>\n" +
                "The World Bank's investment agency said the loan was approved on the condition that Newmont Mining meet stringent social and environmental standards.\n" +
                "</description>\n" +
                "<author>CELIA W. DUGGER</author>\n" +
                "<pubDate>Wed, 01 Feb 2006 00:00:00 EDT</pubDate>\n" +
                "<guid isPermaLink=\"false\">\n" +
                "http://www.nytimes.com/2006/02/01/international/africa/01africa.html\n" +
                "</guid>\n" +
                "</item>\n" +
                "<item>\n" +
                "<title>\n" +
                "World Briefing: Asia, Middle East, Americas, Europe and Africa\n" +
                "</title>\n" +
                "<link>\n" +
                "http://www.nytimes.com/2006/02/01/international/01briefs.html?ex=1296450000&#38;en=a744dabed3339202&#38;ei=5088&#38;partner=rssnyt&#38;emc=rss\n" +
                "</link>\n" +
                "<description>ASIA.</description>\n" +
                "<author>SALMAN MASOOD (NYT)</author>\n" +
                "<pubDate>Wed, 01 Feb 2006 00:00:00 EDT</pubDate>\n" +
                "<guid isPermaLink=\"false\">\n" +
                "http://www.nytimes.com/2006/02/01/international/01briefs.html\n" +
                "</guid>\n" +
                "</item>\n" +
                "<item>\n" +
                "<title>\n" +
                "Khartoum Journal: Sudan Leader Waits, and Waits, for His Ship to Come In\n" +
                "</title>\n" +
                "<link>\n" +
                "http://www.nytimes.com/2006/01/31/international/africa/31khartoum.html?ex=1296363600&#38;en=c514139910f2888c&#38;ei=5088&#38;partner=rssnyt&#38;emc=rss\n" +
                "</link>\n" +
                "<description>\n" +
                "For a war-torn, impoverished country, a gigantic, luxurious presidential yacht.\n" +
                "</description>\n" +
                "<author>MARC LACEY</author>\n" +
                "<pubDate>Tue, 31 Jan 2006 00:00:00 EDT</pubDate>\n" +
                "<guid isPermaLink=\"false\">\n" +
                "http://www.nytimes.com/2006/01/31/international/africa/31khartoum.html\n" +
                "</guid>\n" +
                "</item>\n" +
                "</channel>\n" +
                "</rss>";

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();

        XMLTraversorHandler handler = new XMLTraversorHandler();

        saxParser.parse(new InputSource(new StringReader(data)), handler);

        DomTree domTree = DomTree.buildTree(handler.getxPathEventList());
        System.out.println(domTree);


    }
}
