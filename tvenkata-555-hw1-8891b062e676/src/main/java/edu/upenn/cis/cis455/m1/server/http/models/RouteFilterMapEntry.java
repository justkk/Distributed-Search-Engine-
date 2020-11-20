package edu.upenn.cis.cis455.m1.server.http.models;

import edu.upenn.cis.cis455.handlers.Filter;
import edu.upenn.cis.cis455.handlers.Route;
import edu.upenn.cis.cis455.m1.server.enums.AcceptTypeEnum;
import edu.upenn.cis.cis455.m1.server.http.RequestTypeEnum;

/***
 * Entry for a Filter entry
 */
public class RouteFilterMapEntry extends RouteMapEntry {

    private Filter filterHandler;

    public RouteFilterMapEntry(String routePath, RequestTypeEnum requestTypeEnum, Filter filterHandler) {
        super(routePath, AcceptTypeEnum.WILDCARD.toString(), requestTypeEnum);
        this.filterHandler = filterHandler;
    }


    public Filter getFilterHandler() {
        return filterHandler;
    }
}
