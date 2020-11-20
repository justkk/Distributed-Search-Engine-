package edu.upenn.cis.cis455.model.representationModels;

import edu.upenn.cis.cis455.crawler.info.URLInfo;

import java.util.ArrayList;
import java.util.List;

public class DocInfo {

    private URLMetaInformation urlMetaInformation;
    private List<URLInfo> links = new ArrayList<>();

    public DocInfo(URLMetaInformation urlMetaInformation) {
        this.urlMetaInformation = urlMetaInformation;
    }

    public URLMetaInformation getUrlMetaInformation() {
        return urlMetaInformation;
    }

    public void setUrlMetaInformation(URLMetaInformation urlMetaInformation) {
        this.urlMetaInformation = urlMetaInformation;
    }

    public List<URLInfo> getLinks() {
        return links;
    }

    public void setLinks(List<URLInfo> links) {
        this.links = links;
    }
}
