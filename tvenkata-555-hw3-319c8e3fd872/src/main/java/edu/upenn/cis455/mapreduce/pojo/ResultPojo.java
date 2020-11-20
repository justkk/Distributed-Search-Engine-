package edu.upenn.cis455.mapreduce.pojo;

public class ResultPojo {

    private String key;
    private String value;
    private String execId;

    public ResultPojo(String key, String value, String execId) {
        this.key = key;
        this.value = value;
        this.execId = execId;
    }

    public ResultPojo() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getExecId() {
        return execId;
    }

    public void setExecId(String execId) {
        this.execId = execId;
    }

}
