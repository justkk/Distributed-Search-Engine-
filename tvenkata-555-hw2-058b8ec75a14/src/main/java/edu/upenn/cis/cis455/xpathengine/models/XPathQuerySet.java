package edu.upenn.cis.cis455.xpathengine.models;

import edu.upenn.cis.cis455.xpathengine.utils.XPathRecursiveParser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class XPathQuerySet {

    private String queryId;
    private List<XPathQuery> xPathQueryList;

    public static XPathQuerySet getXPathQuery(String queryId, String query) {

        try {
            List<Step> stepList = null;
            XPathRecursiveParser xPathRecursiveParser = new XPathRecursiveParser(query);
            stepList = xPathRecursiveParser.xPathExtractor();
            List<XPathQuery> xPathQueryList = XPathQueryNode.getQueryFromStepNode(stepList, queryId);
            XPathQuerySet xPathQuery = new XPathQuerySet();
            xPathQuery.queryId = queryId;
            xPathQuery.xPathQueryList = xPathQueryList;
            return xPathQuery;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getQueryId() {
        return queryId;
    }

    public List<XPathQuery> getxPathQueryList() {
        return xPathQueryList;
    }

    public void setxPathQueryList(List<XPathQuery> xPathQueryList) {
        this.xPathQueryList = xPathQueryList;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public static void main(String[] args) {
        XPathQuerySet xPathQuerySet = XPathQuerySet.getXPathQuery("1", "a[text()=\"dfd\"]/b[text()=\"dfd\"]/c[text()=\"dfd\"]");
        System.out.println(xPathQuerySet);
    }



}
