package edu.upenn.cis.cis455.storage.managers;

import com.sleepycat.je.LockMode;
import com.sleepycat.je.Sequence;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.EntityStore;
import com.sleepycat.persist.PrimaryIndex;
import com.sleepycat.persist.SecondaryIndex;
import com.sleepycat.persist.StoreConfig;
import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.model.hostRobotInfo.HostRobotInfo;
import edu.upenn.cis.cis455.model.hostRobotInfo.HostRobotInfoKey;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;

import java.util.Date;

public class HostRobotInfoManager {

    private DataBaseConnectorConfig dataBaseConnectorConfig;
    private StoreConfig stConf;
    private EntityStore store;
    private PrimaryIndex<Long, HostRobotInfo> primaryIndex;
    private SecondaryIndex<HostRobotInfoKey, Long, HostRobotInfo> secondaryIndex;
    private Sequence primaryIdSequence;

    public HostRobotInfoManager(DataBaseConnectorConfig dataBaseConnectorConfig) {
        this.dataBaseConnectorConfig = dataBaseConnectorConfig;
        stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        store = new EntityStore(this.dataBaseConnectorConfig.getDatabaseEnvironment(),
                ConstantsHW2.getInstance().getHOST_ROBOT_DATABASE(), stConf);
        primaryIdSequence = store.getSequence("primaryIdSequence");
        primaryIndex = store.getPrimaryIndex(Long.class, HostRobotInfo.class);
        secondaryIndex = store.getSecondaryIndex(primaryIndex, HostRobotInfoKey.class, "hostRobotInfoKey");
    }

    public HostRobotInfo getHostRobotInfo(HostRobotInfoKey hostRobotInfoKey, Transaction txn) {
        return secondaryIndex.get(txn, hostRobotInfoKey, LockMode.READ_COMMITTED);
    }

    public HostRobotInfo insertHostRobotInfo(HostRobotInfo hostRobotInfo, Transaction txn) {


        HostRobotInfo alreadyPresent = getHostRobotInfo(hostRobotInfo.getHostRobotInfoKey(), txn);

        if (alreadyPresent == null) {
            hostRobotInfo.setCreationDate(new Date());
//            hostRobotInfo.setId(primaryIdSequence.get(txn, 1));
        } else {
            hostRobotInfo.setId(alreadyPresent.getId());
        }
        hostRobotInfo.setLastUpdatedDate(new Date());
        primaryIndex.put(txn, hostRobotInfo);
        return hostRobotInfo;
    }

    public HostRobotInfo updateHostRobotInfo(HostRobotInfo hostRobotInfo) {
        hostRobotInfo.setLastUpdatedDate(new Date());
        primaryIndex.put(hostRobotInfo);
        return hostRobotInfo;
    }

    public void deleteHostRobotInfo(HostRobotInfo hostRobotInfo) {
        primaryIndex.delete(hostRobotInfo.getId());
        secondaryIndex.delete(hostRobotInfo.getHostRobotInfoKey());
    }

    public void close() {
        if(store!=null) {
            store.close();
        }
    }

    public int getSize() {
        int count = (int) primaryIndex.count();
        return count;
    }

    public static void main(String[] args) {
//        DataBaseConnectorConfig dataBaseConnectorConfig = new DataBaseConnectorConfig("/Users/nikhilt/Desktop/playground");
//        HostRobotInfoManager hostRobotInfoManager = new HostRobotInfoManager(dataBaseConnectorConfig);
//        HostRobotInfo hostRobotInfo1 = new HostRobotInfo("http1", "www.google.com", 80);
//        HostRobotInfo hostRobotInfo2 = new HostRobotInfo("http2", "www.google.com", 80);
//        HostRobotInfo hostRobotInfo3 = new HostRobotInfo("http3", "www.google.com", 80);
//
//        hostRobotInfo1.setRobotFilePath("/temp1");
//        hostRobotInfo2.setRobotFilePath("/temp2");
//        hostRobotInfo3.setRobotFilePath("/temp3");
//
//        System.out.println(hostRobotInfoManager.insertHostRobotInfo(hostRobotInfo1).getId());
//        System.out.println(hostRobotInfoManager.insertHostRobotInfo(hostRobotInfo2).getId());
//        System.out.println(hostRobotInfoManager.insertHostRobotInfo(hostRobotInfo3).getId());
//
//        EntityCursor<HostRobotInfo> sec_cursor = hostRobotInfoManager.secondaryIndex.subIndex(
//                hostRobotInfo1.getHostRobotInfoKey()).entities();
//        try {
//            for (HostRobotInfo output : sec_cursor) {
//                System.out.println(output.getHostRobotInfoKey().getHost());
//                System.out.println(output.getRobotFilePath());
//                System.out.println(output.getId());
//            }
//        } finally {
//            sec_cursor.close();
//        }
//
//        //hostRobotInfoManager.deleteHostRobotInfo(hostRobotInfo1);
//
//        System.out.println(hostRobotInfoManager.getHostRobotInfo(hostRobotInfo1.getHostRobotInfoKey()));
//
//        sec_cursor = hostRobotInfoManager.secondaryIndex.subIndex(
//                hostRobotInfo1.getHostRobotInfoKey()).entities();
//        try {
//            for (HostRobotInfo output : sec_cursor) {
//                System.out.println(output.getHostRobotInfoKey().getHost());
//                System.out.println(output.getRobotFilePath());
//                System.out.println(output.getId());
//            }
//        } finally {
//            sec_cursor.close();
//        }
//
//


    }

}
