package edu.upenn.cis.cis455.xpathengine.models;

import edu.upenn.cis.cis455.xpathengine.utils.XPathRecursiveParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XPathQueryNode {

    private String queryId;
    private String nodeName;
    private int position;
    private int relativePosition;
    private int level;
    private Map<String, XPathQueryNode> nodeMap = new HashMap<>();
    private XPathQueryNode defaultPointer;
    private List<XPathFilters> xPathFiltersList = new ArrayList<>();

    public XPathQueryNode() {
    }

    public XPathQueryNode(String queryId, int position, int relativePosition, int level) {
        this.queryId = queryId;
        this.position = position;
        this.relativePosition = relativePosition;
        this.level = level;
    }

    public String getQueryId() {
        return queryId;
    }

    public void setQueryId(String queryId) {
        this.queryId = queryId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getRelativePosition() {
        return relativePosition;
    }

    public void setRelativePosition(int relativePosition) {
        this.relativePosition = relativePosition;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Map<String, XPathQueryNode> getNodeMap() {
        return nodeMap;
    }

    public void setNodeMap(Map<String, XPathQueryNode> nodeMap) {
        this.nodeMap = nodeMap;
    }

    public XPathQueryNode getDefaultPointer() {
        return defaultPointer;
    }

    public void setDefaultPointer(XPathQueryNode defaultPointer) {
        this.defaultPointer = defaultPointer;
    }
    
    public boolean isWildCard() {
        return "*".equals(nodeName);
    }

    public boolean isCurrent() {
        return ".".equals(nodeName);
    }

    public boolean isParent() {
        return "..".equals(nodeName);
    }


    public static List<XPathQuery> getQueryFromStepNode(List<Step> stepNodes, String queryId) {

        List<Step> stepListState = new ArrayList<>();
        List<List<Step>> listOfLists = new ArrayList<>();
        List<XPathQuery> xPathQueries = new ArrayList<>();
        for(Step step : stepNodes) {
            if(step.getTestList().size() == 0) {
                stepListState.add(step);
                continue;
            }

            for(Test test : step.getTestList()) {
                Step stepi  = step.getStepNodeWithOutFilters();
                stepi.testList.add(test);
                List<Step> newList = new ArrayList<>(stepListState);
                newList.add(stepi);
                listOfLists.add(newList);
            }

            Step defaultStep = step.getStepNodeWithOutFilters();
            stepListState.add(defaultStep);
        }
        listOfLists.add(stepListState);
        int index = 0;
        for (List<Step> stepList : listOfLists) {
            XPathQuery xPathQuery = new XPathQuery();
            xPathQuery.setQueryId(queryId + "_" + String.valueOf(index));
            xPathQuery.setxPathQueryNodeList(getQueryNodesFromStepNode(stepList, xPathQuery.getQueryId()));
            xPathQueries.add(xPathQuery);
            index += 1;
        }
        return xPathQueries;
    }

    public static List<XPathQueryNode> getQueryNodesFromStepNode(List<Step> stepNodes, String queryId) {

        int queryNodeIndex = 1;


        List<XPathQueryNode> xPathQueryNodeList = new ArrayList<>();
        int relativePositionPendingCount = 0;

        for (Step step : stepNodes) {

            if(step.getNodeName().equals("*")) {
                relativePositionPendingCount += 1;
                continue;
            }

            XPathQueryNode xPathQueryNode = new XPathQueryNode();
            xPathQueryNode.setPosition(queryNodeIndex);
            xPathQueryNode.setNodeName(step.getNodeName());
            xPathQueryNode.setQueryId(queryId);
            xPathQueryNode.addFilters(step);

            if (!step.isWidTail()) {
                if (queryNodeIndex == 1) {
                    xPathQueryNode.setRelativePosition(0);
                    xPathQueryNode.setLevel(1 + relativePositionPendingCount);
                } else {
                   xPathQueryNode.setRelativePosition(1 + relativePositionPendingCount);
                   xPathQueryNode.setLevel(0);
                }

            } else {
                xPathQueryNode.setRelativePosition(-1);
                xPathQueryNode.setLevel(-1);
            }

            xPathQueryNodeList.add(xPathQueryNode);
            queryNodeIndex += 1;
            relativePositionPendingCount = 0;
        }

        return xPathQueryNodeList;
    }


    public static List<XPathQueryNode> getQueryNodesFromStepNodeV2(List<Step> stepNodes, String queryId) {

        int queryNodeIndex = 1;


        List<XPathQueryNode> xPathQueryNodeList = new ArrayList<>();
        int relativePositionPendingCount = 0;

        for (Step step : stepNodes) {

            XPathQueryNode xPathQueryNode = new XPathQueryNode();
            xPathQueryNode.setPosition(queryNodeIndex);
            xPathQueryNode.setNodeName(step.getNodeName());
            xPathQueryNode.setQueryId(queryId);
            xPathQueryNode.addFilters(step);

            if (!step.isWidTail()) {
                if (queryNodeIndex == 1) {
                    xPathQueryNode.setRelativePosition(0);
                    xPathQueryNode.setLevel(1 + relativePositionPendingCount);
                } else {
                    xPathQueryNode.setRelativePosition(1 + relativePositionPendingCount);
                    xPathQueryNode.setLevel(0);
                }

            } else {
                xPathQueryNode.setRelativePosition(-1);
                xPathQueryNode.setLevel(-1);
            }

            xPathQueryNodeList.add(xPathQueryNode);
            queryNodeIndex += 1;
            relativePositionPendingCount = 0;
        }

        return xPathQueryNodeList;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public void addFilters(Step step) {
        
        for(Test test : step.getTestList()) {
            XPathFilters xPathFilter = new XPathFilters(XPathFilters.getFilterType(test.getTestType()), test.getQuotedString());
            xPathFiltersList.add(xPathFilter);
        }
    }



    public List<XPathFilters> getxPathFiltersList() {
        return xPathFiltersList;
    }

    public void setxPathFiltersList(List<XPathFilters> xPathFiltersList) {
        this.xPathFiltersList = xPathFiltersList;
    }

    public XPathQueryNode copy() {
        XPathQueryNode xPathQueryNode = new XPathQueryNode();
        xPathQueryNode.queryId = queryId;
        xPathQueryNode.nodeName = nodeName;
        xPathQueryNode.position = position;
        xPathQueryNode.relativePosition = relativePosition;
        xPathQueryNode.level = level;
        xPathQueryNode.nodeMap = new HashMap<>();
        xPathQueryNode.defaultPointer =  defaultPointer;
        xPathQueryNode.xPathFiltersList = xPathFiltersList;
        return xPathQueryNode;
    }
}
