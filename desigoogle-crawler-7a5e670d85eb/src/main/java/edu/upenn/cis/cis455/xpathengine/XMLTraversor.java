package edu.upenn.cis.cis455.xpathengine;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.Stack;

public class XMLTraversor {

    private String data;


    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        String data = "<rss version=\"2.0\">\n" +
                "<channel>\n" +
                "<title>NYT > Africa</title>\n" +
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
        DefaultHandler handler = new DefaultHandler() {

            Stack stack = new Stack();

            public void startElement(String uri, String localName,
                                     String qName, Attributes attributes)
                    throws SAXException {
                System.out.println("Start: " + qName);
                stack.push(qName);
            }

            public void endElement(String uri, String localName, String qName)
                    throws SAXException {
                // no op
                System.out.println("Close: " + qName);
                stack.pop();
            }

            public void characters(char ch[], int start, int length)
                    throws SAXException {

                System.out.println("Text Node from " + stack.peek() + " : " + new String(ch, start, length));
            }

        };

        saxParser.parse(new InputSource(new StringReader(data)), handler);


//        Document document = Jsoup.parse(data, "https://dbappserv.cis.upenn.edu/crawltest.html");
//        NodeVisitor nodeVisitor = new NodeVisitorImpl();
//        NodeTraversor nodeTraversor = new NodeTraversor(nodeVisitor);
//        Element element = document.body();
//        nodeTraversor.traverse(element);

    }
}
