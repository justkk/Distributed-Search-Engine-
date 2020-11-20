package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.ServiceFactory;
import edu.upenn.cis.cis455.TestHelper;
import edu.upenn.cis.cis455.WebServiceController;
import edu.upenn.cis.cis455.m1.server.implementations.HttpRequest;
import edu.upenn.cis.cis455.m1.server.implementations.HttpResponse;
import edu.upenn.cis.cis455.m1.server.implementations.HttpWebServiceImpl;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import edu.upenn.cis.cis455.util.HttpResponseBuilder;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertTrue;


public class TestSessionParsing {

    String sampleGetRequest =
            "GET /shutdown HTTP/1.1\r\n" +
                    "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
                    "Host: www.cis.upenn.edu\r\n" +
                    "Accept-Language: en-us\r\n" +
                    "Accept-Encoding: gzip, deflate\r\n" +
                    "Connection: closed\r\n\r\n";
    String prefix = "HTTP/1.1 200 OK\r\n";

    @Test
    public void testSessionParsing() throws IOException, URISyntaxException {

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


        WebServiceController.ipAddress("0.0.0.0");
        WebServiceController.port(9010);
        URL url = this.getClass().getClassLoader().getResource(".");
        String baseDirectory = new File(url.toURI()).getAbsolutePath();
        WebServiceController.staticFileLocation(baseDirectory);
        WebServiceController.awaitInitialization();

        Socket s = TestHelper.getMockSocket(
                sampleGetRequest,
                byteArrayOutputStream);
        HttpRequest request = (HttpRequest) ServiceFactory.createRequest(s, null, false, null, null);
        HttpWebServiceImpl httpWebService = (HttpWebServiceImpl) ServiceFactory.getServerInstance();
        request.setServerInstance(httpWebService.getHttpServer());
        request.session(true);
        Response response = new HttpResponse();
        String testingPrefix = prefix + "Set-Cookie:JSESSIONID=" + request.session().id();
        assertTrue(HttpResponseBuilder.getStatusAndHeaderContent(request, response).startsWith(testingPrefix));
        WebServiceController.stop();

    }
}
