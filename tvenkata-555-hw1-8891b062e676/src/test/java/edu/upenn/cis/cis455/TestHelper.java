package edu.upenn.cis.cis455;

import edu.upenn.cis.cis455.m1.server.HttpServer;
import edu.upenn.cis.cis455.m1.server.handlers.HttpGetRequestHandler;
import edu.upenn.cis.cis455.m1.server.http.*;
import edu.upenn.cis.cis455.m1.server.interfaces.HttpRequestHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TestHelper {
    
    public static Socket getMockSocket(String socketContent, ByteArrayOutputStream output) throws IOException {

        Socket s = mock(Socket.class);

        byte[] arr = socketContent.getBytes();

        final ByteArrayInputStream bis = new ByteArrayInputStream(arr);

        when(s.getInputStream()).thenReturn(bis);
        when(s.getOutputStream()).thenReturn(output);
        when(s.getLocalAddress()).thenReturn(InetAddress.getLocalHost());
        when(s.getRemoteSocketAddress()).thenReturn(InetSocketAddress.createUnresolved("host", 8080));
        
        return s;
    }

    public static String getResult(String request, String folderLocation) throws IOException {


        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Socket s = TestHelper.getMockSocket(
                request,
                byteArrayOutputStream);

        RoutesFilterConfiguration routesFilterConfiguration = mock(RoutesFilterConfiguration.class);
        RoutesHandlerConfiguration routesHandlerConfiguration = mock(RoutesHandlerConfiguration.class);
        StaticFileConfiguration staticFileConfiguration = new StaticFileConfiguration(folderLocation);

        MatchConfiguration matchConfiguration = new MatchConfiguration(routesFilterConfiguration,
                routesHandlerConfiguration, staticFileConfiguration);

        HttpRequestHandler httpRequestHandler = new HttpGetRequestHandler(matchConfiguration);

        HttpServer httpServer = mock(HttpServer.class);

        HttpTaskWrapper httpTaskWrapper = new HttpTaskWrapper(s, httpRequestHandler, httpServer);
        try {
            httpTaskWrapper.run();
        } catch (Exception e) {
            /**/
        }

        String result = byteArrayOutputStream.toString("UTF-8").replace("\r", "");
        return result;
    }
}
