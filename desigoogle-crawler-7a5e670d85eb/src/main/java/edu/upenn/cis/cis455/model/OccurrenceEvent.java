package edu.upenn.cis.cis455.model;

import edu.upenn.cis.cis455.xpathengine.models.XPathEvent;

/**
 * TODO: this class encapsulates the data from a keyword "occurrence"
 */
public class OccurrenceEvent {

    // just using it as wrapper;
    private XPathEvent xPathEvent;
    private boolean cleanup = false;
    private boolean newDocument = false;

    public OccurrenceEvent(XPathEvent xPathEvent) {
        this.xPathEvent = xPathEvent;
    }

    public XPathEvent getxPathEvent() {
        return xPathEvent;
    }

    public void setxPathEvent(XPathEvent xPathEvent) {
        this.xPathEvent = xPathEvent;
    }

    public boolean isCleanup() {
        return cleanup;
    }

    public void setCleanup(boolean cleanup) {
        this.cleanup = cleanup;
    }

    public boolean isNewDocument() {
        return newDocument;
    }

    public void setNewDocument(boolean newDocument) {
        this.newDocument = newDocument;
    }
}
