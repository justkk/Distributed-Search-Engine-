package edu.upenn.cis.cis455.m1.server.http.models;

import edu.upenn.cis.cis455.handlers.Route;
import edu.upenn.cis.cis455.m1.server.enums.AcceptTypeEnum;
import edu.upenn.cis.cis455.m1.server.http.RequestTypeEnum;

import java.util.ArrayList;
import java.util.List;

/***
 * Entry for a route handler
 */

public class RouteHandlerMapEntry extends RouteMapEntry {

    private Route routeHandler;

    public RouteHandlerMapEntry(String routePath, RequestTypeEnum requestTypeEnum, Route routeHandler) {
        super(routePath, AcceptTypeEnum.WILDCARD.toString(), requestTypeEnum);
        this.routeHandler = routeHandler;
    }

    public Route getRouteHandler() {
        return routeHandler;
    }

}
