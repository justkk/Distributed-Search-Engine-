package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.TestHelper;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;

public class TestFileGETRequestContent {

    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
    }

    String sampleGetRequest =
            "GET /index.html HTTP/1.1\r\n" +
                    "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
                    "Host: www.cis.upenn.edu\r\n" +
                    "Accept-Language: en-us\r\n" +
                    "Accept-Encoding: gzip, deflate\r\n" +
                    "Cookie: name1=value1;name2=value2;name3=value3\r\n" +
                    "Connection: closed\r\n\r\n";

    String sampleGetFailureRequest =
            "GET /../index.html HTTP/1.1\r\n" +
                    "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
                    "Host: www.cis.upenn.edu\r\n" +
                    "Accept-Language: en-us\r\n" +
                    "Accept-Encoding: gzip, deflate\r\n" +
                    "Cookie: name1=value1;name2=value2;name3=value3\r\n" +
                    "Connection: closed\r\n\r\n";


    @Test
    public void testFileRequestContent() throws IOException, URISyntaxException {

        URL url = this.getClass().getClassLoader().getResource(".");
        String baseDirectory = new File(url.toURI()).getAbsolutePath();

        String result = TestHelper.getResult(sampleGetRequest, baseDirectory);
        String[] lines = result.split("\n");
        assertEquals("HTTP/1.1 200 OK", lines[0]);
        System.out.println(result);
    }

    @Test
    public void testFileRequestAccess() throws IOException, URISyntaxException {

        URL url = this.getClass().getClassLoader().getResource("folder");
        String baseDirectory = new File(url.toURI()).getAbsolutePath();
        String result = TestHelper.getResult(sampleGetFailureRequest, baseDirectory);
        String[] lines = result.split("\n");
        assertEquals( "HTTP/1.1 403 Forbidden", lines[0]);
        System.out.println(result);

    }

}
