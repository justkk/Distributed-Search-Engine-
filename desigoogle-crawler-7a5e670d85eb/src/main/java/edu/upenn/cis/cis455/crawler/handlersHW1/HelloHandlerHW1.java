package edu.upenn.cis.cis455.crawler.handlersHW1;

import edu.upenn.cis.cis455.WebServiceController;
import edu.upenn.cis.cis455.handlers.Route;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import edu.upenn.cis.cis455.model.User;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HelloHandlerHW1 implements Route {

    private StorageInterfaceImpl db;
    private Logger logger = LogManager.getLogger(HelloHandlerHW1.class);

    public HelloHandlerHW1(StorageInterfaceImpl db) {
        this.db = db;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {

        if(request.session() == null) {
            return "";
        }

        String username = (String) request.session(true).attribute("user");
        logger.debug("Login Request for user " + username);
        if(username == null) {
            request.session().invalidate();
            //logger.error("null User name, returning 400");
            return "";
        }

        logger.debug("Fetching user details from database for " + username);

        User user = db.getUserInfoManager().getUserFromUserName(username);

        if(user == null) {
            logger.debug("user details don't exist in the table for " + username);
            request.session().invalidate();
            WebServiceController.halt(400,"login again");
            return "";
        }

        String body =  "<html><body>" + "Hello " + user.getFirstName() + " " + user.getLastName() +"</body></html>";
        //response.type("text/html");
        //response.body(body);

        logger.debug("Returning information from hello handler");
        logger.debug(body);

        return body;
    }
}
