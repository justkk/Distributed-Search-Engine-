package edu.upenn.cis.cis455.xpathengine.models;

public class XPathEvent {

    private EventType eventType;
    private XPathEventNode eventNode;

    public XPathEvent(EventType eventType, XPathEventNode eventNode) {
        this.eventType = eventType;
        this.eventNode = eventNode;
    }

    public enum EventType {
        START,
        END,
        CHARS;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public XPathEventNode getEventNode() {
        return eventNode;
    }

    public void setEventNode(XPathEventNode eventNode) {
        this.eventNode = eventNode;
    }
}
