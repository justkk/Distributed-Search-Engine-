package edu.upenn.cis.cis455.storage.managers;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.google.gson.Gson;
import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.model.urlDataInfo.DocOnlyInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfoKey;
import edu.upenn.cis.cis455.model.urlDataInfo.URLOnlyInfo;
import edu.upenn.cis.cis455.redisHelper.RedisDBManager;
import edu.upenn.cis.cis455.storage.dynamoDb.DocOnlyInfoDynamoDb;
import edu.upenn.cis.cis455.storage.dynamoDb.UrlOnlyInfoDynamoDb;
import edu.upenn.cis.cis455.utils.SHAHashGenerator;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrlDataDynamoDbManager {

    private AmazonDynamoDB amazonDynamoDB;
    private DynamoDBMapper urlOnlyMapper;
    //private DynamoDBMapper docOnlyMapper;
    private DynamoDB dynamoDB;
    Table urlOnlyInfoTable;
    //Table docOnlyInfoTable;

    private Gson gson = new Gson();

    private RedisDBManager redisDBManager = new RedisDBManager(ConstantsHW2.getInstance().getRedisConfiguration());


    private AmazonS3 s3;

    private String bucket_string = "docbucketdoc";


    public UrlDataDynamoDbManager() {

        amazonDynamoDB = AmazonDynamoDBClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(new AWSCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return "AKIAIWQTWJZIXE2S3FYQ";
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return "HDyK5UG6d7KPAh8hgnJ/AKUyFqbhiyScz4fOxJ33";
                    }
                })).withRegion("us-east-1")
                .build();
        urlOnlyMapper = new DynamoDBMapper(amazonDynamoDB);
        //docOnlyMapper = new DynamoDBMapper(amazonDynamoDB);
        dynamoDB = new DynamoDB(amazonDynamoDB);
        urlOnlyInfoTable = dynamoDB.getTable("UrlOnlyInfo");
        //docOnlyInfoTable = dynamoDB.getTable("DocOnlyInfo");

        s3 = AmazonS3ClientBuilder.standard().withRegion("us-east-1").build();

    }

    public AmazonDynamoDB getAmazonDynamoDB() {
        return amazonDynamoDB;
    }


    public URLOnlyInfo getURLOnlyInfo(URLDataInfoKey urlDataInfoKey) {
        String keyString = urlDataInfoKey.getKeyString();
        UrlOnlyInfoDynamoDb urlOnlyInfoDynamoDb = urlOnlyMapper.load(UrlOnlyInfoDynamoDb.class, keyString);
        return getURLOnlyInfo(urlOnlyInfoDynamoDb);
    }

    public DocOnlyInfo insertDocOnlyInfo(DocOnlyInfo docOnlyInfo) {

        try {
            String docId = docOnlyInfo.getHash();
            docOnlyInfo.setId(docId);
            DocOnlyInfoDynamoDb docOnlyInfoDynamoDb = new DocOnlyInfoDynamoDb(docOnlyInfo);
            String jsonContent = gson.toJson(docOnlyInfoDynamoDb);
            s3.putObject(bucket_string, docId, jsonContent);
            //return docOnlyInfo;
//        docOnlyMapper.save(docOnlyInfoDynamoDb);
            return getDocOnlyInfo(docOnlyInfoDynamoDb);
        } catch (Exception e) {
//            StringWriter sw = new StringWriter();
//            PrintWriter pw = new PrintWriter(sw);
//            e.printStackTrace(pw);
//            redisDBManager.lpush("logs", e.getMessage() + "\n.....\n" + pw.toString());
        }
        return null;


    }


    public URLOnlyInfo insertURLOnlyInfo(URLOnlyInfo urlOnlyInfo) {
        UrlOnlyInfoDynamoDb urlOnlyInfoDynamoDb = new UrlOnlyInfoDynamoDb(urlOnlyInfo);
        urlOnlyMapper.save(urlOnlyInfoDynamoDb);
        return getURLOnlyInfo(urlOnlyInfoDynamoDb);
    }

    public void deleteURLOnlyInfo(URLDataInfoKey urlDataInfoKey) {
        String keyString = urlDataInfoKey.getKeyString();
        urlOnlyInfoTable.deleteItem(new PrimaryKey("keyString", keyString));
    }


    public DocOnlyInfo getDocOnlyInfo(String docId) {

        String content = null;

        try {
            S3Object o = s3.getObject(bucket_string, docId);
            if(o == null) {
                return null;
            }
            InputStream inputStream = o.getObjectContent();
            content = IOUtils.toString(inputStream);
        } catch (IOException e) {
//            StringWriter sw = new StringWriter();
//            PrintWriter pw = new PrintWriter(sw);
//            e.printStackTrace(pw);
//            redisDBManager.lpush("logs", e.getMessage() + "\n.....\n" + pw.toString());
            return null;
        } catch (Exception e) {
//            StringWriter sw = new StringWriter();
//            PrintWriter pw = new PrintWriter(sw);
//            e.printStackTrace(pw);
//            redisDBManager.lpush("logs", e.getMessage() + "\n.....\n" + pw.toString());
        }
        //DocOnlyInfoDynamoDb docOnlyInfoDynamoDb = docOnlyMapper.load(DocOnlyInfoDynamoDb.class, docId);
        DocOnlyInfoDynamoDb docOnlyInfoDynamoDb = gson.fromJson(content, DocOnlyInfoDynamoDb.class);
        return getDocOnlyInfo(docOnlyInfoDynamoDb);
    }

    public void deleteDocOnlyInfo(String docId) {

        try {
            s3.deleteObject(bucket_string, docId);
        } catch (Exception e) {
//            StringWriter sw = new StringWriter();
//            PrintWriter pw = new PrintWriter(sw);
//            e.printStackTrace(pw);
//            redisDBManager.lpush("logs", e.getMessage() + "\n.....\n" + pw.toString());
        }

//        docOnlyInfoTable.deleteItem(new PrimaryKey("id", docId));
    }

    public DocOnlyInfo getDocOnlyInfoByHash(String docHash) {
        return getDocOnlyInfo(docHash);
//        Map<String, AttributeValue> eav = new HashMap<>();
//        eav.put(":v1", new AttributeValue().withS(docHash));
//
//        DynamoDBQueryExpression<DocOnlyInfoDynamoDb> queryExpression = new DynamoDBQueryExpression<DocOnlyInfoDynamoDb>()
//                .withIndexName("hashIndex")
//                .withConsistentRead(false)
//                .withKeyConditionExpression("hashString = :v1")
//                .withExpressionAttributeValues(eav);
//
//
//        List<DocOnlyInfoDynamoDb> iList =  docOnlyMapper.query(DocOnlyInfoDynamoDb.class, queryExpression);
//        if(iList.size() > 0) {
//            return getDocOnlyInfo(iList.get(0));
//        }
//        return null;

    }

    public URLOnlyInfo getUrlOnlyInfoByDocId(String docId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1", new AttributeValue().withS(docId));

        DynamoDBQueryExpression<UrlOnlyInfoDynamoDb> queryExpression = new DynamoDBQueryExpression<UrlOnlyInfoDynamoDb>()
                .withIndexName("docIndex")
                .withConsistentRead(false)
                .withKeyConditionExpression("docId = :v1")
                .withExpressionAttributeValues(eav);


        List<UrlOnlyInfoDynamoDb> iList =  urlOnlyMapper.query(UrlOnlyInfoDynamoDb.class, queryExpression);
        if(iList.size() > 0) {
            return getURLOnlyInfo(iList.get(0));
        }
        return null;

    }

    public List<UrlOnlyInfoDynamoDb> getUrlOnlyInfoByDocIdList(String docId) {
        Map<String, AttributeValue> eav = new HashMap<>();
        eav.put(":v1", new AttributeValue().withS(docId));

        DynamoDBQueryExpression<UrlOnlyInfoDynamoDb> queryExpression = new DynamoDBQueryExpression<UrlOnlyInfoDynamoDb>()
                .withIndexName("docIndex")
                .withConsistentRead(false)
                .withKeyConditionExpression("docId = :v1")
                .withExpressionAttributeValues(eav);


        List<UrlOnlyInfoDynamoDb> iList =  urlOnlyMapper.query(UrlOnlyInfoDynamoDb.class, queryExpression);
        return iList;
    }


    public DocOnlyInfo getDocOnlyInfo(DocOnlyInfoDynamoDb docOnlyInfoDynamoDb) {

        if(docOnlyInfoDynamoDb == null) {
            return null;
        }

        DocOnlyInfo docOnlyInfo = new DocOnlyInfo();
        docOnlyInfo.setHash(docOnlyInfoDynamoDb.getHashString());
        docOnlyInfo.setContent(docOnlyInfoDynamoDb.getContent());
        docOnlyInfo.setContentLength(docOnlyInfoDynamoDb.getContentLength());
        docOnlyInfo.setId(docOnlyInfoDynamoDb.getId());
        return docOnlyInfo;
    }

    public URLOnlyInfo getURLOnlyInfo(UrlOnlyInfoDynamoDb urlOnlyInfoDynamoDb) {

        if(urlOnlyInfoDynamoDb == null) {
            return null;
        }

        URLOnlyInfo urlOnlyInfo = new URLOnlyInfo();
        urlOnlyInfo.setHeaders(urlOnlyInfoDynamoDb.getHeaders());
        urlOnlyInfo.setCreatedTime(urlOnlyInfoDynamoDb.getCreatedTime());
        urlOnlyInfo.setLastModifiedTime(urlOnlyInfoDynamoDb.getLastModifiedTime());
        urlOnlyInfo.setStatusCode(urlOnlyInfoDynamoDb.getStatusCode());
        urlOnlyInfo.setContentType(urlOnlyInfoDynamoDb.getContentType());
        urlOnlyInfo.setDocId(urlOnlyInfoDynamoDb.getDocId());
        urlOnlyInfo.setUrlDataInfoKey(new URLDataInfoKey(urlOnlyInfoDynamoDb.getProtocol(), urlOnlyInfoDynamoDb.getHost(), urlOnlyInfoDynamoDb.getPort(), urlOnlyInfoDynamoDb.getFilePath(),
                urlOnlyInfoDynamoDb.getRequestMethod()));
        urlOnlyInfo.setParentKeyString(urlOnlyInfoDynamoDb.getParentKeyString());

        return urlOnlyInfo;
    }


    public static void main(String[] args) {
        UrlDataDynamoDbManager urlOnlyDynamoDbManager = new UrlDataDynamoDbManager();
        DynamoDBMapper mapper = new DynamoDBMapper(urlOnlyDynamoDbManager.amazonDynamoDB);
//
//        DocOnlyInfo docOnlyInfo =  urlOnlyDynamoDbManager.getDocOnlyInfoByHash("NNNNNN");
//        System.out.println(docOnlyInfo);

        CreateTableRequest req = mapper.generateCreateTableRequest(DocOnlyInfoDynamoDb.class);
//// Table provision throughput is still required since it cannot be specified in your POJO
        req.setProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
        req.getGlobalSecondaryIndexes().get(0).setProvisionedThroughput(new ProvisionedThroughput(5l, 5l));
        req.getGlobalSecondaryIndexes().get(0).setProjection(new Projection().withProjectionType(ProjectionType.ALL));

        //        req.getGlobalSecondaryIndexes().get(1).setProvisionedThroughput(new ProvisionedThroughput(5l, 5l));
//        req.getGlobalSecondaryIndexes().get(2).setProvisionedThroughput(new ProvisionedThroughput(5l, 5l));
//
//
////// Fire off the CreateTableRequest using the low-level client
        urlOnlyDynamoDbManager.amazonDynamoDB.createTable(req);

//        DocOnlyInfo docOnlyInfo = new DocOnlyInfo();
//        docOnlyInfo.setContentLength(6);
//        docOnlyInfo.setHash("NNNNNN");
//        docOnlyInfo.setContent("Nikhil1");
//        DocOnlyInfoDynamoDb docOnlyInfoDynamoDb = new DocOnlyInfoDynamoDb(docOnlyInfo);
//
//        mapper.save(docOnlyInfoDynamoDb);
//        System.out.println(docOnlyInfoDynamoDb.getId());


//        URLDataInfoKey urlDataInfoKey = new URLDataInfoKey("http", "www.google.com", 80,
//                "/welcome", "GET");
//        URLOnlyInfo urlOnlyInfo = new URLOnlyInfo(urlDataInfoKey, 0);
//        urlOnlyInfo.setLastModifiedTime(new Date());
//        urlOnlyInfo.setCreatedTime(new Date());
//        urlOnlyInfo.setHeaders(new HashMap<>());
//
//        UrlOnlyInfoDynamoDb urlOnlyInfoDynamoDb = new UrlOnlyInfoDynamoDb(urlOnlyInfo);
//        DynamoDBMapper mapper = new DynamoDBMapper(urlOnlyDynamoDbManager.amazonDynamoDB);
//        DynamoDB dynamoDB = new DynamoDB(urlOnlyDynamoDbManager.amazonDynamoDB);
//        Table table = dynamoDB.getTable("UrlOnlyInfo");
//        table.delete();
//        try {
//            table.waitForDelete();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
////
//////        mapper.save(urlOnlyInfoDynamoDb);
////
////        UrlOnlyInfoDynamoDb fetchedResult = mapper.load(UrlOnlyInfoDynamoDb.class,  urlOnlyInfoDynamoDb.getKeyString());
////        System.out.println(fetchedResult);
//
//        Item item = table.getItem("keyString", urlOnlyInfoDynamoDb.getKeyString());
//        System.out.println(item);
    }
}
