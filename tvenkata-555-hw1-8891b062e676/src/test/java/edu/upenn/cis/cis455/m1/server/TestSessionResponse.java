package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.ServiceFactory;
import edu.upenn.cis.cis455.TestHelper;
import edu.upenn.cis.cis455.WebServiceController;
import edu.upenn.cis.cis455.m1.server.implementations.HttpRequest;
import edu.upenn.cis.cis455.m1.server.implementations.HttpResponse;
import edu.upenn.cis.cis455.m1.server.implementations.HttpWebServiceImpl;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.util.HttpResponseBuilder;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertTrue;

public class TestSessionResponse {

    String sampleGetRequest =
            "GET /a/b/hello.htm?q=x&v=12%200 HTTP/1.1\r\n" +
                    "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
                    "Host: www.cis.upenn.edu\r\n" +
                    "Accept-Language: en-us\r\n" +
                    "Accept-Encoding: gzip, deflate\r\n" +
                    "Connection: closed\r\n\r\n";

    String expectedResponse = "HTTP/1.1 200 OK\r\n" +
            "Set-Cookie:name=value;path=/;";



    @Test
    public void testRequestParsing() throws IOException, URISyntaxException {

        WebServiceController.ipAddress("0.0.0.0");
        WebServiceController.port(9010);
        URL url = this.getClass().getClassLoader().getResource(".");
        String baseDirectory = new File(url.toURI()).getAbsolutePath();
        WebServiceController.staticFileLocation(baseDirectory);
        WebServiceController.awaitInitialization();

        HttpResponse response = new HttpResponse();
        response.cookie("name", "value", 10);
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                sampleGetRequest,
                byteArrayOutputStream);
        HttpRequest request = (HttpRequest) ServiceFactory.createRequest(s, null, false, null, null);
        HttpWebServiceImpl httpWebService = (HttpWebServiceImpl) ServiceFactory.getServerInstance();
        request.setServerInstance(httpWebService.getHttpServer());
        request.session(true);
        String sessionId = request.session().id();
        System.out.println(sessionId);
        System.out.println(HttpResponseBuilder.getStatusAndHeaderContent(request, response));
        //assertTrue(HttpResponseBuilder.getStatusAndHeaderContent(request, response).startsWith(expectedResponse));
        WebServiceController.stop();
    }

}
