package edu.upenn.cis.cis455.handlers.impl;

import edu.upenn.cis.cis455.handlers.Route;
import edu.upenn.cis.cis455.m1.server.HttpServer;
import edu.upenn.cis.cis455.m1.server.http.models.HttpWorkerInfo;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import edu.upenn.cis.cis455.util.HttpParsing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/*
 ***
 * ControlPanelRouteHandler is the route handler for /control
 * It has server instance as a parameter.
 * It fetches the worker stats from server and returns puts them into a html format.
 */



public class ControlPanelRouteHandler implements Route {

    static final Logger logger = LogManager.getLogger(ControlPanelRouteHandler.class);

    private HttpServer httpServer;

    public ControlPanelRouteHandler(HttpServer httpServer) {
        this.httpServer = httpServer;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {

        logger.info("Fetching the server stats");
        String html = getHtmlContent(httpServer.getServerStats(), httpServer.getErrorLog());
        response.status(HttpServletResponse.SC_OK);
        response.body(html);
        response.type(HttpParsing.getMimeType(".html"));
        return null;
    }


    private String getHtmlContentForLog(List<HttpServer.LogEntry> logEntries) {
        String tableHead = "<tr><th>Index</th><th>Task Info</th><th>Error</th></tr>";
        StringBuilder tableContent = new StringBuilder();
        int index = 1;
        for (HttpServer.LogEntry entry : logEntries) {
            tableContent.append("<tr>");
            tableContent.append("<td>" + String.valueOf(index) + "</td>");
            tableContent.append("<td>" + mapToString(entry.getHttpTaskWrapper().getRequestInfo()) + "</td>");
            tableContent.append("<td>" + entry.getErrorMessage().toString() + "</td>");
            tableContent.append("</tr>");
            index += 1;
        }

        return "<table>" + tableHead + tableContent.toString() + "</table>";
    }

    private String getHtmlStats(Map<String, HttpWorkerInfo> httpWorkerInfoMap) {
        String tableHead = "<tr><th>Index</th><th>Worker id</th><th>Stats</th><th>Info</th></tr>";
        StringBuilder tableContent = new StringBuilder();
        int index = 1;
        for (Map.Entry<String, HttpWorkerInfo> entry : httpWorkerInfoMap.entrySet()) {
            tableContent.append("<tr>");
            tableContent.append("<td>" + String.valueOf(index) + "</td>");
            tableContent.append("<td>" + entry.getKey() + "</td>");
            tableContent.append("<td>" + entry.getValue().getState() + "</td>");
            tableContent.append("<td>" + mapToString(entry.getValue().getRequestInfo()) + "</td>");
            tableContent.append("</tr>");
            index += 1;
        }

        return "<table>" + tableHead + tableContent.toString() + "</table>";
    }


    private String getHtmlContent(Map<String, HttpWorkerInfo> httpWorkerInfoMap, List<HttpServer.LogEntry> logEntries) {

        String head = "<head></head>";

        String body = "<body><a href=\"/shutdown\">Shutdown</a>"+ getHtmlStats(httpWorkerInfoMap)
                + getHtmlContentForLog(logEntries) +"</body>";

        return "<html>" + head + body + "</html>";
    }

    private String mapToString(Map<String, String> stringMap) {

        final StringBuilder stringBuilder = new StringBuilder();

        stringMap.entrySet().stream().forEach(entry -> {
            stringBuilder.append(entry.getKey() + ":" + entry.getValue() + "\n");
        });

        return stringBuilder.toString();

    }
}
