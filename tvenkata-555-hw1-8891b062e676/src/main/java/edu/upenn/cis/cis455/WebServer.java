package edu.upenn.cis.cis455;

import edu.upenn.cis.cis455.handlers.Filter;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WebServer {

    /**
     *
     * Main class to test the server functionality.
     *
     *  Current Implementation has sample routes.
     */

    final static Logger logger = LogManager.getLogger(WebServer.class);

    public static void main(String[] args) {
        org.apache.logging.log4j.core.config.Configurator.setLevel("edu.upenn.cis.cis455", Level.DEBUG);
        
        // TODO: make sure you parse *BOTH* command line arguments properly

        Integer port = null;
        String staticFolderLocation = null;

        if(args!=null) {
            if(args.length == 2) {
                port = Integer.valueOf(args[0]);
                staticFolderLocation = args[1];
            } else if(args.length == 1) {
                staticFolderLocation = args[0];
            }
        }

        // TODO: launch your server daemon
        WebServiceController.ipAddress("0.0.0.0");

        if(port!=null) {
            WebServiceController.port(port);
        }

        if (staticFolderLocation!=null) {
            WebServiceController.staticFileLocation(staticFolderLocation);
        }



//        WebServiceController.get("/login/", ((request, response) -> {
//            request.session(true);
//            request.session().attribute("username", "nikhil");
//            response.redirect("/hello/");
//            return null;
//        }));
//
//        WebServiceController.get("/hello/", ((request, response) -> {
//           if(request.session() == null) {
//               response.redirect("/login/");
//               return null;
//           }
//
//           response.body("<html><body> Hi " + request.session().attribute("username").toString()
//                   + "</body></html>");
//           response.header("content-type", "text/html");
//            return null;
//        }));
//
//        WebServiceController.get("/logout/", ((request, response) -> {
//            if(request.session() == null) {
//                response.redirect("/login/");
//                return null;
//            }
//
//            request.session().invalidate();
//            response.body("<html><body> logout success " + "</body></html>");
//            response.header("content-type", "text/html");
//            return null;
//        }));
//
//
        WebServiceController.post("/urlParams/", ((request, response) -> {
            response.body("<html><body> Sent :" + request.queryParams() + "</body></html>");
            response.header("content-type", "text/html");
            return null;
        }));

        WebServiceController.before("/urlParams/", "POST", new Filter() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                System.out.println("Filter1 Success");
            }
        });

        WebServiceController.before("/urlParams/", "GET", new Filter() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                System.out.println("FilterGET Failure");
            }
        });

        WebServiceController.before("/urlParams/*", "POST", new Filter() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                System.out.println("Filter2 Success");
            }
        });

        WebServiceController.get("/hello", (request, result) -> "Hello 555");

// Cookie Test 1
        WebServiceController.get("/testCookie1", (request, response) -> {
            String body = "<HTML><BODY><h3>Cookie Test 1</h3>";

// Set TestCookie1.
            response.cookie("TestCookie1", "1");

            body += "Added cookie (TestCookie,1) to response.";
            response.type("text/html");
            response.body(body);
            return response.body();
        });

// Before filter for all paths
        WebServiceController.before((request, response) -> {
            request.attribute("attribute1", "everyone");
        });

// Before filter for a specific path
        WebServiceController.before("/testFilter1", "GET", (request, response) -> {
            request.attribute("attribute2", "onlyTestFilter1");
        });

// Before filter that throws a halt exception if no FilterCookie=??
        WebServiceController.before("/testFilter1", "GET", (request, response) -> {

            if (!request.cookies().keySet().contains("FilterCookie")) {
                WebServiceController.halt(401, "You are not allowed!");
            } else {
                request.attribute("passed", "beforeFilter");
            }
        });



//        WebServiceController.get("/example1", (request, response) -> {
//
//            // Meaningless method calls.
//            response.cookie("ex", "1");
//            request.session(true).attribute("attr", "val");
//
//            String body = "Example 1.";
//            response.type("text/html");
//            response.body(body);
//            return response.body();
//        });
//
//        WebServiceController.post("/example2", (request, response) -> {
//            request.queryParams();
//
//            String body = "Example 2";
//            response.type("text/html");
//            response.body(body);
//            return response.body();
//        });
//
//        WebServiceController.options("/example3", (request, result) -> "Options Example");
//
//        WebServiceController.before((request, response) -> {
//            request.attribute("example", "route");
//        });
//
//        WebServiceController.before("/example5", "GET", (request, response) -> {
//            if (request.cookies().keySet().size() > 0) {
//                WebServiceController.halt(401, "This is an example!");
//            }
//        });
//
//        WebServiceController.after("/example6", "GET", (request, response) -> {
//            if (!request.body().contains("Example:")) {
//                WebServiceController.halt(501, "Example 6!");
//            }
//        });







        WebServiceController.threadPool(200);
        WebServiceController.awaitInitialization();

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        logger.info("Main thread exit");

    }

}
