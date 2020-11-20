package edu.upenn.cis.cis455.m1.server.http;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.handlers.Filter;
import edu.upenn.cis.cis455.m1.server.http.models.RouteFilterMapEntry;
import edu.upenn.cis.cis455.m1.server.http.models.RouteMapEntry;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import edu.upenn.cis.cis455.m2.server.interfaces.helper.RouteMatcherHelper;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/***
 * Stores the Filters and will hold the code for route fetching filters based on request path.
 */

public class RoutesFilterConfiguration {

    private List<Filter> beforeFilters;
    private List<Filter> afterFilters;

    private List<RouteFilterMapEntry> beforeRouteMap;
    private List<RouteFilterMapEntry> afterRouteMap;

    private RouteMatcherHelper<RouteFilterMapEntry> routeMatcherHelper;

    public RoutesFilterConfiguration() {
        beforeRouteMap = new LinkedList<>();
        afterRouteMap = new LinkedList<>();

        beforeFilters = new ArrayList<>();
        afterFilters = new ArrayList<>();

        routeMatcherHelper = new RouteMatcherHelper<>();
    }

    public void addBeforeFilters(Filter filter) {
        beforeFilters.add(filter);
    }

    public void addAfterFilters(Filter filter) {
        afterFilters.add(filter);
    }

    public void addBeforeRouteFilterMapEntry(String path, String requestType, Filter filter) {

        beforeRouteMap.add(new RouteFilterMapEntry(path,
                RequestTypeEnum.getEnumFromString(requestType), filter));
    }

    public void addAfterRouteFilterMapEntry(String path, String requestType, Filter filter) {
        afterRouteMap.add(new RouteFilterMapEntry(path,
                RequestTypeEnum.getEnumFromString(requestType), filter));
    }

    public void applyBeforeFilters(Request request, Response response) {
        applyFilter(beforeFilters, beforeRouteMap, request, response);
    }

    public void applyAfterFilters(Request request, Response response) {
        applyFilter(afterFilters, afterRouteMap, request, response);
    }

    public void applyFilter(List<Filter> defaultFilterList,
                            List<RouteFilterMapEntry> routeMap, Request request, Response response) {

        List<Filter> allFilters = new ArrayList<>();

        allFilters.addAll(defaultFilterList);

        List<RouteFilterMapEntry> routeMapEntries =  routeMatcherHelper.findMatchesForRequestRoute(
                request.uri(), RequestTypeEnum.getEnumFromString(request.headers("method")), routeMap);

        if(routeMapEntries!=null) {
            routeMapEntries.forEach(routeFilterMapEntry -> {
                allFilters.add(routeFilterMapEntry.getFilterHandler());
            });
        }

        try {
            for (Filter filter : allFilters) {
                filter.handle(request, response);
            }
        } catch (Exception e) {
            if (e instanceof HaltException) {
                throw (HaltException) e;
            }
            throw new HaltException(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }

    }

}
