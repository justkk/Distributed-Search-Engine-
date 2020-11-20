package edu.upenn.cis.cis455.utils;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.ms2.DomParserBolt;
import edu.upenn.cis.cis455.xpathengine.models.XPathEvent;
import edu.upenn.cis.cis455.xpathengine.models.XPathEventNode;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class HTMLTraversorHandler {


    private String data;
    private String url;
    private DomParserBolt domParserBolt;
    private String docId;

    public HTMLTraversorHandler(String data, String url, DomParserBolt domParserBolt, String docId) {
        this.data = data;
        this.url = url;
        this.docId = docId;
    }

    public void parseHTML(){

        Document document = Jsoup.parse(data, url);
        NodeVisitor nodeVisitor = new NodeVisitorImpl();
        NodeTraversor nodeTraversor = new NodeTraversor(nodeVisitor);
        Node element = document.select("html").first();
        nodeTraversor.traverse(element);

    }

    public class NodeVisitorImpl implements NodeVisitor {

        private List<URLInfo> urlInfoList;
        public NodeVisitorImpl() {
            this.urlInfoList = new ArrayList<>();
        }
        public List<URLInfo> getUrlInfoList() {
            return urlInfoList;
        }
        List<XPathEvent> xPathEventList = new ArrayList<>();
        private int level = 1;
        private Stack<XPathEventNode> pathTracking = new Stack<>();

        @Override
        public void head(Node node, int i) {
            if(node instanceof Element) {
                //System.out.println("Start: " + node.nodeName());
                Element element = (Element) node;

                List<TextNode> textNodes = element.textNodes();

                int level = 1;
                XPathEventNode parentNode = null;
                if(!pathTracking.empty()) {
                    level = 1 + pathTracking.peek().getLevel();
                    parentNode = pathTracking.peek();
                }
                XPathEventNode xPathEventNode = new XPathEventNode(node.nodeName(), level);

                for(TextNode textNode : textNodes) {
                    xPathEventNode.getTextNodes().add(textNode.text());
                    //System.out.println("       Text: " + textNode.text());
                }
                pathTracking.push(xPathEventNode);
                xPathEventNode.setParentNode(parentNode);
                XPathEvent xPathEvent = new XPathEvent(XPathEvent.EventType.START, xPathEventNode);
                xPathEventList.add(xPathEvent);
                callBack(xPathEvent);
            }
        }

        @Override
        public void tail(Node node, int i) {
            if(node instanceof Element) {
                //System.out.println("Close: " + node.nodeName());
                XPathEventNode xPathEventNode = pathTracking.pop();
                XPathEvent xPathEvent = new XPathEvent(XPathEvent.EventType.END, xPathEventNode);
                xPathEventList.add(xPathEvent);
                callBack(xPathEvent);
            }
        }
    }

    public void callBack(XPathEvent xPathEvent) {
        if(domParserBolt == null || docId == null) {
            return;
        }
        OccurrenceEvent occurrenceEvent = new OccurrenceEvent(xPathEvent);
        domParserBolt.emitEvent(docId, occurrenceEvent);
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public DomParserBolt getDomParserBolt() {
        return domParserBolt;
    }

    public void setDomParserBolt(DomParserBolt domParserBolt) {
        this.domParserBolt = domParserBolt;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public static void main(String[] args) {
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
                "        <UL> HLLLLLLLL\n" +
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

//        Document document = Jsoup.parse(data, "https://dbappserv.cis.upenn.edu/crawltest.html");
//        NodeVisitor nodeVisitor = new NodeVisitorImpl();
//        NodeTraversor nodeTraversor = new NodeTraversor(nodeVisitor);
//        Element element = document.body();
//        nodeTraversor.traverse(element);

        HTMLTraversorHandler htmlTraversorHandler = new HTMLTraversorHandler(data, "https://dbappserv.cis.upenn.edu/crawltest.html", null, "1");
        htmlTraversorHandler.parseHTML();
    }
}
