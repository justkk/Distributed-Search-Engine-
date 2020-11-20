package edu.upenn.cis.cis455.m2.server.interfaces.helper;

import edu.upenn.cis.cis455.ServiceFactory;
import edu.upenn.cis.cis455.m1.server.implementations.HttpRequest;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import edu.upenn.cis.cis455.m2.server.interfaces.Session;

public class CookieAndSessionHandler {

    /**
     * Utility to fetch session id from cookies and enrich request.
     * @param request
     */
    public static void enrichRequestWithSession(HttpRequest request) {
        if (request.getCookies().containsKey("JSESSIONID")) {
            String sessionId = request.getCookies().get("JSESSIONID");
            Session session = request.getServerInstance().getSession(sessionId);
            request.setSession(session);
        }
    }

}
