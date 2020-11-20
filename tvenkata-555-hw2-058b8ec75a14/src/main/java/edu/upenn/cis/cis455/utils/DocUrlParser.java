package edu.upenn.cis.cis455.utils;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.model.representationModels.DocInfo;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

import java.util.ArrayList;
import java.util.List;

public class DocUrlParser {


    public List<URLInfo> enrichLinks(String baseUrl, String data) {

        if(data == null) {
            return new ArrayList<>();
        }

        Document document = Jsoup.parse(data, baseUrl);
        NodeVisitorImpl nodeVisitor = new NodeVisitorImpl();
        NodeTraversor nodeTraversor = new NodeTraversor(nodeVisitor);
        nodeTraversor.traverse(document.body());
        return nodeVisitor.getUrlInfoList();
    }

    public static class NodeVisitorImpl implements NodeVisitor {

        private List<URLInfo> urlInfoList;
        public NodeVisitorImpl() {
            this.urlInfoList = new ArrayList<>();
        }

        public List<URLInfo> getUrlInfoList() {
            return urlInfoList;
        }

        @Override
        public void head(Node node, int i) {
            if(node instanceof Element) {
                Element element = (Element) node;

                // parse anchor tag:
                if("a".equals(element.nodeName())) {
                    String url = element.absUrl("href");
                    if(url != null) {
                        urlInfoList.add(new URLInfo(url));
                    }

                }
                //System.out.println(node.nodeName());
            }
        }

        @Override
        public void tail(Node node, int i) {
            if(node instanceof Element) {
                Element element = (Element) node;
                //System.out.println("/" + node.nodeName());
            }
        }

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

        Document document = Jsoup.parse(data, "https://dbappserv.cis.upenn.edu/crawltest.html");
        NodeVisitor nodeVisitor = new NodeVisitorImpl();
        NodeTraversor nodeTraversor = new NodeTraversor(nodeVisitor);
        Element element = document.body();
        nodeTraversor.traverse(element);

    }
}
