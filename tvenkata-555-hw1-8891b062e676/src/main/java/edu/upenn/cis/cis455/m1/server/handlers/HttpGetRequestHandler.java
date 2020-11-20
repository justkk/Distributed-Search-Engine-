package edu.upenn.cis.cis455.m1.server.handlers;

import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.m1.server.http.MatchConfiguration;
import edu.upenn.cis.cis455.m1.server.http.models.RouteContext;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;

/**
 * Handlers specific to method types.
 * For get we need to fetch files.
 * If there are no files, then check the route map and process the request.
 */

public class HttpGetRequestHandler extends DefaultRequestHandler {


    public HttpGetRequestHandler(MatchConfiguration matchConfiguration) {
        super(matchConfiguration);
    }

    @Override
    public void handle(Request request, Response response) throws HaltException {

        HaltException staticFileError = null;
        RouteContext routeContext = new RouteContext(request, response);
        if (!matchConfiguration.getRoutesHandlerConfiguration().hasRoute(routeContext)) {
            matchConfiguration.getStaticFileConfiguration().fetchFile(request, response);
        } else {
            matchConfiguration.getRoutesHandlerConfiguration().handleRoute(routeContext);
        }
    }
}
