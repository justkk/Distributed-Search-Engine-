package edu.upenn.cis.cis455.m1.server;

import edu.upenn.cis.cis455.ServiceFactory;
import edu.upenn.cis.cis455.TestHelper;
import edu.upenn.cis.cis455.WebServiceController;
import edu.upenn.cis.cis455.handlers.impl.ServerShutDownHandler;
import edu.upenn.cis.cis455.m1.server.handlers.HttpGetRequestHandler;
import edu.upenn.cis.cis455.m1.server.http.*;
import edu.upenn.cis.cis455.m1.server.interfaces.HttpRequestHandler;
import org.apache.logging.log4j.Level;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

public class TestShutDown {

    @Before
    public void setUp() {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
    }

    String sampleGetRequest =
            "GET /shutdown HTTP/1.1\r\n" +
                    "User-Agent: Mozilla/4.0 (compatible; MSIE5.01; Windows NT)\r\n" +
                    "Host: www.cis.upenn.edu\r\n" +
                    "Accept-Language: en-us\r\n" +
                    "Accept-Encoding: gzip, deflate\r\n" +
                    "Cookie: name1=value1;name2=value2;name3=value3\r\n" +
                    "Connection: closed\r\n\r\n";

    @Test
    public void testShutDown() throws IOException, URISyntaxException {

        URL url = this.getClass().getClassLoader().getResource(".");
        String baseDirectory = new File(url.toURI()).getAbsolutePath();

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                sampleGetRequest,
                byteArrayOutputStream);

        HttpServer httpServer = mock(HttpServer.class);

        RoutesFilterConfiguration routesFilterConfiguration = mock(RoutesFilterConfiguration.class);
        RoutesHandlerConfiguration routesHandlerConfiguration = new RoutesHandlerConfiguration();
        StaticFileConfiguration staticFileConfiguration = new StaticFileConfiguration(baseDirectory);
        routesHandlerConfiguration.addRouteHandler("/shutdown", new ServerShutDownHandler(httpServer), RequestTypeEnum.GET);
        MatchConfiguration matchConfiguration = new MatchConfiguration(routesFilterConfiguration,
                routesHandlerConfiguration, staticFileConfiguration);
        HttpRequestHandler httpRequestHandler = new HttpGetRequestHandler(matchConfiguration);

        HttpTaskWrapper httpTaskWrapper = new HttpTaskWrapper(s, httpRequestHandler, httpServer);
        httpTaskWrapper.run();
        String result = byteArrayOutputStream.toString("UTF-8").replace("\r", "");
        String[] lines = result.split("\n");
        assertEquals("HTTP/1.1 200 OK", lines[0]);
        System.out.println(result);
    }

    @Test
    public void testCompleteShutDown() throws IOException, URISyntaxException {

        WebServiceController.ipAddress("0.0.0.0");
        WebServiceController.port(9010);
        URL url = this.getClass().getClassLoader().getResource(".");
        String baseDirectory = new File(url.toURI()).getAbsolutePath();
        WebServiceController.staticFileLocation(baseDirectory);
        WebServiceController.awaitInitialization();
        WebServiceController.stop();
    }


}
