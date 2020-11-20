package edu.upenn.cis.cis455.xpathengine.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class XPathEventNode {

    private String id = UUID.randomUUID().toString();

    private List<String> textNodes = new ArrayList<>();

    private String nodeName;

    private List<XPathEventNode> path;

    private XPathEventNode parentNode;

    private int level;

    public XPathEventNode() {
    }

    public XPathEventNode(String nodeName, int level) {
        this.nodeName = nodeName;
        this.level = level;
    }

    public void addTextNode(String textNode) {
        this.textNodes.add(textNode);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getTextNodes() {
        return textNodes;
    }

    public void setTextNodes(List<String> textNodes) {
        this.textNodes = textNodes;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public List<XPathEventNode> getPath() {
        return path;
    }

    public void setPath(List<XPathEventNode> path) {
        this.path = path;
    }

    public XPathEventNode getParentNode() {
        return parentNode;
    }

    public void setParentNode(XPathEventNode parentNode) {
        this.parentNode = parentNode;
    }

    public XPathEventNode copy() {

        XPathEventNode xPathEventNode = new XPathEventNode();
        xPathEventNode.id = id;
        xPathEventNode.nodeName = nodeName;
        xPathEventNode.path =path;
        xPathEventNode.level = level;
        xPathEventNode.parentNode = parentNode;
        return xPathEventNode;
    }
}
