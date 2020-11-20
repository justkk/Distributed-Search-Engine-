package edu.upenn.cis555.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBIndexHashKey;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTable;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;


@DynamoDBTable(tableName = "UrlOnlyInfoTest")
public class URLInfo {

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

    public URLInfo() {
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
    
    @Override
    public String toString() {
    	return String.join(",",keyString,protocol,host,""+port+"",filePath,requestMethod,docId,""+lastModifiedTime+"",""+createdTime+"",contentType,""+headers+"",""+statusCode+"",parentKeyString);
    }
}
