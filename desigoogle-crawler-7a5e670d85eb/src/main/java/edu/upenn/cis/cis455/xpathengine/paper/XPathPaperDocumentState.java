package edu.upenn.cis.cis455.xpathengine.paper;

import edu.upenn.cis.cis455.xpathengine.models.XPathQuery;
import edu.upenn.cis.cis455.xpathengine.models.XPathQueryNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class XPathPaperDocumentState {

    private String documentId;
    private Map<String, XPathPaperQueryIndexModel> queryIndex = new HashMap<>();

    public static XPathPaperDocumentState getXPathDocumentState(String documentId, List<XPathQuery> xPathQueryList) {

        XPathPaperDocumentState xPathPaperDocumentState = new XPathPaperDocumentState();
        xPathPaperDocumentState.documentId = documentId;

        for(XPathQuery xPathQuery : xPathQueryList) {

            int index = 0;
            XPathQueryNode prev = null;
            for(XPathQueryNode xPathQueryNode : xPathQuery.getxPathQueryNodeList()) {

                String nodeName = xPathQueryNode.getNodeName();
                if(!xPathPaperDocumentState.queryIndex.containsKey(nodeName)) {
                    xPathPaperDocumentState.queryIndex.put(nodeName, new XPathPaperQueryIndexModel());
                }

                if(prev!=null) {
                    prev.setDefaultPointer(xPathQueryNode);
                }

                if(index == 0) {
                    xPathPaperDocumentState.queryIndex.get(nodeName).getCandidateList().add(xPathQueryNode);

                } else {
                    xPathPaperDocumentState.queryIndex.get(nodeName).getWaitList().add(xPathQueryNode);
                }
                index += 1;
                prev  = xPathQueryNode;
            }

        }

        return xPathPaperDocumentState;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Map<String, XPathPaperQueryIndexModel> getQueryIndex() {
        return queryIndex;
    }

    public void setQueryIndex(Map<String, XPathPaperQueryIndexModel> queryIndex) {
        this.queryIndex = queryIndex;
    }
}
