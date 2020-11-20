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
import edu.upenn.cis.cis455.storage.dynamoDb.UrlOnlyInfoDynamoDb;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class URLDataManager {

    private DataBaseConnectorConfig dataBaseConnectorConfig;
//    private StoreConfig stConf;
//    private EntityStore store;
//    private PrimaryIndex<Integer, URLDataInfo> primaryIndex;
//    private SecondaryIndex<URLDataInfoKey, Integer, URLDataInfo> secondaryIndex;
//    private SecondaryIndex<String, Integer, URLDataInfo> contentSeenIndex;
//
//
//
//    private PrimaryIndex<String, DocOnlyInfo> docOnlyInfoPrimaryIndex;
//    private PrimaryIndex<URLDataInfoKey, URLOnlyInfo> urlOnlyInfoPrimaryIndex;
//    private SecondaryIndex<String, String, DocOnlyInfo> docOnlyInfoContentSeenIndex;
//    private SecondaryIndex<String, URLDataInfoKey, URLOnlyInfo> urlOnlyInfoSecondaryIndex;
//
//    private EntityStore docOnlyInfoStore;
//    private EntityStore urlOnlyInfoStore;

    private UrlDataDynamoDbManager urlDataDynamoDbManager;


    public URLDataManager(DataBaseConnectorConfig dataBaseConnectorConfig) {
        this.dataBaseConnectorConfig = dataBaseConnectorConfig;
        urlDataDynamoDbManager = new UrlDataDynamoDbManager();

    }


    public URLDataInfo getURLDataInfo(URLDataInfoKey urlDataInfoKey, Transaction txn) {
        return getURLDataTest(urlDataInfoKey, txn);
    }

    public ContentSeenInfo getContentSeenInfo(String key, Transaction txn) {
        return getContentSeenInfoTest(key, txn);

    }


    public URLDataInfo insertURLDataInfo(URLDataInfo urlDataInfo, Transaction txn) {
        return insertURLDataInfoTest(urlDataInfo, txn);

    }

    public ContentSeenInfo getContentSeenInfoTest(String key, Transaction txn) {

        //DocOnlyInfo seenDocument = docOnlyInfoContentSeenIndex.get(txn, key, LockMode.READ_COMMITTED);
        DocOnlyInfo seenDocument = urlDataDynamoDbManager.getDocOnlyInfoByHash(key);
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

        DocOnlyInfo checkIfPresent = urlDataDynamoDbManager.getDocOnlyInfoByHash(docOnlyInfo.getHash());
        if (checkIfPresent!=null) {
            System.out.println("Duplicate Document from Dynamo");
            return checkIfPresent;
        } else {
            System.out.println("Creating New Document Dynamo DB");
            return urlDataDynamoDbManager.insertDocOnlyInfo(docOnlyInfo);
        }
    }

    public URLOnlyInfo insertURLOnlyInfo(DocOnlyInfo docOnlyInfo, URLOnlyInfo urlOnlyInfo, Transaction txn) {

        urlOnlyInfo.setDocId(docOnlyInfo.getId());
        urlOnlyInfo.setCreatedTime(new Date());
        urlOnlyInfo.setLastModifiedTime(new Date());
        return urlDataDynamoDbManager.insertURLOnlyInfo(urlOnlyInfo);
    }

    public URLOnlyInfo updateURLOnlyInfo(DocOnlyInfo docOnlyInfo, URLOnlyInfo urlOnlyInfo, Transaction txn) {

        urlOnlyInfo.setDocId(docOnlyInfo.getId());
        urlOnlyInfo.setLastModifiedTime(new Date());
        return urlDataDynamoDbManager.insertURLOnlyInfo(urlOnlyInfo);
    }

    public void processOldDocInfo(DocOnlyInfo oldDocOnlyInfo, Transaction txn) {


        URLOnlyInfo temp = urlDataDynamoDbManager.getUrlOnlyInfoByDocId(oldDocOnlyInfo.getId());
        if(temp == null) {
            urlDataDynamoDbManager.deleteDocOnlyInfo(oldDocOnlyInfo.getId());
        }

    }


    public URLDataInfo getURLDataTest(URLDataInfoKey urlDataInfoKey, Transaction txn) {
        // TODO -- Done
        URLOnlyInfo urlOnlyInfo = urlDataDynamoDbManager.getURLOnlyInfo(urlDataInfoKey);


        if(urlOnlyInfo == null) {
            return null;
        }
        // TODO -- Done
        DocOnlyInfo docOnlyInfo = urlDataDynamoDbManager.getDocOnlyInfo(urlOnlyInfo.getDocId());


        if(docOnlyInfo == null) {
            // TODO
            System.out.println("Deleting old info");
            urlDataDynamoDbManager.deleteURLOnlyInfo(urlDataInfoKey);
            return null;
        }

        return new URLDataInfo(docOnlyInfo, urlOnlyInfo);
    }


    public List<URLDataInfo> getURLDataInfoForDocument(String docId) {

        List<URLDataInfo> urlDataInfoArrayList = new ArrayList<>();
        //DocOnlyInfo docOnlyInfo = docOnlyInfoPrimaryIndex.get(docId);
        DocOnlyInfo docOnlyInfo = urlDataDynamoDbManager.getDocOnlyInfo(docId);

        List<URLOnlyInfo> urlOnlyInfoList = new ArrayList<>();
        List<UrlOnlyInfoDynamoDb> urlOnlyInfoEntityCursor = urlDataDynamoDbManager.getUrlOnlyInfoByDocIdList(docId);

        try {
            for(UrlOnlyInfoDynamoDb urlOnlyInfoDynamoDb : urlOnlyInfoEntityCursor) {
                urlOnlyInfoList.add(urlDataDynamoDbManager.getURLOnlyInfo(urlOnlyInfoDynamoDb));
            }
        } finally {
        }

        for(URLOnlyInfo urlOnlyInfo : urlOnlyInfoList) {
            urlDataInfoArrayList.add(new URLDataInfo(docOnlyInfo, urlOnlyInfo));
        }

        return urlDataInfoArrayList;
    }



    public void close() {

    }

    public DocOnlyInfo getOnlyDocOnlyInfoFromId(String docId) {
        return urlDataDynamoDbManager.getDocOnlyInfo(docId);
    }

}
