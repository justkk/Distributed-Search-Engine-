package edu.upenn.cis.cis455.storageTest;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import edu.upenn.cis.cis455.model.contentSeen.ContentSeenInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.DocOnlyInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfoKey;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;
import edu.upenn.cis.cis455.storage.managers.URLDataManager;
import junit.framework.TestCase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class URLDataMangerTest extends TestCase {

    private URLDataManager urlDataManager;
    private StorageInterfaceImpl storageInterface;

    @Before
    public void setUp() throws URISyntaxException {

        URL url = this.getClass().getClassLoader().getResource(".");
        String baseDirectory = new File(url.toURI()).getAbsolutePath();
        String folderDirectory = Paths.get(baseDirectory, "playground2").toString();
        DataBaseConnectorConfig dataBaseConnectorConfig = new DataBaseConnectorConfig();
        //dataBaseConnectorConfig.se
        Properties properties = new Properties();
        properties.put(EnvironmentConfig.LOG_MEM_ONLY, "true");
        EnvironmentConfig configuration = new EnvironmentConfig(properties);
        File file = new File(folderDirectory);
        configuration.setAllowCreate(true);
        configuration.setTransactional(true);
        Environment environment = new Environment(file, configuration);
        dataBaseConnectorConfig.setDatabaseEnvironment(environment);
        storageInterface = new StorageInterfaceImpl(dataBaseConnectorConfig);
        urlDataManager = new URLDataManager(storageInterface.getDataBaseConnectorConfig());
    }

    @Test
    public void testAdd() throws URISyntaxException {

        URLDataInfoKey urlDataInfoKey = new URLDataInfoKey("http", "www.google.com", 80,
                "/", "GET");
        URLDataInfo urlDataInfo = new URLDataInfo(urlDataInfoKey);
        urlDataInfo.setContentLength(100);
        urlDataInfo.setContentType("text/html");
        urlDataInfo.setData("hello");
        urlDataInfo.setHeaders(new HashMap<>());
        Transaction transaction = storageInterface.getDataBaseConnectorConfig().getDatabaseEnvironment().beginTransaction(null, null);
        urlDataManager.insertURLDataInfo(urlDataInfo, transaction);
        ContentSeenInfo contentSeenInfo = urlDataManager.getContentSeenInfo(urlDataInfo.getMd5Hash(), transaction);
        Assert.assertNotNull(contentSeenInfo);
        transaction.commit();
        Map<Integer, DocOnlyInfo> elements= urlDataManager.getDocOnlyInfoPrimaryIndex().sortedMap();
        for(Integer integer : elements.keySet()) {
            System.out.println(integer);
        }
        Assert.assertEquals(urlDataManager.getSize(), 1);
    }

    @After
    public void tearDown() {
        storageInterface.close();
    }
}
