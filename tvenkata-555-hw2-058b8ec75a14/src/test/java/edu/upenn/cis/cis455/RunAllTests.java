package edu.upenn.cis.cis455;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase
{
  public static Test suite() 
  {
    try {
      Class[]  testClasses = {
        /* TODO: Add the names of your unit test classes here */
              Class.forName("edu.upenn.cis.cis455.handlers.HelloHandlerTest"),
              Class.forName("edu.upenn.cis.cis455.handlers.LoginHandlerTest"),
              Class.forName("edu.upenn.cis.cis455.handlers.LoginFilterTest"),
              Class.forName("edu.upenn.cis.cis455.handlers.RegisterHandlerTest"),
              Class.forName("edu.upenn.cis.cis455.info.UrlInfoTest"),
              Class.forName("edu.upenn.cis.cis455.queue.ReadyQueueInstanceTest"),
              Class.forName("edu.upenn.cis.cis455.service.CrawlerServiceTest"),
              Class.forName("edu.upenn.cis.cis455.service.HostInfoExtractorServiceTest"),
              Class.forName("edu.upenn.cis.cis455.service.URLDataExtractorServiceTest"),
              Class.forName("edu.upenn.cis.cis455.service.URLInfoExtractorServiceTest"),
              Class.forName("edu.upenn.cis.cis455.storageTest.HostRobotInfoMangerTest"),
              Class.forName("edu.upenn.cis.cis455.storageTest.URLDataMangerTest"),
              Class.forName("edu.upenn.cis.cis455.utils.DocParserTest"),
              Class.forName("edu.upenn.cis.cis455.utils.RobotPathMatcherTest"),
              Class.forName("edu.upenn.cis.cis455.xmlMatching.XmlMatchingTest"),
              Class.forName("edu.upenn.cis.cis455.xmlMatching.XmlMatchingTest"),
              Class.forName("edu.upenn.cis.cis455.dryRun.DryRunTest")
      };   
      
      return new TestSuite(testClasses);
    } catch(Exception e){
      e.printStackTrace();
    } 
    
    return null;
  }
}
