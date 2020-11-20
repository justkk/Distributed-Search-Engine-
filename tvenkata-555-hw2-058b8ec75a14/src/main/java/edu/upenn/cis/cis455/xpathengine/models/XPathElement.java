package edu.upenn.cis.cis455.xpathengine.models;

import java.util.List;

public class XPathElement {

    private String elementName;
    private List<XPathFilters> xPathFiltersList;
    private ElementType elementType;

    public XPathElement(String elementName, List<XPathFilters> xPathFiltersList) {
        this.elementName = elementName;
        this.xPathFiltersList = xPathFiltersList;
    }

    public XPathElement() {
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public List<XPathFilters> getxPathFiltersList() {
        return xPathFiltersList;
    }

    public void setxPathFiltersList(List<XPathFilters> xPathFiltersList) {
        this.xPathFiltersList = xPathFiltersList;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public void setElementType(ElementType elementType) {
        this.elementType = elementType;
    }

    public enum ElementType {
        WILD_CARD,
        WILD_TAIL,
        NORMAL;

        public static ElementType getElementTypeFromString(String name) {
            if (name.equals("*")) {
                return WILD_CARD;
            } else if (name.equals("")) {
                return WILD_TAIL;
            } else {
                return NORMAL;
            }
        }
    }
}
