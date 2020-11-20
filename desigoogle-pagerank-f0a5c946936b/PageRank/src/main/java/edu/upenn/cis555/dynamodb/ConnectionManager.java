package edu.upenn.cis555.dynamodb;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

public class ConnectionManager {

	Logger logger = LogManager.getLogger(ConnectionManager.class);

	private AmazonDynamoDB amazonDynamoDB;
	private DynamoDBMapper urlOnlyMapper;
	//private DynamoDBMapper docOnlyMapper;
	private DynamoDB dynamoDB;
	Table urlOnlyInfoTable;
	//Table docOnlyInfoTable;

	private String bucket_string = "docbucketdoc";


	public ConnectionManager() {

		amazonDynamoDB = AmazonDynamoDBClientBuilder
				.standard()
				.withCredentials(new AWSStaticCredentialsProvider(new AWSCredentials() {
					public String getAWSAccessKeyId() {
						return "AKIAIWQTWJZIXE2S3FYQ";
					}

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
	}

	public AmazonDynamoDB getAmazonDynamoDB() {
		return amazonDynamoDB;
	}

	Map<String,List<String>> backwardList = new LinkedHashMap<String,List<String>>();

	public void addToAdjacenyList(Map<String,List<String>> adjacentList, String key, String value) {
		if(key!=value) {
			List<String> keyList = null;
			if(!adjacentList.containsKey(key)) {
				keyList = new ArrayList<String>();
			} else {
				keyList = adjacentList.get(key);
			}
			keyList.add(value);
			adjacentList.put(key,keyList);
		}
	}

	public String parseKeyString(String url) {
		if("Root".equalsIgnoreCase(url)) {
			return url;
		}
		String[] keyStringSplit = url.split(":",2);
		logger.info(Arrays.toString(keyStringSplit));
		try {
			URL urlObj = new URL(keyStringSplit[1]);
			return urlObj.getProtocol()+"://"+urlObj.getHost()+":"+urlObj.getPort();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return keyStringSplit[1];
	}

	public List<URLInfo> getAllKeys() {
		logger.debug("load url table");
		List<URLInfo> list = null;
		try {
			list = urlOnlyMapper.scan(URLInfo.class, new DynamoDBScanExpression());
			//            for(URLInfo url : list) {
			//            	//String parent = parseKeyString(url.getParentKeyString());
			//            	//String current = parseKeyString(url.getKeyString());
			//            	//addToAdjacenyList(backwardList, parent, current);
			//            }

		} catch(Exception e) {
			e.printStackTrace();
		}

		if(list!=null) {
			

			//logger.debug("Total records in database=" + list.size());
			System.out.println("Total records in database=" + list.size());
		} else {
			//logger.debug("Bruh we got nothing");
			System.out.println("We got nithing bruv");
		}

		return list;
	}

	public static void main(String[] args) {
		org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
		ConnectionManager manager = new ConnectionManager();
		long startTime = System.currentTimeMillis();
		List<URLInfo> res = manager.getAllKeys();
		//String sampleUrl = "GET:https://vuzlit.ru:443/2296078/do_rozdumiv_schodo_tyaglosti/perervnosti_istoriyi_ukrayini_ta_znachuschosti/nespromozhnosti_ukrayinskogo_natsionalnogo_y_derzhavnitskogo_proektiv";
		//String sampleUrl = "Root";
		//System.out.println(manager.parseKeyString(sampleUrl));
		long endTime = System.currentTimeMillis();
		System.out.println(res.size());
		if(!res.isEmpty()) {
			BufferedWriter writer;
			try {
				writer = new BufferedWriter(new FileWriter(System.getProperty("user.dir")+"/rows.csv"));
				writer.write("keyString,protocol,host,port,filePath,requestMethod,docId,lastModifiedTime,createdTime,contentType,headers,statusCode,parentKeyString");
				writer.append("\n");
				for(URLInfo url: res) {
					writer.append(url.toString());
					writer.append("\n");
					writer.flush();
				}
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("Time taken: "+(endTime-startTime));
	}

}
