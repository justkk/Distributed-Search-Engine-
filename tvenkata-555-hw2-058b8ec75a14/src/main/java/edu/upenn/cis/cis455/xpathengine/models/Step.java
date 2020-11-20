package edu.upenn.cis.cis455.xpathengine.models;

import java.util.ArrayList;
import java.util.List;

public class Step {

    String nodeName;
    List<Test> testList;
    private boolean widTail;


    public Step() {
    }

    public Step getStepNodeWithOutFilters() {
        Step step = new Step();
        step.nodeName = nodeName;
        step.testList = new ArrayList<>();
        step.widTail = widTail;
        return step;
    }

    public Step(String nodeName, List<Test> testList, boolean widTail) {
        this.nodeName = nodeName;
        this.testList = testList;
        this.widTail = widTail;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public List<Test> getTestList() {
        return testList;
    }

    public void setTestList(List<Test> testList) {
        this.testList = testList;
    }

    public boolean isWidTail() {
        return widTail;
    }

    public void setWidTail(boolean widTail) {
        this.widTail = widTail;
    }
}
