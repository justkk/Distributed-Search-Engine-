package edu.upenn.cis.cis455.service;

import com.sleepycat.je.Environment;
import com.sleepycat.je.Transaction;
import edu.upenn.cis.cis455.TestException;
import edu.upenn.cis.cis455.crawler.CrawlServiceResponse;
import edu.upenn.cis.cis455.crawler.Crawler;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.crawler.queue.CrawlDataStructure;
import edu.upenn.cis.cis455.crawler.queue.ReadyQueueInstance;
import edu.upenn.cis.cis455.crawler.service.CrawlerService;
import edu.upenn.cis.cis455.crawler.validator.ProcessContext;
import edu.upenn.cis.cis455.crawler.validator.ProcessManager;
import edu.upenn.cis.cis455.crawler.validator.processImpl.*;
import edu.upenn.cis.cis455.model.representationModels.URLMetaInformation;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import edu.upenn.cis.cis455.storage.berkDb.DataBaseConnectorConfig;
import edu.upenn.cis.cis455.storage.managers.URLDataManager;
import edu.upenn.cis.cis455.utils.DocUrlParser;
import junit.framework.TestCase;
import org.apache.logging.log4j.Level;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;

public class CrawlerServiceTest extends TestCase {

    private ProcessManager processManager;
    private Crawler crawler;
    private DocUrlParser documentParser;
    private StorageInterfaceImpl storageInterface;

    private RobotPermissionChecker robotPermissionChecker;
    private RobotDelayChecker robotDelayChecker;
    private DBDataEnricher dbDataEnricher;
    private HeadDataEnricher headDataEnricher;
    private GetDataEnricher getDataEnricher;
    private IndexDocumentProcessor indexDocumentProcessor;
    private CrawlerService crawlerService;

    private Transaction transaction;
    private DataBaseConnectorConfig dataBaseConnectorConfig;
    private Environment databaseEnvironment;
    private CrawlDataStructure crawlDataStructure;
    private HashSet hashSet;


    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        storageInterface = Mockito.mock(StorageInterfaceImpl.class);
        crawler = Mockito.mock(Crawler.class);
        documentParser = Mockito.mock(DocUrlParser.class);
        robotPermissionChecker = Mockito.mock(RobotPermissionChecker.class);
        robotDelayChecker = Mockito.mock(RobotDelayChecker.class);
        dbDataEnricher = Mockito.mock(DBDataEnricher.class);
        headDataEnricher = Mockito.mock(HeadDataEnricher.class);
        getDataEnricher = Mockito.mock(GetDataEnricher.class);
        indexDocumentProcessor = Mockito.mock(IndexDocumentProcessor.class);
        crawlerService = new CrawlerService(storageInterface, crawler);
        transaction = Mockito.mock(Transaction.class);
        dataBaseConnectorConfig = Mockito.mock(DataBaseConnectorConfig.class);
        databaseEnvironment = Mockito.mock(Environment.class);
        processManager = Mockito.mock(ProcessManager.class);

        Mockito.when(storageInterface.getDataBaseConnectorConfig()).thenReturn(dataBaseConnectorConfig);
        Mockito.when(dataBaseConnectorConfig.getDatabaseEnvironment()).thenReturn(databaseEnvironment);
        Mockito.when(databaseEnvironment.beginTransaction(Matchers.any(), Matchers.any())).thenReturn(transaction);
        Mockito.when(crawler.getMaxDocumentSize()).thenReturn(100);
        crawlDataStructure = Mockito.mock(CrawlDataStructure.class);
        Mockito.when(crawler.getCrawlDataStructure()).thenReturn(crawlDataStructure);
        hashSet = Mockito.mock(HashSet.class);
        HashSet<String> hashSet = new HashSet<>();
        hashSet.add(null);
        Mockito.when(crawlDataStructure.getVisitedHashDocumentSet()).thenReturn(hashSet);
        URLDataManager urlDataManager = Mockito.mock(URLDataManager.class);
        Mockito.when(storageInterface.getUrlDataManager()).thenReturn(urlDataManager);

    }


    @Test
    public void testExecutionException() {

        crawlerService.setProcessManager(processManager);
        Mockito.doThrow(TestException.class).when(processManager).processRequest(Matchers.any());
        CrawlServiceResponse crawlServiceResponse =
                crawlerService.processRequest(new ReadyQueueInstance(new URLInfo("http://www.google.com")));
        Assert.assertFalse(crawlServiceResponse.isSuccess());
    }


    @Test
    public void testBadDocument() {

        crawlerService.setProcessManager(processManager);
        CrawlServiceResponse crawlServiceResponse =
                crawlerService.processRequest(new ReadyQueueInstance(new URLInfo("http://www.google.com")));
        Assert.assertTrue(crawlServiceResponse.isSuccess());
    }

    @Test
    public void testProcessDocumentTest() {
        ReadyQueueInstance readyQueueInstance = new ReadyQueueInstance(new URLInfo("http://www.google.com"));

        URLMetaInformation urlMetaInformation = Mockito.mock(URLMetaInformation.class);

        ProcessContext processContext = new ProcessContext(readyQueueInstance.getUrlInfo(), transaction, 10);

        processContext.setGetInfo(urlMetaInformation);

        Mockito.when(urlMetaInformation.getContentType()).thenReturn("text/html");

        processContext.setMd5Hash("####");
        Mockito.when(documentParser.enrichLinks(Matchers.anyString(), Matchers.anyString()))
                .thenReturn(Arrays.asList(new URLInfo("http://www.facebook.com")));
        crawlerService.setDocumentParser(documentParser);
        CrawlServiceResponse crawlServiceResponse = crawlerService.processDocument(readyQueueInstance, processContext);
        Assert.assertEquals(crawlServiceResponse.getReadyQueueInstanceList().get(0),
                new ReadyQueueInstance(new URLInfo("http://www.facebook.com")));
    }

    @Test
    public void testProcessXMLDocumentTest() {
        ReadyQueueInstance readyQueueInstance = new ReadyQueueInstance(new URLInfo("http://www.google.com"));

        URLMetaInformation urlMetaInformation = Mockito.mock(URLMetaInformation.class);

        ProcessContext processContext = new ProcessContext(readyQueueInstance.getUrlInfo(), transaction, 10);

        processContext.setGetInfo(urlMetaInformation);

        Mockito.when(urlMetaInformation.getContentType()).thenReturn("text/xml");

        processContext.setMd5Hash("####");
        crawlerService.setDocumentParser(documentParser);
        CrawlServiceResponse crawlServiceResponse = crawlerService.processDocument(readyQueueInstance, processContext);
        Assert.assertEquals(crawlServiceResponse.getReadyQueueInstanceList().size(), 0);
    }


}
