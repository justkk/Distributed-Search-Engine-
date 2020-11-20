package edu.upenn.cis.cis455.crawler.handlersHW1;

import edu.upenn.cis.cis455.handlers.Route;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URLDecoder;

public class RegisterHandlerHW1 implements Route {

    private Logger logger = LogManager.getLogger(RegisterHandlerHW1.class);

    private StorageInterfaceImpl db;

    public RegisterHandlerHW1(StorageInterfaceImpl db) {
        this.db = db;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {

        String username = request.queryParams("username");
        String password = request.queryParams("password");
        String firstName = request.queryParams("firstname");
        String lastName = request.queryParams("lastname");

        logger.debug("User details");
        logger.debug("Username: " + username);
        logger.debug("Password: " + username);
        logger.debug("FirstName: " + username);
        logger.debug("LastName: " + username);

        if(username == null || "".equals(username)) {
            logger.debug("redirecting because of invalid required");
            response.redirect("/register.html?message=" + "username cant be empty");
            return null;
        }
        if(password == null) {
            logger.debug("redirecting because of invalid required");
            response.redirect("/register.html?message=" + "password cant be empty");
            return null;
        }
        if(firstName == null) {
            logger.debug("redirecting because of invalid required");
            response.redirect("/register.html?message=" + "firstName cant be empty");
            return null;
        }
        if(lastName == null) {
            logger.debug("redirecting because of invalid required");
            response.redirect("/register.html?message=" + "lastName cant be empty");
            return null;
        }

        logger.debug("inserting user record in database");

        username = URLDecoder.decode(username);
        password = URLDecoder.decode(password);
        firstName = URLDecoder.decode(firstName);
        lastName = URLDecoder.decode(lastName);

        int id = db.addUser(username, password, firstName, lastName);

        if(id == -1) {
            logger.debug("user already exists");
            response.redirect("/register.html?message=" + "user already exists");
            return null;
        }

        logger.debug("user added successfully");

        return "User added successfully";
    }
}
