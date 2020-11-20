package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

public class RegisterHandler implements Route {

    private Logger logger = LogManager.getLogger(RegisterHandler.class);

    private StorageInterfaceImpl db;

    public RegisterHandler(StorageInterfaceImpl db) {
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
        }
        if(password == null) {
            logger.debug("redirecting because of invalid required");
            response.redirect("/register.html?message=" + "password cant be empty");
        }
        if(firstName == null) {
            logger.debug("redirecting because of invalid required");
            response.redirect("/register.html?message=" + "firstName cant be empty");
        }
        if(lastName == null) {
            logger.debug("redirecting because of invalid required");
            response.redirect("/register.html?message=" + "lastName cant be empty");
        }

        logger.debug("inserting user record in database");

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
