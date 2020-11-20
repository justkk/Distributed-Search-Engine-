package edu.upenn.cis.cis455.storage.managers;

import com.sleepycat.persist.*;
import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.model.UserChannelInfo;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;

import java.util.ArrayList;
import java.util.List;

public class UserChannelDataManager {

    private DataBaseConnectorConfig dataBaseConnectorConfig;
    private StoreConfig stConf;
    private EntityStore store;
    private PrimaryIndex<String, UserChannelInfo> primaryIndex;
    private SecondaryIndex<Integer, String, UserChannelInfo> userChannelInfoSecondaryIndex;


    public UserChannelDataManager(DataBaseConnectorConfig dataBaseConnectorConfig) {

        this.dataBaseConnectorConfig = dataBaseConnectorConfig;
        stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        store = new EntityStore(this.dataBaseConnectorConfig.getDatabaseEnvironment(),
                ConstantsHW2.getInstance().getUSER_CHANNEL_DATABASE(), stConf);

        primaryIndex = store.getPrimaryIndex(String.class, UserChannelInfo.class);
        userChannelInfoSecondaryIndex = store.getSecondaryIndex(primaryIndex, Integer.class, "userId");

    }

    public UserChannelInfo insert(UserChannelInfo userChannelInfo) {
        UserChannelInfo oldInfo = getUserChannelInfo(userChannelInfo.getChannelName());
        if (oldInfo != null) {
            return null;
        }
        primaryIndex.put(userChannelInfo);
        return userChannelInfo;
    }

    public UserChannelInfo getUserChannelInfo(String channelName) {
        return primaryIndex.get(channelName);
    }

    public List<UserChannelInfo> getUserChannelInfoList(Integer userId) {

        List<UserChannelInfo> userChannelInfoList = new ArrayList<>();
        EntityCursor<UserChannelInfo> sec_cursor =
                userChannelInfoSecondaryIndex.subIndex(userId).entities();
        try {
            for (UserChannelInfo userChannelInfo : sec_cursor) {
                userChannelInfoList.add(userChannelInfo);
            }
        } finally {
            sec_cursor.close();
        }
        return userChannelInfoList;
    }

    public List<UserChannelInfo> getAllChannels() {
        List<UserChannelInfo> userChannelInfoList = new ArrayList<>();
        EntityCursor<UserChannelInfo> cursor = primaryIndex.entities();
        try {
            for (UserChannelInfo userChannelInfo : cursor) {
                userChannelInfoList.add(userChannelInfo);
            }
        } finally {
            cursor.close();
        }
        return userChannelInfoList;
    }

    public void close() {
        if(store!=null) {
            store.close();
        }
    }


}
