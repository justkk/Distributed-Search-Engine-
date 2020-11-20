package edu.upenn.cis.cis455.m1.server.http;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.exceptions.IllegalResponseStateException;
import edu.upenn.cis.cis455.handlers.Route;
import edu.upenn.cis.cis455.m1.server.http.models.RouteContext;
import edu.upenn.cis.cis455.m1.server.http.models.RouteHandlerMapEntry;
import edu.upenn.cis.cis455.m1.server.http.models.RouteMapEntry;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.helper.RouteMatcherHelper;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/***
 * Stores the Route Handlers and will hold the code for route fetching handler based on request path.
 */

public class RoutesHandlerConfiguration {

    private Map<RequestTypeEnum, Map<String, RouteHandlerMapEntry>> routeMap;
    private RouteMatcherHelper<RouteHandlerMapEntry> routeMatcherHelper;

    public RoutesHandlerConfiguration() {
        routeMap = new HashMap<>();
        routeMatcherHelper = new RouteMatcherHelper<>();
        Arrays.stream(RequestTypeEnum.values()).forEach(enumEntry -> {
            routeMap.put(enumEntry, new HashMap<>());
        });
    }


    public void addRouteHandler(String path, Route handlerFunction, RequestTypeEnum requestType) {

        Map<String, RouteHandlerMapEntry> entryMap = routeMap.get(requestType);
        RouteHandlerMapEntry routeMapEntry = new RouteHandlerMapEntry(path, requestType, handlerFunction);
        entryMap.put(path, routeMapEntry);
    }

    public RouteHandlerMapEntry getRouteHandlerMapEntry(Request request) {

        String path = request.pathInfo();
        if (!routeMap.containsKey(RequestTypeEnum.getEnumFromString(request.headers("method")))) {
            throw new HaltException(HttpServletResponse.SC_NOT_FOUND, "Route Not Found");
        }
        Map<String, RouteHandlerMapEntry> entry = routeMap.get(RequestTypeEnum.getEnumFromString(
                request.headers("method")));

        String acceptType = request.headers("accept");

        if (acceptType == null) {
            acceptType = "*/*";
        }

        RouteMapEntry routeMatch = routeMatcherHelper.findRouteForRequestedRoute(path, RequestTypeEnum.getEnumFromString(
                request.headers("method")), acceptType,
                new ArrayList<>(entry.values()));

        if(routeMatch == null) {
            return null;
        }

        return (RouteHandlerMapEntry) routeMatch;
    }

    public boolean hasRoute(RouteContext routeContext) {
        try {
            RouteHandlerMapEntry routeHandlerMapEntry = getRouteHandlerMapEntry(routeContext.getRequest());
            if(routeHandlerMapEntry == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void handleRoute(RouteContext routeContext) {

        RouteHandlerMapEntry routeHandlerMapEntry = getRouteHandlerMapEntry(routeContext.getRequest());

        /*
            Enrich Ro:wute Params ....
         */

        try {

            if(routeHandlerMapEntry == null) {
                throw new HaltException(HttpServletResponse.SC_NOT_FOUND, "Route not configured");
            }

            Object response = routeHandlerMapEntry.getRouteHandler().handle(routeContext.getRequest(), routeContext.getResponse());
            if (response != null) {
                routeContext.getResponse().body(response.toString());
                routeContext.getResponse().header("content-type", "text/html");
            }
        }
        catch (IllegalResponseStateException e) {
            System.out.println(e);
        }
        catch (Exception e) {
            if (e instanceof HaltException) {
                throw (HaltException) e;
            }
            throw new HaltException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }

    }


//    public List<RouteHandlerMapEntry> getRouteMapEntry(String requestType, String path, String acceptType){
//
//        AcceptTypeEnum acceptTypeEnum = AcceptTypeEnum.getContentTypeFromString(acceptType);
//        RequestTypeEnum requestTypeEnum = RequestTypeEnum.getEnumFromString(requestType);
//
//        List<RouteHandlerMapEntry> allEntries = new ArrayList<>(routeMap.get(requestTypeEnum).values());
//        List<RouteHandlerMapEntry> matchedEntries = new ArrayList<>();
//
////        for(RouteHandlerMapEntry entry: allEntries) {
////
////            if(acceptTypeEnum!=null) {
////                String bestMatch =
////            }
////
////            if (acceptType != null) {
////                String bestMatch = MimeParse.bestMatch(Arrays.asList(routeEntry.acceptedType), acceptType);
////                if (this.routeWithGivenAcceptType(bestMatch)) {
////                    matchSet.add(new RouteMatch(httpMethod, routeEntry.target, routeEntry.path, path, acceptType));
////                }
////            } else {
////                matchSet.add(new RouteMatch(httpMethod, routeEntry.target, routeEntry.path, path, acceptType));
////            }
////
////        }
//
//    }

}
