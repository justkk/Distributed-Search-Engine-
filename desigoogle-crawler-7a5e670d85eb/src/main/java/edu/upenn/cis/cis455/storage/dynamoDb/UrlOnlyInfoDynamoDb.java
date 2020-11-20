package edu.upenn.cis.cis455.storage.dynamoDb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfoKey;
import edu.upenn.cis.cis455.model.urlDataInfo.URLOnlyInfo;

import java.util.Date;
import java.util.Map;

@DynamoDBTable(tableName = "UrlOnlyInfoTest")
public class UrlOnlyInfoDynamoDb {

    @DynamoDBHashKey
    private String keyString;

    private String protocol;
    private String host;
    private int port;
    private String filePath;
    private String requestMethod;

    @DynamoDBIndexHashKey(globalSecondaryIndexName = "docIndex")
    private String docId;
    private Date lastModifiedTime;
    private Date createdTime;
    private String contentType;
    private Map<String, String> headers;
    private int statusCode;

    private String parentKeyString;

    public UrlOnlyInfoDynamoDb() {
    }

    public UrlOnlyInfoDynamoDb(URLOnlyInfo urlOnlyInfo) {

        this.protocol = urlOnlyInfo.getUrlDataInfoKey().getProtocol();
        this.host = urlOnlyInfo.getUrlDataInfoKey().getHost();
        this.port = urlOnlyInfo.getUrlDataInfoKey().getPort();
        this.filePath = urlOnlyInfo.getUrlDataInfoKey().getFilePath();
        this.requestMethod = urlOnlyInfo.getUrlDataInfoKey().getRequestMethod();
        this.keyString = requestMethod + ":" + protocol + "://" + host + ":" + port + filePath;
        this.docId = urlOnlyInfo.getDocId();
        this.lastModifiedTime = urlOnlyInfo.getLastModifiedTime();
        this.createdTime = urlOnlyInfo.getCreatedTime();
        this.contentType = urlOnlyInfo.getContentType();
        this.headers = urlOnlyInfo.getHeaders();
        this.statusCode = urlOnlyInfo.getStatusCode();
        this.parentKeyString = urlOnlyInfo.getParentKeyString();
    }


    public String getKeyString() {
        return keyString;
    }

    public void setKeyString(String keyString) {
        this.keyString = keyString;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
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

    public String getParentKeyString() {
        return parentKeyString;
    }

    public void setParentKeyString(String parentKeyString) {
        this.parentKeyString = parentKeyString;
    }
}
