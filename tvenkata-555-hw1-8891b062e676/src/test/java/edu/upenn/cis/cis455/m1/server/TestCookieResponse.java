package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.ServiceFactory;
import edu.upenn.cis.cis455.TestHelper;
import edu.upenn.cis.cis455.m1.server.implementations.HttpRequest;
import edu.upenn.cis.cis455.m1.server.implementations.HttpResponse;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.util.HttpResponseBuilder;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestCookieResponse {

    String sampleGetRequest =
            "GET /a/b/hello.htm?q=x&v=12%200 HTTP/1.1\r\n" +
                    "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
                    "Host: www.cis.upenn.edu\r\n" +
                    "Accept-Language: en-us\r\n" +
                    "Accept-Encoding: gzip, deflate\r\n" +
                    "Cookie: name1=value1;name2=value2;name3=value3\r\n" +
                    "Connection: closed\r\n\r\n";

    String expectedResponse = "HTTP/1.1 200 OK\r\n" +
            "Set-Cookie:name=value;path=/;";



    @Test
    public void testRequestParsing() throws IOException {

        HttpResponse response = new HttpResponse();
        response.cookie("name", "value", 10);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                sampleGetRequest,
                byteArrayOutputStream);
        Request request = ServiceFactory.createRequest(s,  null, false, null, null );
        assertTrue(HttpResponseBuilder.getStatusAndHeaderContent(request, response).startsWith(expectedResponse));
    }
}
