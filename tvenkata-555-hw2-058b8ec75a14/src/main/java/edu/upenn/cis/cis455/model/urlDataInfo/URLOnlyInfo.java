package edu.upenn.cis.cis455.model.urlDataInfo;

import com.sleepycat.persist.model.*;

import java.util.Date;
import java.util.Map;

@Entity
public class URLOnlyInfo {

    @PrimaryKey
    private URLDataInfoKey urlDataInfoKey;
    @SecondaryKey(relate = Relationship.MANY_TO_ONE, name = "docId")
    private Integer docId;
    private Date lastModifiedTime;
    private Date createdTime;
    private String contentType;
    private Map<String, String> headers;
    private int statusCode;

    public URLOnlyInfo() {
    }

    public URLOnlyInfo(URLDataInfoKey urlDataInfoKey, Integer docId) {
        this.urlDataInfoKey = urlDataInfoKey;
        this.docId = docId;
    }

    public URLDataInfoKey getUrlDataInfoKey() {
        return urlDataInfoKey;
    }

    public void setUrlDataInfoKey(URLDataInfoKey urlDataInfoKey) {
        this.urlDataInfoKey = urlDataInfoKey;
    }

    public Integer getDocId() {
        return docId;
    }

    public void setDocId(Integer docId) {
        this.docId = docId;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }
}
