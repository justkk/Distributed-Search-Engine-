package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.TestHelper;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestSend {

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
                    "Connection: Keep-Alive\r\n\r\n";

    @Test
    public void testSendHead() throws IOException {

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                sampleGetRequest,
                byteArrayOutputStream);

        Request request = mock(Request.class);
        Response response = mock(Response.class);

        when(request.host()).thenReturn("");
        when(request.protocol()).thenReturn("HTTP/1.1");
        when(request.uri()).thenReturn("/index.html");
        when(request.requestMethod()).thenReturn("HEAD");
        when(response.body()).thenReturn(null);
        when(response.status()).thenReturn(200);
        when(response.getHeaders()).thenReturn("");

        HttpIoHandler.sendResponse(s, request, response);
        String result = byteArrayOutputStream.toString("UTF-8").replace("\r", "");
        System.out.println(result);

        assertTrue(result.startsWith("HTTP/1.1 200 OK"));
    }


}
