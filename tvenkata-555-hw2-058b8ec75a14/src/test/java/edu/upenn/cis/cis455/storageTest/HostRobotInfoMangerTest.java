package edu.upenn.cis.cis455.storageTest;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.Transaction;
import edu.upenn.cis.cis455.model.hostRobotInfo.HostRobotInfo;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;
import edu.upenn.cis.cis455.storage.managers.HostRobotInfoManager;
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
import java.util.Properties;

public class HostRobotInfoMangerTest extends TestCase {

    private HostRobotInfoManager hostRobotInfoManager;
    private StorageInterfaceImpl storageInterface;

    @Before
    public void setUp() throws URISyntaxException {

        URL url = this.getClass().getClassLoader().getResource(".");
        String baseDirectory = new File(url.toURI()).getAbsolutePath();
        String folderDirectory = Paths.get(baseDirectory, "playground3").toString();
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

        hostRobotInfoManager = new HostRobotInfoManager(storageInterface.getDataBaseConnectorConfig());
    }

    @Test
    public void testAdd() {


        HostRobotInfo hostRobotInfo = new HostRobotInfo("http", "www.google.com", 80);
        hostRobotInfo.setRobotFilePath("/robots.txt");
        hostRobotInfo.setAdditionalInfo(new HashMap<>());
        hostRobotInfo.setRobotStructure("");
        Transaction transaction = storageInterface.getDataBaseConnectorConfig().getDatabaseEnvironment().beginTransaction(null, null);
        hostRobotInfoManager.insertHostRobotInfo(hostRobotInfo, transaction);
        transaction.commit();
        Assert.assertEquals(hostRobotInfoManager.getSize(), 1);
    }

    @After
    public void tearDown() {
        storageInterface.close();
    }
}
