package edu.upenn.cis.cis455.xpathengine.models;

import edu.upenn.cis.cis455.xpathengine.utils.XPathRecursiveParser;

import java.text.ParseException;
import java.util.List;

public class XPathQuery {

    private String queryId;
    private List<XPathQueryNode> xPathQueryNodeList;

    public static XPathQuery getXPathQuery(String queryId, String query) {

        try {
            List<Step> stepList = null;
            XPathRecursiveParser xPathRecursiveParser = new XPathRecursiveParser(query);
            stepList = xPathRecursiveParser.xPathExtractor();
            List<XPathQueryNode> xPathQueryNodeList = XPathQueryNode.getQueryNodesFromStepNode(stepList, query);
            XPathQuery xPathQuery = new XPathQuery();
            xPathQuery.queryId = queryId;
            xPathQuery.xPathQueryNodeList = xPathQueryNodeList;
            return xPathQuery;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static XPathQuery getXPathQueryV2(String queryId, String query) {

        try {
            List<Step> stepList = null;
            XPathRecursiveParser xPathRecursiveParser = new XPathRecursiveParser(query);
            stepList = xPathRecursiveParser.xPathExtractor();
            List<XPathQueryNode> xPathQueryNodeList = XPathQueryNode.getQueryNodesFromStepNodeV2(stepList, query);
            XPathQuery xPathQuery = new XPathQuery();
            xPathQuery.queryId = queryId;
            xPathQuery.xPathQueryNodeList = xPathQueryNodeList;
            return xPathQuery;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public List<XPathQueryNode> getxPathQueryNodeList() {
        return xPathQueryNodeList;
    }

    public void setxPathQueryNodeList(List<XPathQueryNode> xPathQueryNodeList) {
        this.xPathQueryNodeList = xPathQueryNodeList;
    }
}
