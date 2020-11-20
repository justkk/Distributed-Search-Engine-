package edu.upenn.cis.cis455.m1.server.http.models;

import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;

public class RouteContext {

    private Request request;
    private Response response;
    private RouteHandlerMapEntry routeHandlerMapEntry;
    private Object target;
    boolean valid;

    public RouteContext(Request request, Response response) {
        this.request = request;
        this.response = response;
        this.valid = true;
    }


    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }
}
