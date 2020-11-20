package edu.upenn.cis.cis455.crawler;

import edu.upenn.cis.cis455.Constants;
import edu.upenn.cis.cis455.ServiceFactory;
import edu.upenn.cis.cis455.WebServiceController;
import edu.upenn.cis.cis455.crawler.handlersHW1.HelloHandlerHW1;
import edu.upenn.cis.cis455.crawler.handlersHW1.LoginFilterHW1;
import edu.upenn.cis.cis455.crawler.handlersHW1.LoginHandlerHW1;
import edu.upenn.cis.cis455.crawler.handlersHW1.RegisterHandlerHW1;
import edu.upenn.cis.cis455.m1.server.implementations.HttpWebServiceImpl;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class WebInterfaceHw1 {

    public static void main(String args[]) {
        if (args.length < 1 || args.length > 2) {
            System.out.println("Syntax: WebInterface {path} {root}");
            System.exit(1);
        }

        if (!Files.exists(Paths.get(args[0]))) {
            try {
                Files.createDirectory(Paths.get(args[0]));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                System.exit(1);
            }
        }

        WebServiceController.port(8080);
        StorageInterface database = StorageFactory.getDatabaseInstance(args[0]);

        LoginFilterHW1 testIfLoggedIn = new LoginFilterHW1(database);

        if (args.length == 2) {
            File directory = new File(args[1]);
            try {
                String absolute = directory.getCanonicalPath(); // may throw IOException
                WebServiceController.staticFileLocation(absolute);
            } catch (IOException e) {
                System.out.println("web directory path doesn't exist");
                System.exit(1);
            }
        }

        WebServiceController.before("/*", "POST", testIfLoggedIn);
        WebServiceController.before("/*", "GET", testIfLoggedIn);


        // TODO:  add /register, /logout, /index.html, /, /lookup
        WebServiceController.post("/register", new RegisterHandlerHW1((StorageInterfaceImpl) database));
        WebServiceController.post("/login", new LoginHandlerHW1(database));

        WebServiceController.get("/", new HelloHandlerHW1((StorageInterfaceImpl) database));
        WebServiceController.get("/index.html", new HelloHandlerHW1((StorageInterfaceImpl) database));

        WebServiceController.get("/logout", (req, res) -> {
            req.session().invalidate();
            return "logout Success";
        });

        Constants.getInstance().setSESSION_AGE(300);
        Constants.getInstance().setCHUNKED_ENCODING(false);

        WebServiceController.get("/lookup", ((request, response) -> {
            String url = request.queryParams("url");
            StorageInterfaceImpl db = (StorageInterfaceImpl) database;
            String content = db.getDocument(url);
            String contentType = db.getContentType(url);
            if (content == null) {
                WebServiceController.halt(404, "url not found");
            }
            if ("HTML".equals(contentType)) {
                return content;
            } else {
                response.body(content);
                response.type("text/xml");
                return null;
//                return "<html style=\"width:100%; height:100%\"><body style=\"width:100%; height:100%\">" +
//                        "<textarea style=\"width:100%; height:100%\" readonly>" + content + "</textarea></body></html>";
            }
        }));


        WebServiceController.awaitInitialization();

        HttpWebServiceImpl httpWebService = (HttpWebServiceImpl) ServiceFactory.getServerInstance();

        while (true) {
            if (httpWebService.getHttpServer().isActive()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                database.close();
                break;
            }
        }
    }
}
