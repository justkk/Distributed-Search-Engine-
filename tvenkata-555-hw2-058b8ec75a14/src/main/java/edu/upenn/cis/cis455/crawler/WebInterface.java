package edu.upenn.cis.cis455.crawler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

import edu.upenn.cis.cis455.crawler.handlers.HelloHandler;
import edu.upenn.cis.cis455.crawler.handlers.LoginFilter;
import edu.upenn.cis.cis455.crawler.handlers.RegisterHandler;
import edu.upenn.cis.cis455.model.User;
import edu.upenn.cis.cis455.model.UserChannelInfo;
import edu.upenn.cis.cis455.model.index.DocChannelIndex;
import edu.upenn.cis.cis455.model.subscription.SubscriptionKey;
import edu.upenn.cis.cis455.model.subscription.SubscriptionValue;
import edu.upenn.cis.cis455.model.urlDataInfo.DocOnlyInfo;
import edu.upenn.cis.cis455.model.urlDataInfo.URLDataInfo;
import edu.upenn.cis.cis455.storage.StorageFactory;
import edu.upenn.cis.cis455.storage.StorageInterface;
import edu.upenn.cis.cis455.crawler.handlers.LoginHandler;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import edu.upenn.cis.cis455.xpathengine.models.XPathQuery;

public class WebInterface {
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
        
        port(8080);
        StorageInterface database = StorageFactory.getDatabaseInstance(args[0]);
        
        LoginFilter testIfLoggedIn = new LoginFilter(database);
        
        if (args.length == 2) {
            staticFiles.externalLocation(args[1]);
            staticFileLocation(args[1]);
        }
            
        before("/*", "POST", testIfLoggedIn);
        // TODO:  add /register, /logout, /index.html, /, /lookup
        post("/register", new RegisterHandler((StorageInterfaceImpl) database));
        post("/login", new LoginHandler(database));

        get("/", new HelloHandler((StorageInterfaceImpl) database));
        get("/index.html", new HelloHandler((StorageInterfaceImpl) database));

        get("/logout", (req, res) -> {
            req.session().invalidate();
            return "logout Success";
        });

        get("/lookup", ((request, response) -> {
            String url = request.queryParams("url");
            StorageInterfaceImpl db = (StorageInterfaceImpl) database;
            String content = db.getDocument(url);
            String contentType = db.getContentType(url);
            if(content == null) {
                halt(404, "url not found");
            }
            if("HTML".equals(contentType)) {
                return content;
            }
            else{
                response.type("text/xml");
                //response.body(content);
                return content;
//                return "<html style=\"width:100%; height:100%\"><body style=\"width:100%; height:100%\">" +
//                        "<textarea style=\"width:100%; height:100%\" readonly>" + content + "</textarea></body></html>";
            }
        }));


        get("/create/:name", ((request, response) -> {
            String xpath = request.queryParams("xpath");
            String channelName = request.params(":name");
            if(xpath == null || channelName == null) {
                halt(404, "bad request");
            }
            XPathQuery xPathQuery = XPathQuery.getXPathQuery("temp", xpath);
            if
            (xPathQuery == null) {
                halt(400, "Bad XPath");
            }
            StorageInterfaceImpl db = (StorageInterfaceImpl) database;
            String username = request.session().attribute("user");
            User user = db.getUserInfoManager().getUserFromUserName(username);
            if(user == null) {
                halt(404, "bad user");
            }
            Integer id = user.getUserId();
            UserChannelInfo userChannelInfo = db.getUserChannelDataManager().insert(new UserChannelInfo(id,
                    channelName, xpath));
            if(userChannelInfo == null) {
                halt(404, "channel already exists");
            }
            return "channel added";
        }));

        get("/getChannelList", ((request, response) -> {

            String username = request.session().attribute("user");
            StorageInterfaceImpl db = (StorageInterfaceImpl) database;
            User user = db.getUserInfoManager().getUserFromUserName(username);
            List<UserChannelInfo> userChannelInfoList = db.getUserChannelDataManager().getUserChannelInfoList(user.getUserId());
            String st = "";
            for(UserChannelInfo userChannelInfo : userChannelInfoList) {
                st += userChannelInfo.getChannelName();
                st += "\n";
            }
            return st;
        }));

        get("/show", ((request, response) -> {

            String channelName = request.queryParams("channel");
            if(channelName == null || channelName.equals("")) {
                return halt(400, "bad channel name");
            }
            StorageInterfaceImpl db = (StorageInterfaceImpl) database;
            UserChannelInfo userChannelInfo = db.getUserChannelDataManager().getUserChannelInfo(channelName);
            if(userChannelInfo == null) {
                halt(404, "channel not found");
            }
            User user = db.getUserInfoManager().getUserFromId(userChannelInfo.getUserId());
            String userName = "";
            if(user!=null) {
                userName = user.getUserName();
            }
            StringBuilder htmlDocument = new StringBuilder();

            htmlDocument.append("<html><body>");
            // Channel Info
            htmlDocument.append("<div class=\"channelheader\" style=\"width:100%\">");
            htmlDocument.append("Channel name: " + userChannelInfo.getChannelName());
            htmlDocument.append("<br/>");
            htmlDocument.append("created by: " + userName);
            htmlDocument.append("</div>");

            htmlDocument.append("<br/>");
            htmlDocument.append("<br/>");
            htmlDocument.append("<br/>");
            htmlDocument.append("<br/>");



            //
            List<Integer> docChannelIndices = db.getIndexManager().getDocIdForChannel(userChannelInfo.getChannelName());
            List<DocOnlyInfo> docOnlyInfoList = new ArrayList<>();

            docChannelIndices.stream().forEach(entry -> {
                try {
                    DocOnlyInfo docOnlyInfo = db.getUrlDataManager().getDocOnlyInfoPrimaryIndex().get(entry);
                    if(docOnlyInfo!=null) {
                        docOnlyInfoList.add(docOnlyInfo);
                    }
                } catch (Exception e) {

                }
            });

            String pattern = "yyyy-MM-dd'T'hh:mm:ss";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

            for(DocOnlyInfo docOnlyInfo : docOnlyInfoList) {
                List<URLDataInfo> urlDataInfoList = db.getUrlDataManager().getURLDataInfoForDocument(docOnlyInfo.getId());
                for(URLDataInfo urlDataInfo : urlDataInfoList) {
                    htmlDocument.append("Crawled on: " +
                            simpleDateFormat.format(urlDataInfo.getURLOnlyInfo().getLastModifiedTime()));
                    htmlDocument.append("<br/>");
                    htmlDocument.append("Location: " + urlDataInfo.getURLOnlyInfo().getUrlDataInfoKey().getLocation());
                    htmlDocument.append("<br/>");
                    htmlDocument.append("<div class=\"document\">");
                    htmlDocument.append(urlDataInfo.getData());
                    htmlDocument.append("</div>");
                    htmlDocument.append("<br/>");
                    htmlDocument.append("<br/>");
                }
            }

            htmlDocument.append("</body></html>");
            return htmlDocument.toString();

        }));
        get("/subscribe", ((request, response) -> {

            String channelName = request.queryParams("channel");
            if(channelName == null || channelName.equals("")) {
                return halt(400, "bad channel name");
            }
            StorageInterfaceImpl db = (StorageInterfaceImpl) database;

            String userName = (String) request.attribute("user");
            User user = db.getUserInfoManager().getUserFromUserName(userName);
            Integer userId = user.getUserId();

            //List<SubscriptionValue> subscriptionValues = db.getSubcriptionManger().getSubList(userId);

            SubscriptionKey subscriptionKey = new SubscriptionKey(userId, channelName);
            db.getSubcriptionManger().insert(subscriptionKey);

            return "subscription success";

        }));

        get("/getSubscriptionList", (request, response) -> {
            StorageInterfaceImpl db = (StorageInterfaceImpl) database;

            String userName = (String) request.attribute("user");

            if(userName == null) {
                return halt(404, "bad request");
            }

            User user = db.getUserInfoManager().getUserFromUserName(userName);

            List<SubscriptionValue> subscriptionValues = db.getSubcriptionManger().getSubList(user.getUserId());

            StringBuilder htmlDocument = new StringBuilder();
            htmlDocument.append("<html><body>");

            for (SubscriptionValue subscriptionValue : subscriptionValues) {

                htmlDocument.append("<p>");
                htmlDocument.append(subscriptionValue.getChannelName());
                htmlDocument.append("</p>");
            }
            htmlDocument.append("</body>");
            htmlDocument.append("</html>");

            return htmlDocument.toString();

        });

        awaitInitialization();
    }
}
