package edu.upenn.cis.cis455.model.hostRobotInfo;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.StoreConfig;
import com.sleepycat.persist.model.*;

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Entity
public class HostRobotInfo implements Serializable {

    @SecondaryKey(relate = Relationship.ONE_TO_ONE)
    private HostRobotInfoKey hostRobotInfoKey;
    @PrimaryKey(sequence = "HostRobotInfoID")
    private Long id;
    private String robotStructure;
    private Date lastUpdatedDate;
    private Date creationDate;
    private String robotFilePath;
    private Map<String, String> additionalInfo = new HashMap<>();
    private String protocol;
    private String host;
    private int port;

    public HostRobotInfo() {
    }


    public HostRobotInfo(String protocol, String host, int port) {
        this.hostRobotInfoKey = new HostRobotInfoKey(protocol, host, port);
        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }


    public String getRobotStructure() {
        return robotStructure;
    }

    public void setRobotStructure(String robotStructure) {
        this.robotStructure = robotStructure;
    }

    public Date getLastUpdatedDate() {
        return lastUpdatedDate;
    }

    public void setLastUpdatedDate(Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public Map<String, String> getAdditionalInfo() {
        return additionalInfo;
    }

    public HostRobotInfoKey getHostRobotInfoKey() {
        return hostRobotInfoKey;
    }


    public String getRobotFilePath() {
        return robotFilePath;
    }

    public void setRobotFilePath(String robotFilePath) {
        this.robotFilePath = robotFilePath;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public static void main(String[] args) throws MalformedURLException {
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setAllowCreate(true);
        Environment dbEnv = new Environment(new File("/Users/nikhilt/Desktop/playground"),
                envConfig);

        StoreConfig stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        EntityStore store = new EntityStore(dbEnv, "DPLSample", stConf);
        PrimaryIndex<HostRobotInfoKey, HostRobotInfo> userIndex;
        userIndex = store.getPrimaryIndex(HostRobotInfoKey.class, HostRobotInfo.class);
        HostRobotInfo inserttion = new HostRobotInfo("http","www.google.com", 80);
        inserttion.getAdditionalInfo().put("Name", "nikhil");
        userIndex.putNoReturn(inserttion);
        HostRobotInfo hostRobotInfo = userIndex.get(new HostRobotInfoKey("http", "www.google.com", 80));
        //System.out.println(hostRobotInfo.getAdditionalInfo());
    }

    public void setHostRobotInfoKey(HostRobotInfoKey hostRobotInfoKey) {
        this.hostRobotInfoKey = hostRobotInfoKey;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setAdditionalInfo(Map<String, String> additionalInfo) {
        this.additionalInfo = additionalInfo;
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
}
