package edu.upenn.cis.cis455.m2.server.interfaces.helper;

import edu.upenn.cis.cis455.m1.server.http.RequestTypeEnum;
import edu.upenn.cis.cis455.m1.server.http.models.RouteMapEntry;
import edu.upenn.cis.cis455.util.AcceptTypeParse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RouteMatcherHelper<T extends RouteMapEntry> {


//    private List<RouteMatchContext> findMatchesForRequestRoute(RequestTypeEnum requestTypeEnum, String path, String acceptType, List<RouteHandlerMapEntry> mapEntryList) {
//
//        List<RouteMatchContext> matchingSet = new ArrayList<>();
//        List<RouteHandlerMapEntry> routeEntries = this.findMatchesForRequestRoute(requestTypeEnum, path, mapEntryList);
//        if(acceptType == null) {
//            routeEntries.stream().forEach(routeHandlerMapEntry -> {
//                matchingSet.add(new RouteMatchContext(path, routeHandlerMapEntry.getRoutePath(),
//                        routeHandlerMapEntry.getRequestTypeEnum(), routeHandlerMapEntry.getRouteHandler(),
//                        routeHandlerMapEntry.getAcceptType()));
//            });
//            return matchingSet;
//        }
//
//        for(RouteHandlerMapEntry entry: routeEntries) {
//            String bestMatch = AcceptTypeParse.findBestMatch(acceptType, Arrays.asList(entry.getAcceptType()));
//            if(!"".equals(bestMatch)) {
//                matchingSet.add(new RouteMatchContext(path, entry.getRoutePath(), entry.getRequestTypeEnum(),
//                        entry.getRouteHandler(), entry.getAcceptType()));
//            }
//        }
//
//        return matchingSet;
//    }

    public List<T> findMatchesForRequestRoute(String path,RequestTypeEnum requestTypeEnum, List<T> mapEntryList) {

        List<T> matchingSet = new ArrayList<>();

        for (T entry : mapEntryList) {
            if (entry.getRequestTypeEnum() == requestTypeEnum && entry.matchPath(path)) {
                matchingSet.add(entry);
            }
        }

        return matchingSet;
    }

    public T findRouteForRequestedRoute(String path, RequestTypeEnum requestTypeEnum,
                                        String acceptType, List<T> mapEntryList) {
        List<T> routeEntries = this.findMatchesForRequestRoute(path, requestTypeEnum, mapEntryList);
        T entry = this.findRouteWithGivenAcceptType(routeEntries, acceptType);
        return entry;
    }

    private T findRouteWithGivenAcceptType(List<T> matchedContexts,
                                           String acceptType) {

        if (acceptType != null && matchedContexts.size() > 0) {
            Map<String, T> acceptedMimeTypes = this.getAcceptedMimeTypes(matchedContexts);
            String bestMatch = AcceptTypeParse.findBestMatch(acceptType, new ArrayList<>(acceptedMimeTypes.keySet()));
            return !"".equals(bestMatch) ? acceptedMimeTypes.get(bestMatch) : null;

        } else {
            return matchedContexts.size() > 0 ? matchedContexts.get(0) : null;
        }

    }

    private Map<String, T> getAcceptedMimeTypes(List<T> routes) {
        Map<String, T> acceptedTypes = new HashMap<>();
        for (T routeEntry : routes) {
            if (!acceptedTypes.containsKey(routeEntry.getAcceptType())) {
                acceptedTypes.put(routeEntry.getAcceptType(), routeEntry);
            }
        }
        return acceptedTypes;
    }


}
