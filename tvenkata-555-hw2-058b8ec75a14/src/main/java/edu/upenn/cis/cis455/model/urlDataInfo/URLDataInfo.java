package edu.upenn.cis.cis455.model.urlDataInfo;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;
import edu.upenn.cis.cis455.utils.Md5HashGenerator;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

@Entity
public class URLDataInfo implements Serializable {

    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private URLDataInfoKey urlDataInfoKey;
    @PrimaryKey(sequence = "URLDataInfoID")
    private Integer id;
    private Date lastModifiedTime;
    private Date createdTime;
    private int contentLength;
    private String contentType;
    private String data;
    private Map<String, String> headers;
    private int statusCode;
    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private String md5Hash;

    public URLDataInfo() {
    }

    public URLDataInfo(URLDataInfoKey urlDataInfoKey) {
        this.urlDataInfoKey = urlDataInfoKey;
    }

    public URLDataInfoKey getUrlDataInfoKey() {
        return urlDataInfoKey;
    }

    public void setUrlDataInfoKey(URLDataInfoKey urlDataInfoKey) {
        this.urlDataInfoKey = urlDataInfoKey;
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
        this.md5Hash = Md5HashGenerator.getHash(data);
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMd5Hash() {
        return md5Hash;
    }



    public URLDataInfo(DocOnlyInfo docOnlyInfo, URLOnlyInfo urlOnlyInfo) {

        urlDataInfoKey = urlOnlyInfo.getUrlDataInfoKey();
        id = docOnlyInfo.getId();
        lastModifiedTime = urlOnlyInfo.getLastModifiedTime();
        createdTime = urlOnlyInfo.getCreatedTime();
        contentLength = docOnlyInfo.getContentLength();
        contentType = urlOnlyInfo.getContentType();
        data = docOnlyInfo.getContent();
        headers = urlOnlyInfo.getHeaders();
        statusCode = urlOnlyInfo.getStatusCode();
        md5Hash = docOnlyInfo.getHash();
    }

    public DocOnlyInfo getDocOnlyInfo() {

        if(id == null) {
            return new DocOnlyInfo(data, contentLength);
        }

        return new DocOnlyInfo(id, data, contentLength);
    }

    public URLOnlyInfo getURLOnlyInfo() {

        URLOnlyInfo urlOnlyInfo = new URLOnlyInfo(urlDataInfoKey, id);
        urlOnlyInfo.setContentType(contentType);
        urlOnlyInfo.setCreatedTime(createdTime);
        urlOnlyInfo.setDocId(id);
        urlOnlyInfo.setHeaders(headers);
        urlOnlyInfo.setLastModifiedTime(lastModifiedTime);
        urlOnlyInfo.setStatusCode(statusCode);
        return urlOnlyInfo;
    }


}
