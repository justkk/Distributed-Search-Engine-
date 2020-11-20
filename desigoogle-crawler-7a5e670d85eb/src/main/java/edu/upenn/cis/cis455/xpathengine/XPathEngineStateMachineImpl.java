package edu.upenn.cis.cis455.xpathengine;

import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.xpathengine.dom.DomTree;
import edu.upenn.cis.cis455.xpathengine.dom.XPathDomTreeMatcher;
import edu.upenn.cis.cis455.xpathengine.models.XPathEvent;
import edu.upenn.cis.cis455.xpathengine.models.XPathQuery;
import edu.upenn.cis.cis455.xpathengine.models.XPathQuerySet;
import edu.upenn.cis.cis455.xpathengine.paper.XPathPaperDocumentEventMatcherHandler;
import org.apache.commons.lang3.StringEscapeUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

public class XPathEngineStateMachineImpl implements XPathEngine {

    private XPathPaperDocumentEventMatcherHandler xPathPaperDocumentEventMatcherHandler;
    private String documentId = UUID.randomUUID().toString();
    private List<XPathQuerySet> xPathQuerySetList = new ArrayList<>();
    List<XPathQuery> xPathQueryList = new ArrayList<>();
    Map<String, Integer> xPathQueryMapIndex = new HashMap<>();
    Map<String, List<String>> idMatching = new HashMap<>();
    List<Boolean> booleanList = new ArrayList<>();

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    @Override
    public void setXPaths(String[] expressions) {

        int id = 0;
        int index = 0;
        for (String expression : expressions) {
            XPathQuerySet xPathQuerySet = XPathQuerySet.getXPathQuery(String.valueOf(id), expression);
            if(xPathQuerySet == null) {
                xPathQuerySetList.add(null);
                continue;
            }
            xPathQuerySetList.add(xPathQuerySet);
            for(XPathQuery xPathQuery : xPathQuerySet.getxPathQueryList()){
                xPathQueryList.add(xPathQuery);
                booleanList.add(false);
                xPathQueryMapIndex.put(xPathQuery.getQueryId(), index);
                if(!idMatching.containsKey(xPathQuerySet.getQueryId())) {
                    idMatching.put(xPathQuerySet.getQueryId(), new ArrayList<>());
                }
                idMatching.get(xPathQuerySet.getQueryId()).add(xPathQuery.getQueryId());
                index += 1;
            }
            id += 1;
        }

        xPathPaperDocumentEventMatcherHandler = new XPathPaperDocumentEventMatcherHandler(documentId, xPathQueryList);
    }

    public boolean didDocumentMatch(int i) {
        XPathQuerySet xPathQuerySet = xPathQuerySetList.get(i);
        if(xPathQuerySet == null) {
            return false;
        }

        List<Integer> indexes = idMatching.get(xPathQuerySet.getQueryId()).stream().map(id -> xPathQueryMapIndex.get(id))
                .collect(Collectors.toList());

        Optional<Boolean> answer = indexes.stream().map(index -> booleanList.get(index)).reduce(Boolean::logicalAnd);

        if(answer.isPresent())
            return answer.get();

        return false;
    }

    @Override
    public boolean isValid(int i) {
        XPathQuerySet xPathQuerySet = xPathQuerySetList.get(i);
        return xPathQuerySet == null;
    }


    @Override
    public boolean[] evaluateEvent(OccurrenceEvent event) {

        if(xPathPaperDocumentEventMatcherHandler == null) {
            return null;
        }

        List<String> matchingIDList = xPathPaperDocumentEventMatcherHandler
                .handleEvent(xPathPaperDocumentEventMatcherHandler.getxPathPaperDocumentState(), event.getxPathEvent());

        matchingIDList.stream().forEach(id -> {
            Integer index  = xPathQueryMapIndex.get(id);
            booleanList.set(index, true);
        });
        return getBooleanArray();
    }

    public boolean[] getBooleanArray() {

        boolean[] array = new boolean[xPathQuerySetList.size()];

        for(int i = 0; i < xPathQuerySetList.size(); i++) {
            array[i] = didDocumentMatch(i);
        }
        return array;
    }

    public List<Boolean> getBooleanList() {
        return booleanList;
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
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

        for (XPathEvent xPathEvent : handler.getxPathEventList()) {
            System.out.println(xPathEvent.getEventNode().getNodeName() + " : " + xPathEvent.getEventType().toString());
            boolean[] result = xPathEngine.evaluateEvent(new OccurrenceEvent(xPathEvent));
//            for(Boolean bool : xPathEngine.getBooleanList())
//                System.out.println(bool);
            for(Boolean bool : result)
                System.out.println(bool);

        }

        //System.out.println(true);
    }


}
