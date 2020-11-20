package edu.upenn.cis.cis455.storage.managers;

import com.sleepycat.je.LockMode;
import com.sleepycat.je.Transaction;
import com.sleepycat.persist.*;
import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.model.contentSeen.ContentSeenInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.DocOnlyInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfoKey;
import edu.upenn.cis.cis455.model.urlDataInfo.URLOnlyInfo;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class URLDataManager {

    private DataBaseConnectorConfig dataBaseConnectorConfig;
    private StoreConfig stConf;
    private EntityStore store;
    private PrimaryIndex<Integer, URLDataInfo> primaryIndex;
    private SecondaryIndex<URLDataInfoKey, Integer, URLDataInfo> secondaryIndex;
    private SecondaryIndex<String, Integer, URLDataInfo> contentSeenIndex;



    private PrimaryIndex<Integer, DocOnlyInfo> docOnlyInfoPrimaryIndex;
    private PrimaryIndex<URLDataInfoKey, URLOnlyInfo> urlOnlyInfoPrimaryIndex;
    private SecondaryIndex<String, Integer, DocOnlyInfo> docOnlyInfoContentSeenIndex;
    private SecondaryIndex<Integer, URLDataInfoKey, URLOnlyInfo> urlOnlyInfoSecondaryIndex;

    private EntityStore docOnlyInfoStore;
    private EntityStore urlOnlyInfoStore;


    public URLDataManager(DataBaseConnectorConfig dataBaseConnectorConfig) {
        this.dataBaseConnectorConfig = dataBaseConnectorConfig;
        stConf = new StoreConfig();
        stConf.setAllowCreate(true);
        stConf.setTransactional(true);
        store = new EntityStore(this.dataBaseConnectorConfig.getDatabaseEnvironment(),
                ConstantsHW2.getInstance().getURL_DOC_DATABASE(), stConf);

        docOnlyInfoStore = new EntityStore(this.dataBaseConnectorConfig.getDatabaseEnvironment(),
                ConstantsHW2.getInstance().getDOC_ONLY_DATABASE(), stConf);
        urlOnlyInfoStore = new EntityStore(this.dataBaseConnectorConfig.getDatabaseEnvironment(),
                ConstantsHW2.getInstance().getURL_ONLY_DATABASE(), stConf);

        docOnlyInfoPrimaryIndex = docOnlyInfoStore.getPrimaryIndex(Integer.class, DocOnlyInfo.class);
        urlOnlyInfoPrimaryIndex = urlOnlyInfoStore.getPrimaryIndex(URLDataInfoKey.class, URLOnlyInfo.class);
        docOnlyInfoContentSeenIndex = docOnlyInfoStore.getSecondaryIndex(docOnlyInfoPrimaryIndex, String.class, "hash");
        urlOnlyInfoSecondaryIndex = urlOnlyInfoStore.getSecondaryIndex(urlOnlyInfoPrimaryIndex, Integer.class, "docId");

    }


    public URLDataInfo getURLDataInfo(URLDataInfoKey urlDataInfoKey, Transaction txn) {
        return getURLDataTest(urlDataInfoKey, txn);
//        return secondaryIndex.get(txn, urlDataInfoKey, LockMode.READ_COMMITTED);
    }

    public ContentSeenInfo getContentSeenInfo(String key, Transaction txn) {
        return getContentSeenInfoTest(key, txn);
//        URLDataInfo alreadyPresent = contentSeenIndex.get(txn, key, LockMode.READ_COMMITTED);
//        if(alreadyPresent == null) {
//            return null;
//        }
//        ContentSeenInfo contentSeenInfo = new ContentSeenInfo(alreadyPresent.getMd5Hash());
//        return contentSeenInfo;
    }


    public URLDataInfo insertURLDataInfo(URLDataInfo urlDataInfo, Transaction txn) {
        return insertURLDataInfoTest(urlDataInfo, txn);
//        URLDataInfo alreadyPresent = getURLDataInfo(urlDataInfo.getUrlDataInfoKey(), txn);
//        if(alreadyPresent == null) {
//            urlDataInfo.setCreatedTime(new Date());
//        } else {
//            urlDataInfo.setId(alreadyPresent.getId());
//        }
//        urlDataInfo.setLastModifiedTime(new Date());
//        primaryIndex.put(txn, urlDataInfo);
//        return urlDataInfo;
    }

    public ContentSeenInfo getContentSeenInfoTest(String key, Transaction txn) {

        DocOnlyInfo seenDocument = docOnlyInfoContentSeenIndex.get(txn, key, LockMode.READ_COMMITTED);
        if(seenDocument == null) {
            return null;
        }
        ContentSeenInfo contentSeenInfo = new ContentSeenInfo(seenDocument.getHash());
        return contentSeenInfo;
    }

    public URLDataInfo insertURLDataInfoTest(URLDataInfo urlDataInfo, Transaction txn) {
        URLDataInfo alreadyPresent = getURLDataTest(urlDataInfo.getUrlDataInfoKey(), txn);
        if(alreadyPresent == null) {
            // new insert
            DocOnlyInfo docOnlyInfo = insertNewDocOnlyInfo(urlDataInfo.getDocOnlyInfo(), txn);
            URLOnlyInfo urlOnlyInfo = urlDataInfo.getURLOnlyInfo();
            urlOnlyInfo.setDocId(docOnlyInfo.getId());
            urlOnlyInfo = insertURLOnlyInfo(docOnlyInfo, urlOnlyInfo, txn);
            return new URLDataInfo(docOnlyInfo, urlOnlyInfo);

        } else {
            DocOnlyInfo oldDocOnlyInfo = alreadyPresent.getDocOnlyInfo();
            URLOnlyInfo urlOnlyInfo = alreadyPresent.getURLOnlyInfo();
            DocOnlyInfo docOnlyInfo = insertNewDocOnlyInfo(urlDataInfo.getDocOnlyInfo(), txn);
            urlOnlyInfo = updateURLOnlyInfo(docOnlyInfo, urlOnlyInfo, txn);
            processOldDocInfo(oldDocOnlyInfo, txn);
            return new URLDataInfo(docOnlyInfo, urlOnlyInfo);
        }
    }

    public DocOnlyInfo insertNewDocOnlyInfo(DocOnlyInfo docOnlyInfo, Transaction txn) {

        DocOnlyInfo checkIfPresent = docOnlyInfoContentSeenIndex.get(txn, docOnlyInfo.getHash(), LockMode.READ_COMMITTED);
        if (checkIfPresent!=null) {
            return checkIfPresent;
        } else {
            docOnlyInfoPrimaryIndex.put(txn, docOnlyInfo);
            return docOnlyInfo;
        }
    }

    public URLOnlyInfo insertURLOnlyInfo(DocOnlyInfo docOnlyInfo, URLOnlyInfo urlOnlyInfo, Transaction txn) {

        urlOnlyInfo.setDocId(docOnlyInfo.getId());
        urlOnlyInfo.setCreatedTime(new Date());
        urlOnlyInfo.setLastModifiedTime(new Date());
        urlOnlyInfoPrimaryIndex.put(txn, urlOnlyInfo);
        return urlOnlyInfo;
    }

    public URLOnlyInfo updateURLOnlyInfo(DocOnlyInfo docOnlyInfo, URLOnlyInfo urlOnlyInfo, Transaction txn) {

        urlOnlyInfo.setDocId(docOnlyInfo.getId());
        urlOnlyInfo.setLastModifiedTime(new Date());
        urlOnlyInfoPrimaryIndex.put(txn, urlOnlyInfo);
        return urlOnlyInfo;
    }

    public void processOldDocInfo(DocOnlyInfo oldDocOnlyInfo, Transaction txn) {


        URLOnlyInfo temp = urlOnlyInfoSecondaryIndex.get(txn, oldDocOnlyInfo.getId(), LockMode.READ_COMMITTED);
        if(temp == null) {
            docOnlyInfoPrimaryIndex.delete(txn, oldDocOnlyInfo.getId());
        }

    }


    public URLDataInfo getURLDataTest(URLDataInfoKey urlDataInfoKey, Transaction txn) {
        URLOnlyInfo urlOnlyInfo = urlOnlyInfoPrimaryIndex.get(txn, urlDataInfoKey, LockMode.READ_COMMITTED);
        if(urlOnlyInfo == null) {
            return null;
        }

        DocOnlyInfo docOnlyInfo = docOnlyInfoPrimaryIndex.get(txn, urlOnlyInfo.getDocId(), LockMode.READ_COMMITTED);

        if(docOnlyInfo == null) {
            urlOnlyInfoPrimaryIndex.delete(txn, urlDataInfoKey);
            return null;
        }

        return new URLDataInfo(docOnlyInfo, urlOnlyInfo);
    }


    public List<URLDataInfo> getURLDataInfoForDocument(Integer docId) {

        List<URLDataInfo> urlDataInfoArrayList = new ArrayList<>();
        DocOnlyInfo docOnlyInfo = docOnlyInfoPrimaryIndex.get(docId);
        List<URLOnlyInfo> urlOnlyInfoList = new ArrayList<>();
        EntityCursor<URLOnlyInfo> urlOnlyInfoEntityCursor = urlOnlyInfoSecondaryIndex.subIndex(docId).entities();

        try {
            for(URLOnlyInfo urlOnlyInfo : urlOnlyInfoEntityCursor) {
                urlOnlyInfoList.add(urlOnlyInfo);
            }
        } finally {
            urlOnlyInfoEntityCursor.close();
        }

        for(URLOnlyInfo urlOnlyInfo : urlOnlyInfoList) {
            urlDataInfoArrayList.add(new URLDataInfo(docOnlyInfo, urlOnlyInfo));
        }

        return urlDataInfoArrayList;
    }

//    public URLDataInfo updateURLDataInfo(URLDataInfo hostRobotInfo) {
//        hostRobotInfo.setLastModifiedTime(new Date());
//        primaryIndex.put(hostRobotInfo);
//        return hostRobotInfo;
//    }
//
//    public void deleteHostRobotInfo(URLDataInfo urlDataInfo) {
//        primaryIndex.delete(urlDataInfo.getId());
//        secondaryIndex.delete(urlDataInfo.getUrlDataInfoKey());
//    }


    public int getSize() {
        int count = (int) docOnlyInfoPrimaryIndex.count();
        return count;
    }

    public void close() {
        if(store!=null) {
            store.close();
            urlOnlyInfoStore.close();
            docOnlyInfoStore.close();
        }
    }

    public static void main(String[] args) {

//        DataBaseConnectorConfig dataBaseConnectorConfig = new DataBaseConnectorConfig("/Users/nikhilt/Desktop/Penn/CourseWork/IWS/projects/555-hw2/dbs");
//
//        URLDataManager urlDataManager = new URLDataManager(dataBaseConnectorConfig);
//        System.out.println(urlDataManager.docOnlyInfoPrimaryIndex.count());

    }

    public PrimaryIndex<Integer, DocOnlyInfo> getDocOnlyInfoPrimaryIndex() {
        return docOnlyInfoPrimaryIndex;
    }

    public void setDocOnlyInfoPrimaryIndex(PrimaryIndex<Integer, DocOnlyInfo> docOnlyInfoPrimaryIndex) {
        this.docOnlyInfoPrimaryIndex = docOnlyInfoPrimaryIndex;
    }
}
