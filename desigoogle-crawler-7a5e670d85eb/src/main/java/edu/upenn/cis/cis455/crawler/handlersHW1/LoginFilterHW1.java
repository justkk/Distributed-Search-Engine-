package edu.upenn.cis.cis455.crawler.handlersHW1;

import edu.upenn.cis.cis455.handlers.Filter;
import edu.upenn.cis.cis455.m1.server.interfaces.Request;
import edu.upenn.cis.cis455.m1.server.interfaces.Response;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;



public class LoginFilterHW1 implements Filter {
    private Logger logger = LogManager.getLogger(LoginFilterHW1.class);


    public LoginFilterHW1(StorageInterface db) {
    }


    @Override
    public void handle(Request hw1Request, Response hw1Response) throws Exception {

        edu.upenn.cis.cis455.m2.server.interfaces.Request req = (edu.upenn.cis.cis455.m2.server.interfaces.Request) hw1Request;
        edu.upenn.cis.cis455.m2.server.interfaces.Response response = (edu.upenn.cis.cis455.m2.server.interfaces.Response) hw1Response;

        if (!req.pathInfo().equals("/login-form.html") &&
                !req.pathInfo().equals("/login") &&
                !req.pathInfo().equals("/register") &&
                !req.pathInfo().equals("/register.html") &&
                !(req.pathInfo().equals("/shutdown"))
        ) {
            logger.debug("Request is NOT login/registration");
            if (req.session(false) == null) {
//                logger.debug
                //System.err.println("Not logged in - redirecting!");
                logger.debug("not logged in, redirecting to /login-form.html");
                response.redirect("/login-form.html");
            } else {
//                logger.debug
                //System.err.println("Logged in!");
                logger.debug("user is logged in " +  req.session().attribute("user"));
                req.attribute("user", req.session().attribute("user"));
            }

        } else {
//            logger.debug
            logger.debug("Request is for Login/Register form");
            System.err.println("Request is LOGIN FORM");
        }

    }
}
