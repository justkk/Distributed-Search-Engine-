package edu.upenn.cis.cis455.model.representationModels;

import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfo;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class URLMetaInformation {

    private URLInfo urlInfo;
    private Date lastModifiedTime;
    private int contentLength;
    private String contentType;
    private String data;
    private String requestMethod;
    private Map<String, String> headers;
    private Map<String, List<String>> responseHeaders;
    private int statusCode;

    public URLMetaInformation(URLInfo urlInfo, URLResponse urlResponse, String requestMethod,
                              Map<String, String> requestHeaders) {
        this.urlInfo = urlInfo;
        this.lastModifiedTime = new Date(urlResponse.getLastModifiedTime());
        this.contentLength = urlResponse.getContentLength();
        this.contentType = urlResponse.getContentType();
        this.data = urlResponse.getData();
        this.requestMethod = requestMethod;
        this.headers = headers;
        this.statusCode = urlResponse.getStatusCode();
        this.responseHeaders = urlResponse.getResponseHeaders();
    }

    public URLMetaInformation(URLInfo urlInfo, URLDataInfo urlDataInfo) {
        this.urlInfo = urlInfo;
        this.lastModifiedTime = urlDataInfo.getLastModifiedTime();
        this.contentLength = urlDataInfo.getContentLength();
        this.contentType = urlDataInfo.getContentType();
        this.data = urlDataInfo.getData();
        this.requestMethod = urlDataInfo.getUrlDataInfoKey().getRequestMethod();
        this.headers = urlDataInfo.getHeaders();
        this.statusCode = urlDataInfo.getStatusCode();
    }

    public URLInfo getUrlInfo() {
        return urlInfo;
    }

    public void setUrlInfo(URLInfo urlInfo) {
        this.urlInfo = urlInfo;
    }

    public Date getLastModifiedTime() {
        return lastModifiedTime;
    }

    public void setLastModifiedTime(Date lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }

    public int getContentLength() {
        return contentLength;
    }

    public void setContentLength(int contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
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

    public Map<String, List<String>> getResponseHeaders() {
        return responseHeaders;
    }
}
