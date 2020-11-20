package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;


public class LoginFilter implements Filter {
    private Logger logger = LogManager.getLogger(LoginFilter.class);


    public LoginFilter(StorageInterface db) {
    }


    @Override
    public void handle(Request req, Response response) throws Exception {
        if (!req.pathInfo().equals("/login-form.html") &&
                !req.pathInfo().equals("/login") &&
                !req.pathInfo().equals("/register") &&
                !req.pathInfo().equals("/register.html") &&
                !req.pathInfo().equals("/shutdown")
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
