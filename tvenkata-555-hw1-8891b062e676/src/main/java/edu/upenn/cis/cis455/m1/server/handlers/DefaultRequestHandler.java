package edu.upenn.cis.cis455.m1.server.handlers;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.server.http.MatchConfiguration;
import edu.upenn.cis.cis455.m1.server.http.models.RouteContext;
import edu.upenn.cis.cis455.m1.server.interfaces.HttpRequestHandler;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;

import javax.servlet.http.HttpServletResponse;

/***
 * A default request handler
 * It will contain the basic default look up and returns the response.
 */

public class DefaultRequestHandler implements HttpRequestHandler {

    protected MatchConfiguration matchConfiguration;

    public DefaultRequestHandler(MatchConfiguration matchConfiguration) {
        this.matchConfiguration = matchConfiguration;
    }

    @Override
    public void handle(Request request, Response response) throws HaltException {
        RouteContext routeContext = new RouteContext(request, response);
        matchConfiguration.getRoutesHandlerConfiguration().handleRoute(routeContext);
        //throw new HaltException(HttpServletResponse.SC_NOT_IMPLEMENTED);
    }
}
