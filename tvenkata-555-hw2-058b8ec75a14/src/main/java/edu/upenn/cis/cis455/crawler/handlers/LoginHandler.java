package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.storage.StorageInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.*;

public class LoginHandler implements Route {

    private Logger logger = LogManager.getLogger(LoginHandler.class);
    StorageInterface db;

    public LoginHandler(StorageInterface db) {
        this.db = db;
    }

    @Override
    public String handle(Request req, Response resp) throws HaltException {
        String user = req.queryParams("username");
        String pass = req.queryParams("password");

        logger.debug("User Name: " + user);
        logger.debug("Password " + pass);

        System.err.println("Login request for " + user + " and " + pass);
        if (db.getSessionForUser(user, pass)) {
            logger.debug("Login request success");
            System.err.println("Logged in!");
            logger.debug("setting the session attributes");
            Session session = req.session();
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
