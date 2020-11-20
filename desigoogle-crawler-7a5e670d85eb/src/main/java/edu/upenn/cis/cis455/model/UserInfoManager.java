package edu.upenn.cis.cis455.model;

import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;

public class UserInfoManager {

    private DataBaseConnectorConfig dataBaseConnectorConfig;
    private StoreConfig stConf;
    private EntityStore store;
    private PrimaryIndex<Integer, User> primaryIndex;
    private SecondaryIndex<String, Integer, User> secondaryIndex;

    public UserInfoManager(DataBaseConnectorConfig dataBaseConnectorConfig) {
        this.dataBaseConnectorConfig = dataBaseConnectorConfig;
        stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        store = new EntityStore(this.dataBaseConnectorConfig.getDatabaseEnvironment(),
                ConstantsHW2.getInstance().getHOST_ROBOT_DATABASE(), stConf);
        primaryIndex = store.getPrimaryIndex(Integer.class, User.class);
        secondaryIndex = store.getSecondaryIndex(primaryIndex, String.class, "userName");
    }


    public User getUserFromId(int id) {
        return primaryIndex.get(id);
    }

    public User getUserFromUserName(String username) {
        return secondaryIndex.get(username);
    }

//    public User insertUser

    public User insertUser(User user) {
        primaryIndex.put(user);
        return user;
    }

    public void close() {
        if(store!=null) {
            store.close();
        }
    }
}
