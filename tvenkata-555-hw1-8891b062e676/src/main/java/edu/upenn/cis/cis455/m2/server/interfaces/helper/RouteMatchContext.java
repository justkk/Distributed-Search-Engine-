package edu.upenn.cis.cis455.m2.server.interfaces.helper;

import edu.upenn.cis.cis455.handlers.Route;
import edu.upenn.cis.cis455.m1.server.http.RequestTypeEnum;

public class RouteMatchContext {

    private String route;
    private String matchingRoute;
    private RequestTypeEnum requestTypeEnum;
    private Route routeHandler;
    private String acceptType;

    public RouteMatchContext(String route, String matchingRoute, RequestTypeEnum requestTypeEnum,
                             Route routeHandler, String acceptType) {
        this.route = route;
        this.matchingRoute = matchingRoute;
        this.requestTypeEnum = requestTypeEnum;
        this.routeHandler = routeHandler;
        this.acceptType = acceptType;
    }

    public String getRoute() {
        return route;
    }

    public void setRoute(String route) {
        this.route = route;
    }

    public String getMatchingRoute() {
        return matchingRoute;
    }

    public void setMatchingRoute(String matchingRoute) {
        this.matchingRoute = matchingRoute;
    }

    public RequestTypeEnum getRequestTypeEnum() {
        return requestTypeEnum;
    }

    public void setRequestTypeEnum(RequestTypeEnum requestTypeEnum) {
        this.requestTypeEnum = requestTypeEnum;
    }

    public Route getRouteHandler() {
        return routeHandler;
    }

    public void setRouteHandler(Route routeHandler) {
        this.routeHandler = routeHandler;
    }

    public String getAcceptType() {
        return acceptType;
    }

    public void setAcceptType(String acceptType) {
        this.acceptType = acceptType;
    }
}
