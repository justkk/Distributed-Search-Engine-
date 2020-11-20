package edu.upenn.cis.cis455.xpathengine;

import edu.upenn.cis.cis455.model.OccurrenceEvent;
import edu.upenn.cis.cis455.xpathengine.dom.DomTree;
import edu.upenn.cis.cis455.xpathengine.dom.XPathDomTreeMatcher;
import edu.upenn.cis.cis455.xpathengine.models.XPathEvent;
import edu.upenn.cis.cis455.xpathengine.models.XPathQuery;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class XPathEngineImpl implements XPathEngine {

    DomTree domTree = new DomTree();
    List<XPathQuery> xPathQueryList = new ArrayList<>();
    List<Boolean> booleanList = new ArrayList<>();
    XPathDomTreeMatcher xPathDomTreeMatcher = new XPathDomTreeMatcher();

    @Override
    public void setXPaths(String[] expressions) {

        int id = 0;
        for (String expression : expressions) {
            XPathQuery xPathQuery = XPathQuery.getXPathQueryV2(String.valueOf(id), expression);
            xPathQueryList.add(xPathQuery);
            booleanList.add(false);
            id += 1;
        }
    }

    @Override
    public boolean isValid(int i) {
        XPathQuery xPathQuery = xPathQueryList.get(i);
        return xPathQuery == null;
    }

    public boolean didDocumentMatch(int i) {
        return booleanList.get(i);
    }

    @Override
    public boolean[] evaluateEvent(OccurrenceEvent event) {

        XPathEvent xPathEvent = event.getxPathEvent();
        DomTree.buildTree(domTree, xPathEvent);
        int index = 0;
        for (XPathQuery xPathQuery : xPathQueryList) {
            if (xPathQuery == null) {
                continue;
            }
            booleanList.set(index, xPathDomTreeMatcher.match(domTree, xPathQuery));
            index += 1;
        }
        boolean[] result = new boolean[booleanList.size()];
        for (int i = 0; i < booleanList.size(); i++) {
            result[i] = booleanList.get(i);
        }
        return result;
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
        Map<String, String> channels = new HashMap<>();
        channels.put("q1", "/a//b/*/c[contains(text(),\"hello\")]");
        String data = "<a><b><x></x><d><c>hello</c></d></b></a>";
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLTraversorHandler handler = new XMLTraversorHandler();
        saxParser.parse(new InputSource(new StringReader(data)), handler);
        XPathEngineImpl xPathEngine = new XPathEngineImpl();
        xPathEngine.setXPaths(channels.values().toArray(new String[0]));

        for (XPathEvent xPathEvent : handler.getxPathEventList()) {
            boolean[] result = xPathEngine.evaluateEvent(new OccurrenceEvent(xPathEvent));
            System.out.println("Step");
            System.out.println(result[0]);
        }

        System.out.println(true);
    }


}
