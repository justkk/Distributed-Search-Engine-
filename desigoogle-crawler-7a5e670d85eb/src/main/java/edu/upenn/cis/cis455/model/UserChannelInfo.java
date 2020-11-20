package edu.upenn.cis.cis455.model;

import com.sleepycat.persist.model.Entity;
import com.sleepycat.persist.model.PrimaryKey;
import com.sleepycat.persist.model.Relationship;
import com.sleepycat.persist.model.SecondaryKey;

@Entity
public class UserChannelInfo {

    @SecondaryKey(relate = Relationship.MANY_TO_ONE)
    private int userId;
    @PrimaryKey
    private String channelName;
    private String xPath;

    public UserChannelInfo(int userId, String channelName, String xPath) {
        this.userId = userId;
        this.channelName = channelName;
        this.xPath = xPath;
    }

    public UserChannelInfo() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }

    public String getxPath() {
        return xPath;
    }

    public void setxPath(String xPath) {
        this.xPath = xPath;
    }
}
