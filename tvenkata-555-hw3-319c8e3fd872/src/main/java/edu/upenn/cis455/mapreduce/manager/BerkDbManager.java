package edu.upenn.cis455.mapreduce.manager;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Sequence;
import com.sleepycat.persist.*;

import java.io.File;

public class BerkDbManager {


    private String storageLocation;
    private String databaseName;

    private StoreConfig stConf;
    private EntityStore store;
    private Environment databaseEnvironment;
    private Sequence primaryIdSequence;

    private PrimaryIndex<Integer, TupleStore> primaryIndex;
    private SecondaryIndex<String, Integer, TupleStore> secondaryIndex;

    public BerkDbManager(Environment databaseEnvironment, String databaseName) {
        this.databaseName = databaseName;
        stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        this.databaseEnvironment = databaseEnvironment;
        store = new EntityStore(databaseEnvironment, databaseName, stConf);
        primaryIndex = store.getPrimaryIndex(Integer.class, TupleStore.class);
        secondaryIndex = store.getSecondaryIndex(primaryIndex, String.class, "key");
    }


    public void addTuple(String key, String value) {
        TupleStore tupleStore = new TupleStore(key, value);
        primaryIndex.put(tupleStore);
    }

    public EntityCursor<TupleStore> getCursorForKey(String key){
        EntityCursor<TupleStore> entityCursor = secondaryIndex.subIndex(key).entities();
        return entityCursor;
    }

    public EntityCursor<String> getKeyCursor() {
        EntityCursor<String> entityCursor = secondaryIndex.keys();
        return entityCursor;
    }

    public void deleteStore() {
        store.truncateClass(TupleStore.class);
        System.out.println("Store Deleted");
    }

    public void close() {
        try {
            store.close();
            databaseEnvironment.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }






}
