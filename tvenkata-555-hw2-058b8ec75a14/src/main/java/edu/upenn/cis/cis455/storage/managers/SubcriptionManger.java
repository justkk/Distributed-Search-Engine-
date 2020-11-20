package edu.upenn.cis.cis455.storage.managers;

import com.sleepycat.persist.*;
import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.model.subscription.SubscriptionKey;
import edu.upenn.cis.cis455.model.subscription.SubscriptionValue;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;

import java.util.ArrayList;
import java.util.List;

public class SubcriptionManger {

    private DataBaseConnectorConfig dataBaseConnectorConfig;
    private StoreConfig stConf;
    private EntityStore store;
    private PrimaryIndex<SubscriptionKey, SubscriptionValue> primaryIndex;
    private SecondaryIndex<Integer, SubscriptionKey, SubscriptionValue> secondaryIndexUser;

    public SubcriptionManger(DataBaseConnectorConfig dataBaseConnectorConfig) {
        this.dataBaseConnectorConfig = dataBaseConnectorConfig;
        stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        store = new EntityStore(this.dataBaseConnectorConfig.getDatabaseEnvironment(),
                ConstantsHW2.getInstance().getSUBSCRIPTION_DATABASE(), stConf);
        primaryIndex = store.getPrimaryIndex(SubscriptionKey.class, SubscriptionValue.class);
        secondaryIndexUser = store.getSecondaryIndex(primaryIndex, Integer.class, "userId");
    }


    public SubscriptionValue insert(SubscriptionKey subscriptionKey) {
        SubscriptionValue subscriptionValue = new SubscriptionValue(subscriptionKey);
        primaryIndex.put(subscriptionValue);
        return subscriptionValue;
    }

    public List<SubscriptionValue> getSubList(Integer user) {

        EntityCursor<SubscriptionValue> entityCursor = secondaryIndexUser.subIndex(user).entities();
        List<SubscriptionValue> subscriptionValueList = new ArrayList<>();
        try {
            for (SubscriptionValue subscriptionValue : entityCursor) {
                subscriptionValueList.add(subscriptionValue);
            }
        } finally {
            entityCursor.close();
        }
        return subscriptionValueList;
    }


}
