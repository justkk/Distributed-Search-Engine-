package edu.upenn.cis.cis455.crawler.handlersHW1;

import edu.upenn.cis.cis455.WebServiceController;
import edu.upenn.cis.cis455.exceptions.HaltException;
import edu.upenn.cis.cis455.handlers.Route;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import edu.upenn.cis.cis455.m2.server.interfaces.Session;
import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URLDecoder;

public class LoginHandlerHW1 implements Route {

    private Logger logger = LogManager.getLogger(LoginHandlerHW1.class);
    StorageInterface db;
    private URLDecoder urlDecoder = new URLDecoder();

    public LoginHandlerHW1(StorageInterface db) {
        this.db = db;
    }

    @Override
    public String handle(Request req, Response resp) throws HaltException {

        String user = req.queryParams("username");
        String pass = req.queryParams("password");

        if(user == null || pass == null) {
            WebServiceController.halt(400, "invalid request");
        }


        user = URLDecoder.decode(req.queryParams("username"));
        pass = URLDecoder.decode(req.queryParams("password"));


        logger.debug("User Name: " + user);
        logger.debug("Password " + pass);

        System.err.println("Login request for " + user + " and " + pass);
        if (db.getSessionForUser(user, pass)) {
            logger.debug("Login request success");
            System.err.println("Logged in!");
            logger.debug("setting the session attributes");
            Session session = req.session(true);
            session.maxInactiveInterval(300);
            session.attribute("user", user);
            session.attribute("password", pass);

            logger.debug("redirecting to /index.html");
            resp.redirect("/index.html");
        } else {
            System.err.println("Invalid credentials");
            logger.debug("invalid credentials");
            resp.redirect("/login-form.html?message=Invalid credentials");
        }


        return "";
    }
}
