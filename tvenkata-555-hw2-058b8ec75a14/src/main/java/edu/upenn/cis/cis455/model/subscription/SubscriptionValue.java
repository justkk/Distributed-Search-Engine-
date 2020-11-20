package edu.upenn.cis.cis455.model.subscription;

import com.sleepycat.persist.model.*;
import edu.upenn.cis.cis455.model.User;

@Entity
public class SubscriptionValue {

    @PrimaryKey
    private SubscriptionKey subscriptionKey;

    @SecondaryKey(relate = Relationship.MANY_TO_ONE, relatedEntity = User.class, onRelatedEntityDelete = DeleteAction.CASCADE)
    private Integer userId;

    private String channelName;

    public SubscriptionValue() {
    }

    public SubscriptionValue(SubscriptionKey subscriptionKey) {
        this.subscriptionKey = subscriptionKey;
        this.userId = subscriptionKey.getUserId();
        this.channelName = subscriptionKey.getChannelName();
    }

    public SubscriptionKey getSubscriptionKey() {
        return subscriptionKey;
    }

    public Integer getUserId() {
        return userId;
    }

    public String getChannelName() {
        return channelName;
    }
}
