package edu.upenn.cis.cis455.m1.server.http;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.server.interfaces.HttpRequestHandler;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/***
 * Gate way handler for any http request.
 * Check the request details and call specific handler.
 *
 */

public class MethodHandlerExecutor implements HttpRequestHandler {

    Map<RequestTypeEnum, HttpRequestHandler> requestHandlerMap;
    HttpRequestHandler defaultRequestHandler;
    MatchConfiguration matchConfiguration;

    public MethodHandlerExecutor(HttpRequestHandler defaultRequestHandler, MatchConfiguration matchConfiguration) {
        requestHandlerMap = new HashMap<>();
        this.defaultRequestHandler = defaultRequestHandler;
        this.matchConfiguration = matchConfiguration;
    }

    public void setRequestHandler(RequestTypeEnum requestTypeEnum, HttpRequestHandler httpRequestHandler) {
        requestHandlerMap.put(requestTypeEnum, httpRequestHandler);
    }

    public HttpRequestHandler getRequestHandler(RequestTypeEnum requestTypeEnum) {
        if (!requestHandlerMap.containsKey(requestTypeEnum)) {
            throw new HaltException(HttpServletResponse.SC_NOT_FOUND, "Route Not Found");
        }
        return requestHandlerMap.get(requestTypeEnum);
    }


    @Override
    public void handle(Request request, Response response) throws HaltException {
        if (request == null || request.headers("method") == null) {
            return;
        }
        try{
        matchConfiguration.getRoutesFilterConfiguration().applyBeforeFilters(request, response);

        RequestTypeEnum requestTypeEnum = RequestTypeEnum.getEnumFromString(request.headers("method"));
        if (requestTypeEnum == null) {
            return;
        }

            if (requestHandlerMap.containsKey(requestTypeEnum)) {
                requestHandlerMap.get(requestTypeEnum).handle(request, response);
            } else {
                defaultRequestHandler.handle(request, response);
            }
        } catch (HaltException e) { matchConfiguration.getRoutesFilterConfiguration().applyAfterFilters(request, response);throw e; }

        matchConfiguration.getRoutesFilterConfiguration().applyAfterFilters(request, response);

    }
}
