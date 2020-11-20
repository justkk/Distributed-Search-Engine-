package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.ServiceFactory;
import edu.upenn.cis.cis455.TestHelper;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestCookieParsing {

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
        assertEquals(request.cookie("name1"), "value1");
        assertEquals(request.cookie("name2"), "value2");
        assertEquals(request.cookie("name3"), "value3");
    }
}
