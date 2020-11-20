package edu.upenn.cis.cis455.m1.server.implementations;

import edu.upenn.cis.cis455.exceptions.ServerHaltException;
import edu.upenn.cis.cis455.handlers.Filter;
import edu.upenn.cis.cis455.handlers.Route;
import edu.upenn.cis.cis455.handlers.impl.ControlPanelRouteHandler;
import edu.upenn.cis.cis455.handlers.impl.HttpProtocolFilter;
import edu.upenn.cis.cis455.handlers.impl.PostRequestDataFilter;
import edu.upenn.cis.cis455.handlers.impl.ServerShutDownHandler;
import edu.upenn.cis.cis455.m1.server.enums.AcceptTypeEnum;
import edu.upenn.cis.cis455.m1.server.genericImpl.models.ServerStatus;
import edu.upenn.cis.cis455.m1.server.HttpServer;
import edu.upenn.cis.cis455.m1.server.http.RequestTypeEnum;
import edu.upenn.cis.cis455.m1.server.http.RoutesFilterConfiguration;
import edu.upenn.cis.cis455.m1.server.http.RoutesHandlerConfiguration;
import edu.upenn.cis.cis455.m1.server.http.models.HttpServerConfig;
import edu.upenn.cis.cis455.m2.server.interfaces.Session;
import edu.upenn.cis.cis455.m2.server.interfaces.WebService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/***
 *
 * Implementation fo Webservice.
 * It has server, server config, routes information and filer information.
 *
 */

public class HttpWebServiceImpl extends WebService {

    final static Logger logger = LogManager.getLogger(HttpWebServiceImpl.class);

    private HttpServer httpServer;

    private HttpServerConfig serverConfig;

    private RoutesHandlerConfiguration routesHandlerConfiguration;

    private RoutesFilterConfiguration routesFilterConfiguration;


    public ServerStatus getHttpServerStatus() {
        return httpServer.getServerStatus();
    }



    /***
     * Initialized the parameters to new instances
     */

    public HttpWebServiceImpl() {

        serverConfig = new HttpServerConfig();
        routesHandlerConfiguration = new RoutesHandlerConfiguration();
        routesFilterConfiguration = new RoutesFilterConfiguration();

    }

    @Override
    public void post(String path, Route route) {
        routesHandlerConfiguration.addRouteHandler(path, route, RequestTypeEnum.POST);
    }

    @Override
    public void put(String path, Route route) {
        routesHandlerConfiguration.addRouteHandler(path, route, RequestTypeEnum.PUT);
    }

    @Override
    public void delete(String path, Route route) {
        routesHandlerConfiguration.addRouteHandler(path, route, RequestTypeEnum.DELETE);
    }

    @Override
    public void head(String path, Route route) {
        routesHandlerConfiguration.addRouteHandler(path, route, RequestTypeEnum.HEAD);
    }

    @Override
    public void options(String path, Route route) {
        routesHandlerConfiguration.addRouteHandler(path, route, RequestTypeEnum.OPTIONS);
    }

    @Override
    public void before(Filter filter) {
        routesFilterConfiguration.addBeforeFilters(filter);
    }

    @Override
    public void after(Filter filter) {
        routesFilterConfiguration.addAfterFilters(filter);
    }

    @Override
    public void before(String path, String acceptType, Filter filter) {
        routesFilterConfiguration.addBeforeRouteFilterMapEntry(path,
                acceptType, filter);

    }

    @Override
    public void after(String path, String acceptType, Filter filter) {
        routesFilterConfiguration.addAfterRouteFilterMapEntry(path,
                acceptType, filter);
    }

    /***
     * Start the server.
     * Set some default path handlers, /shutdown and /control.
     * Set default filter handlers.
     */

    @Override
    public void start() {

        try {

            this.httpServer = new HttpServer(serverConfig, this);
            logger.info(" setting default routes, shutdown and control");
            get("/shutdown", new ServerShutDownHandler(httpServer));
            get("/control", new ControlPanelRouteHandler(httpServer));
            logger.info(" setting default filters, HttpProtocolFilter");
            before(new HttpProtocolFilter());
            before("/*", "POST", new PostRequestDataFilter());
            httpServer.startServer();
        } catch (Exception e) {
            logger.error("Server start failed", e);
            throw new ServerHaltException(e);
        }

    }

    @Override
    public void stop() {
        httpServer.stopAndWait();
        httpServer = null;
    }

    @Override
    public void staticFileLocation(String directory) {
        Path absolutePath = Paths.get(directory).toAbsolutePath();
        serverConfig.setStaticFolderLocation(absolutePath.toString());
    }

    @Override
    public void get(String path, Route route) {
        routesHandlerConfiguration.addRouteHandler(path, route, RequestTypeEnum.GET);
    }

    @Override
    public void ipAddress(String ipAddress) {
        serverConfig.setIpAddress(ipAddress);
    }

    @Override
    public void port(int port) {
        serverConfig.setPort(port);
    }

    @Override
    public void threadPool(int threads) {
        serverConfig.setThreadCount(threads);
    }

    public RoutesHandlerConfiguration getRoutesHandlerConfiguration() {
        return routesHandlerConfiguration;
    }

    public RoutesFilterConfiguration getRoutesFilterConfiguration() {
        return routesFilterConfiguration;
    }


    public String createSession() {
        if(httpServer == null) {
            return null;
        }
        return httpServer.createSession();
    }

    /**
     * Looks up a session by ID and updates / returns it
     */
    public Session getSession(String id) {
        if(httpServer == null) {
            return null;
        }
        return httpServer.getSession(id);
    }

    public HttpServer getHttpServer() {
        return httpServer;
    }
}
