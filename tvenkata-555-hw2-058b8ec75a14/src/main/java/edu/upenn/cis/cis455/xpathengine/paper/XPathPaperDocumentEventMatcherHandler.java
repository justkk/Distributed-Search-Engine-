package edu.upenn.cis.cis455.xpathengine.paper;

import edu.upenn.cis.cis455.xpathengine.XMLTraversorHandler;
import edu.upenn.cis.cis455.xpathengine.models.*;
import org.apache.commons.lang3.StringEscapeUtils;
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

public class XPathPaperDocumentEventMatcherHandler {

    private XPathPaperDocumentState xPathPaperDocumentState;
    private Map<String, String> channels;

    public XPathPaperDocumentEventMatcherHandler(String documentId, List<XPathQuery> xPathQueryList) {
        this.xPathPaperDocumentState = getDocumentState(documentId, xPathQueryList);
        this.channels = channels;
    }

    // channels is id and xpathString
    public XPathPaperDocumentState getDocumentState(String documentId, List<XPathQuery> xPathQueryList) {

        List<XPathQuery> xPathQueries = xPathQueryList;
        XPathPaperDocumentState xPathPaperDocumentState = XPathPaperDocumentState.getXPathDocumentState(documentId, xPathQueries);
        return xPathPaperDocumentState;
    }


    public List<String> handleEvent(XPathPaperDocumentState xPathPaperDocumentState, XPathEvent xPathEvent) {
        List<String> matchingChannels = new ArrayList<>();
        switch (xPathEvent.getEventType()) {
            case START:
                matchingChannels = handleStartEvent(xPathPaperDocumentState, xPathEvent.getEventNode());
                break;
            case END:
                handleCloseEvent(xPathPaperDocumentState, xPathEvent.getEventNode());
                break;
            default:
                break;
        }
        return matchingChannels;

    }

    public List<String> handleStartEvent(XPathPaperDocumentState xPathPaperDocumentState, XPathEventNode xPathEventNode) {

        String nodeName = xPathEventNode.getNodeName();
        List<String> matchingChannels = new ArrayList<>();
        if(!xPathPaperDocumentState.getQueryIndex().containsKey(nodeName)) {
            return matchingChannels;
        }
        XPathPaperQueryIndexModel xPathPaperQueryIndexModel = xPathPaperDocumentState.getQueryIndex().get(nodeName);
        List<XPathQueryNode> xPathQueryNodeList = xPathPaperQueryIndexModel.getCandidateList();
        for(XPathQueryNode xPathQueryNode : xPathQueryNodeList) {
            if(xPathQueryNode.getRelativePosition() != -1 && xPathQueryNode.getLevel() != xPathEventNode.getLevel()) {
                continue;
            }
            if(!isMatch(xPathQueryNode, xPathEventNode)) {
                continue;
            }

            XPathQueryNode nextNode = xPathQueryNode.getDefaultPointer();
            if(nextNode == null) {
                matchingChannels.add(xPathQueryNode.getQueryId());
                continue;
            }
            if(xPathQueryNode.getNodeMap().containsKey(xPathEventNode.getId())) {
                continue;
            }
            XPathQueryNode copyNode = nextNode.copy();
            xPathQueryNode.getNodeMap().put(xPathEventNode.getId(), copyNode);
            if(copyNode.getRelativePosition()!=-1)
                copyNode.setLevel(xPathEventNode.getLevel() + copyNode.getRelativePosition());
            else
                copyNode.setLevel(0);
            xPathPaperDocumentState.getQueryIndex().get(copyNode.getNodeName()).getCandidateList().add(copyNode);
        }
        return matchingChannels;
    }

    public void handleCloseEvent(XPathPaperDocumentState xPathPaperDocumentState, XPathEventNode xPathEventNode) {

        if(!xPathPaperDocumentState.getQueryIndex().containsKey(xPathEventNode.getNodeName())) {
            return;
        }

        List<XPathQueryNode> queryNodeList = xPathPaperDocumentState.getQueryIndex().get(xPathEventNode.getNodeName()).getCandidateList();
        for(XPathQueryNode queryNode : queryNodeList) {
            if(!queryNode.getNodeMap().containsKey(xPathEventNode.getId())) {
                continue;
            }
            XPathQueryNode nodeMapPointer = queryNode.getNodeMap().get(xPathEventNode.getId());
            queryNode.getNodeMap().remove(xPathEventNode.getId());
            xPathPaperDocumentState.getQueryIndex().get(nodeMapPointer.getNodeName()).getCandidateList().remove(nodeMapPointer);
        }
    }


    public boolean isMatch(XPathQueryNode xPathQueryNode, XPathEventNode xPathEventNode) {

        if(xPathQueryNode.getNodeName().equals(xPathEventNode.getNodeName()) || xPathQueryNode.isWildCard()) {
            boolean success = compareFilters(xPathQueryNode, xPathEventNode);
            return success;
        }
        return false;

    }

    private boolean compareFilters(XPathQueryNode xPathQueryNode, XPathEventNode xPathEventNode) {
        boolean success = true;
        for(XPathFilters xPathFilters: xPathQueryNode.getxPathFiltersList()) {

            success = success && matchTextNodes(xPathEventNode.getTextNodes(), xPathFilters.getMatchingValue(),
                    xPathFilters.getFilterType());
        }
        return success;
    }

    public boolean matchTextNodes(List<String> textNodes, String queryString, XPathFilters.FilterType filterType) {
        queryString = queryString.trim();
        queryString = StringEscapeUtils.unescapeJava(queryString);

        for(String s : textNodes) {
            if(filterType == XPathFilters.FilterType.EXACT_MATCH) {
                if(s.toLowerCase().equals(queryString.toLowerCase())) {
                    return true;
                }
            } else {
                if(s.toLowerCase().contains(queryString.toLowerCase())) {
                    return true;
                }
            }
        }
        return false;
    }

    public XPathPaperDocumentState getxPathPaperDocumentState() {
        return xPathPaperDocumentState;
    }

    public void setxPathPaperDocumentState(XPathPaperDocumentState xPathPaperDocumentState) {
        this.xPathPaperDocumentState = xPathPaperDocumentState;
    }

    public Map<String, String> getChannels() {
        return channels;
    }

    public void setChannels(Map<String, String> channels) {
        this.channels = channels;
    }

    public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {

        Map<String, String> channels = new HashMap<>();
        channels.put("q1", "/a//b/*/c[contains(text(),\"hello\")]");

        String data = "<a><b><x></x><d><c>hello</c></d></b></a>";

        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        XMLTraversorHandler handler = new XMLTraversorHandler();
        saxParser.parse(new InputSource(new StringReader(data)), handler);

//        XPathPaperDocumentEventMatcherHandler xPathDocumentHandler = new XPathPaperDocumentEventMatcherHandler("1", channels);
//
//
//        for(XPathEvent xPathEvent : handler.getxPathEventList()) {
//            List<String> matchingIDList = xPathDocumentHandler.handleEvent(xPathDocumentHandler.getxPathPaperDocumentState(), xPathEvent);
//            System.out.println("Step");
//            for(String s : matchingIDList) {
//                System.out.println(s);
//            }
//        }



    }





}
