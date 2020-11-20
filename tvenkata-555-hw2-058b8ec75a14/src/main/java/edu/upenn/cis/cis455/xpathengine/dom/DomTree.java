package edu.upenn.cis.cis455.xpathengine.dom;

import edu.upenn.cis.cis455.xpathengine.models.XPathEvent;
import edu.upenn.cis.cis455.xpathengine.models.XPathEventNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomTree {

    private DomElement rootElement;
    private Map<String, DomElement> domElementMap = new HashMap<>();

    public static DomTree buildTree(List<XPathEvent> xPathEventList) {
        DomTree domTree = new DomTree();
        for(XPathEvent xPathEvent : xPathEventList) {
            buildTree(domTree, xPathEvent);
        }
        return domTree;
    }

    public static void buildTree(DomTree tree, XPathEvent xPathEvent) {

        XPathEventNode xPathEventNode =  xPathEvent.getEventNode();

        if(tree.rootElement == null) {
            DomElement domElement = new DomElement();
            domElement.setId(xPathEventNode.getId());
            domElement.setParentPointer(null);
            domElement.setLevel(1);
            domElement.setTagName(xPathEventNode.getNodeName());
            domElement.setTextNodes(new ArrayList<>(xPathEventNode.getTextNodes()));
            tree.domElementMap.put(domElement.getId(), domElement);
            tree.rootElement = domElement;
            return;
        }

        if(xPathEvent.getEventType() == XPathEvent.EventType.START) {

            if(tree.domElementMap.containsKey(xPathEventNode.getId())) {
                DomElement domElement = tree.domElementMap.get(xPathEventNode.getId());
                domElement.getTextNodes().addAll(xPathEventNode.getTextNodes());
                return;
            }

            DomElement parentElement = tree.domElementMap.get(xPathEventNode.getParentNode().getId());
            DomElement domElement = new DomElement();
            domElement.setId(xPathEventNode.getId());
            domElement.setParentPointer(parentElement);
            domElement.setTagName(xPathEventNode.getNodeName());
            domElement.setLevel(parentElement.getLevel() + 1);
            domElement.setTextNodes(new ArrayList<>(xPathEventNode.getTextNodes()));
            tree.domElementMap.put(domElement.getId(), domElement);
            parentElement.getChildPointers().add(domElement);


        } else if(xPathEvent.getEventType() == XPathEvent.EventType.END) {
            // do nothing.
        }
    }

    public DomElement getRootElement() {
        return rootElement;
    }

    public void setRootElement(DomElement rootElement) {
        this.rootElement = rootElement;
    }

    public Map<String, DomElement> getDomElementMap() {
        return domElementMap;
    }

    public void setDomElementMap(Map<String, DomElement> domElementMap) {
        this.domElementMap = domElementMap;
    }
}
