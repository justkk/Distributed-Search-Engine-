package edu.upenn.cis.cis455.model.representationModels;

import java.util.Date;

public class RemoteFileStructure {

    private String host;
    private String location;

    private Date lastModified;
    private Date lastFetched;

    private String url;

    private long dataSize; //in bytes

    private String data;


    public RemoteFileStructure(String host, String location, Date lastModified, Date lastFetched, String url,
                               long dataSize, String data) {
        this.host = host;
        this.location = location;
        this.lastModified = lastModified;
        this.lastFetched = lastFetched;
        this.url = url;
        this.dataSize = dataSize;
        this.data = data;
    }


    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public Date getLastFetched() {
        return lastFetched;
    }

    public void setLastFetched(Date lastFetched) {
        this.lastFetched = lastFetched;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getDataSize() {
        return dataSize;
    }

    public void setDataSize(long dataSize) {
        this.dataSize = dataSize;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
