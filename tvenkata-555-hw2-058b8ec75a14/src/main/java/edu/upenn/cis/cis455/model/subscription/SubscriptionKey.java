package edu.upenn.cis.cis455.model.subscription;

import com.sleepycat.persist.model.KeyField;
import com.sleepycat.persist.model.Persistent;

@Persistent
public class SubscriptionKey {

    @KeyField(1) private Integer userId;
    @KeyField(2) private String channelName;

    public SubscriptionKey(Integer userId, String channelName) {
        this.userId = userId;
        this.channelName = channelName;
    }

    public SubscriptionKey() {
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
    }
}
