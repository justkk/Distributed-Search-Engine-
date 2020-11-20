package edu.upenn.cis.cis455.dryRun;

import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.service.CrawlerService;
import edu.upenn.cis.cis455.model.UserChannelInfo;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;

public class DryRunTest extends TestCase {

    private StorageInterfaceImpl storageInterface;
    private Crawler crawler;

    @Before
    public void setUp() {

        URL url = this.getClass().getClassLoader().getResource(".");
        String baseDirectory = null;
        try {
            baseDirectory = new File(url.toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            fail();
        }
        String folderDirectory = Paths.get(baseDirectory, "playground").toString();
        DataBaseConnectorConfig dataBaseConnectorConfig = new DataBaseConnectorConfig();


        Properties properties = new Properties();
        properties.put(EnvironmentConfig.LOG_MEM_ONLY, "true");
        EnvironmentConfig configuration = new EnvironmentConfig(properties);
        File file = new File(folderDirectory);
        configuration.setAllowCreate(true);
        configuration.setTransactional(true);
        Environment environment = new Environment(file, configuration);
        dataBaseConnectorConfig.setDatabaseEnvironment(environment);
        storageInterface = new StorageInterfaceImpl(dataBaseConnectorConfig);
    }


    @Test
    public void testCase1() {

        String startUrl = "https://dbappserv.cis.upenn.edu/crawltest/nytimes/Business.xml";
        crawler = new Crawler(startUrl, storageInterface, 120000, 300);

        storageInterface.getUserChannelDataManager().insert(new UserChannelInfo(1,
                "channelBusiness", "/rss/channel/title[contains(text(), \"Business\")]"));

        Crawler.crawler_for_this_run = crawler;
        crawler.startV2();

        while (!crawler.isDone()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        List<Integer> docId = storageInterface.getIndexManager().getDocIdForChannel("channelBusiness");
        Assert.assertEquals(1, docId.size());

        System.out.println("Done crawling!");


    }

    public void tearDown() {
        storageInterface.close();
    }
}
