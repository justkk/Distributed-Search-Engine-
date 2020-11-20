package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.ServiceFactory;
import edu.upenn.cis.cis455.TestHelper;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestRequestParsing {

    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
    }

    String sampleGetRequest =
            "GET /a/b/hello.htm?q=x&v=12%200 HTTP/1.1\r\n" +
                    "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
                    "Host: www.cis.upenn.edu\r\n" +
                    "Accept-Language: en-us\r\n" +
                    "Accept-Encoding: gzip, deflate\r\n" +
                    "Cookie: name1=value1;name2=value2;name3=value3\r\n" +
                    "Connection: closed\r\n\r\n";

    @Test
    public void testRequestParsing() throws IOException {

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                sampleGetRequest,
                byteArrayOutputStream);

        Request request = ServiceFactory.createRequest(s,  null, false, null, null );

        assertTrue(request.protocol().equals("HTTP/1.1"));
        assertTrue(request.userAgent().equals("Mozilla/4.0 (compatible; MSIE5.01; Windows NT)"));
        assertTrue(request.host().equals("www.cis.upenn.edu"));
        assertFalse(request.persistentConnection());
        assertTrue(request.uri().equals("/a/b/hello.htm"));
        assertTrue(request.url().equals("http://www.cis.upenn.edu/a/b/hello.htm"));
        assertTrue(request.queryString().equals("q=x&v=12%200"));
        assertTrue(request.requestMethod().equals("GET"));
        assertTrue(request.port() == 80);
        assertTrue(request.pathInfo().equals("/a/b/hello.htm"));
        assertTrue(request.headers("accept-language").equals("en-us"));

    }

}
