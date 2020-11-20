package edu.upenn.cis.cis455.storage.managers;

import com.sleepycat.persist.*;
import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.model.index.DocChannelIndex;
import edu.upenn.cis.cis455.model.index.DocChannelIndexKey;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;

import java.util.ArrayList;
import java.util.List;

public class IndexManager {

    private DataBaseConnectorConfig dataBaseConnectorConfig;
    private StoreConfig stConf;
    private EntityStore store;
    private PrimaryIndex<DocChannelIndexKey, DocChannelIndex> primaryIndex;
    private SecondaryIndex<Integer, DocChannelIndexKey, DocChannelIndex> secondaryIndexDoc;
    private SecondaryIndex<String, DocChannelIndexKey, DocChannelIndex> secondaryIndexChannel;

    public IndexManager(DataBaseConnectorConfig dataBaseConnectorConfig) {
        this.dataBaseConnectorConfig = dataBaseConnectorConfig;
        this.dataBaseConnectorConfig = dataBaseConnectorConfig;
        stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        store = new EntityStore(this.dataBaseConnectorConfig.getDatabaseEnvironment(),
                ConstantsHW2.getInstance().getINDEX_DATABASE(), stConf);
        primaryIndex = store.getPrimaryIndex(DocChannelIndexKey.class, DocChannelIndex.class);
        secondaryIndexDoc = store.getSecondaryIndex(primaryIndex, Integer.class, "docId");
        secondaryIndexChannel = store.getSecondaryIndex(primaryIndex, String.class, "channelName");
    }


    public DocChannelIndex insert(DocChannelIndex docChannelIndex) {
        primaryIndex.put(docChannelIndex);
        return docChannelIndex;
    }

    public void delete(Integer docID) {
//        EntityCursor<DocChannelIndex> entityCursor  = secondaryIndexDoc.subIndex(docID).entities();
//        List<DocChannelIndexKey> docChannelIndexKeys = new ArrayList<>();
//
//        try {
//            for(DocChannelIndex entry: entityCursor) {
//                docChannelIndexKeys.add(entry.getDocChannelIndexKey());
////                primaryIndex.delete(entry.getDocChannelIndexKey());
//            }
//        } finally {
//            entityCursor.close();
//        }
//
//        for(DocChannelIndexKey docChannelIndexKey : docChannelIndexKeys) {
//            primaryIndex.delete()
//
//        }
        secondaryIndexDoc.delete(docID);

    }

    public List<Integer> getDocIdForChannel(String channelName) {
        EntityCursor<DocChannelIndex> entityCursor = secondaryIndexChannel.subIndex(channelName).entities();
        List<Integer> resultList = new ArrayList<>();
        try {
            for(DocChannelIndex entry: entityCursor) {
                resultList.add(entry.getDocId());
            }
        } finally {
            entityCursor.close();
        }
        return resultList;
    }

    public void close() {
        if(store!=null) {
            store.close();
        }
    }

}
