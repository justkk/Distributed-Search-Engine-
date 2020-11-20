package edu.upenn.cis.cis455.m1.server.http.models;

import edu.upenn.cis.cis455.m1.server.http.RequestTypeEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * Default skeleton for a Route based entries.
 */

public abstract class RouteMapEntry {

    private String routePath;
    private String acceptType;
    protected RequestTypeEnum requestTypeEnum;

    public RouteMapEntry(String routePath, String acceptType, RequestTypeEnum requestTypeEnum) {
        this.routePath = routePath;
        this.acceptType = acceptType;
        this.requestTypeEnum = requestTypeEnum;
    }

    public RouteMapEntry(String routePath) {
        this.routePath = routePath;
        this.acceptType = "*/*";
    }

    public String getRoutePath() {
        return routePath;
    }

    public void setRoutePath(String routePath) {
        this.routePath = routePath;
    }

    public String getAcceptType() {
        return acceptType;
    }


    public boolean matchPath(String path) {


        if (this.getRoutePath().endsWith("*") || (this.getRoutePath().endsWith("/") && path.endsWith("/"))
                || (!this.getRoutePath().endsWith("/") && !path.endsWith("/"))) {


            if (this.getRoutePath().equals(path)) {
                return true;
            } else {
                List<String> currentPathList = routeSplitter(this.getRoutePath());
                List<String> givenPathList = routeSplitter(path);

                int currentPathSize = currentPathList.size();
                int givenPathSize = givenPathList.size();

                if(!this.getRoutePath().endsWith("*") && givenPathSize!=currentPathSize) {
                    return false;
                }

                if (this.getRoutePath().endsWith("*") && givenPathSize == currentPathSize - 1
                && path.endsWith("/")) {
                    givenPathList.add("*");
                    givenPathList.add("*");
                    givenPathSize += 2;
                }
                if (currentPathSize <= givenPathSize) {

                    for (int index = 0; index < currentPathSize; index++) {

                        String currentPathEntry = currentPathList.get(index);
                        String givenPathEntry = givenPathList.get(index);
                        if (index == currentPathSize - 1 && isWildCard(currentPathEntry)
                                && this.getRoutePath().endsWith("*")) {
                            return true;
                        }
                        if (!isParam(currentPathEntry) && !currentPathEntry.equals(givenPathEntry) && !isWildCard(currentPathEntry)) {
                            return false;
                        }
                    }
                    return true;
                } else {
                    return false;
                }
            }
        }
        return false;
    }

    public Map<String, String> getRouteParams(String requestPath) {

        List<String> currentPathList = routeSplitter(this.getRoutePath());
        List<String> givenPathList = routeSplitter(requestPath);
        Map<String, String> params = new HashMap<>();

        int currentPathSize = currentPathList.size();
        int givenPathSize = givenPathList.size();
        if (this.getRoutePath().endsWith("*") && givenPathSize == currentPathSize - 1
        && requestPath.endsWith("/")) {
            givenPathList.add("*");
            givenPathList.add("*");
            givenPathSize += 2;
        }

        if (currentPathSize <= givenPathSize) {

            for (int index = 0; index < currentPathSize; index++) {

                String currentPathEntry = currentPathList.get(index);
                String givenPathEntry = givenPathList.get(index);

                if (isParam(currentPathEntry)) {
                    String paramName = getParamName(currentPathEntry);
                    String value = givenPathEntry;
                    params.put(paramName, value);
                }
            }

        }

        return params;
    }

    public RequestTypeEnum getRequestTypeEnum() {
        return requestTypeEnum;
    }


    private List<String> routeSplitter(String route) {

        String[] pathArray = route.split("/");
        List<String> path = new ArrayList<>();
        for (String currentString : pathArray) {
            if (currentString.length() > 0) {
                path.add(currentString);
            }
        }
        return path;
    }

    private static boolean isParam(String routeSegment) {
        return routeSegment.startsWith(":");
    }

    private static String getParamName(String routeSegment) {
        return routeSegment.toLowerCase();
    }

    private static boolean isWildCard(String routeSegment) {
        return routeSegment.equals("*");
    }

    public boolean matches(RequestTypeEnum requestTypeEnum, String path) {
        if (this.getRequestTypeEnum() == requestTypeEnum) {
            return this.matchPath(path);
        }
        return false;
    }

    public static void main(String[] args) {

        RouteMapEntry routeMapEntry = new RouteHandlerMapEntry("/hello/:name", RequestTypeEnum.GET,
                null);

        System.out.println(routeMapEntry.matchPath("/hello/temp/"));

    }
}

