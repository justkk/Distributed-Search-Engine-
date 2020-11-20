package edu.upenn.cis.cis455.xpathengine.dom;

import java.util.ArrayList;
import java.util.List;

public class DomElement {

    private String id = "";
    private String tagName = "";
    private DomElement parentPointer;
    private List<String> textNodes = new ArrayList<>();
    private List<DomElement> childPointers = new ArrayList<>();
    private int level;

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DomElement getParentPointer() {
        return parentPointer;
    }

    public void setParentPointer(DomElement parentPointer) {
        this.parentPointer = parentPointer;
    }

    public List<String> getTextNodes() {
        return textNodes;
    }

    public void setTextNodes(List<String> textNodes) {
        this.textNodes = textNodes;
    }

    public List<DomElement> getChildPointers() {
        return childPointers;
    }

    public void setChildPointers(List<DomElement> childPointers) {
        this.childPointers = childPointers;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean equals(Object domElement) {

        if(domElement instanceof DomElement) {
            if(((DomElement) domElement).id == null || ((DomElement) domElement).tagName == null) {
                return false;
            }

            if(((DomElement) domElement).id.equals(id) && ((DomElement) domElement).tagName.equals(tagName)) {
                return true;
            }
        }
        return false;

    }

    public int hashCode() {
        String identifier =  id + "/" + tagName;
        return identifier.hashCode();
    }
}
