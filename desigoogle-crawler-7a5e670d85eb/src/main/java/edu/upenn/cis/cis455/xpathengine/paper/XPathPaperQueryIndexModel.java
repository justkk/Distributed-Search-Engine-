package edu.upenn.cis.cis455.xpathengine.paper;

import edu.upenn.cis.cis455.xpathengine.models.XPathQueryNode;

import java.util.ArrayList;
import java.util.List;

public class XPathPaperQueryIndexModel {

    private List<XPathQueryNode> candidateList;
    private List<XPathQueryNode> waitList;

    public XPathPaperQueryIndexModel() {
        candidateList = new ArrayList<>();
        waitList = new ArrayList<>();
    }

    public List<XPathQueryNode> getCandidateList() {
        return candidateList;
    }

    public void setCandidateList(List<XPathQueryNode> candidateList) {
        this.candidateList = candidateList;
    }

    public List<XPathQueryNode> getWaitList() {
        return waitList;
    }

    public void setWaitList(List<XPathQueryNode> waitList) {
        this.waitList = waitList;
    }
}
