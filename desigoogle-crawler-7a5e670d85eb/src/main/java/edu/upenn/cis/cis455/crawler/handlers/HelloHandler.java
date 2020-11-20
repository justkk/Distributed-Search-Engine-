package edu.upenn.cis.cis455.crawler.handlers;

import edu.upenn.cis.cis455.model.User;
import edu.upenn.cis.cis455.model.UserChannelInfo;
import edu.upenn.cis.cis455.model.subscription.SubscriptionValue;
import edu.upenn.cis.cis455.storage.StorageInterfaceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.util.List;

public class HelloHandler implements Route {
    private StorageInterfaceImpl db;
    private Logger logger = LogManager.getLogger(HelloHandler.class);

    public HelloHandler(StorageInterfaceImpl db) {
        this.db = db;
    }

    @Override
    public Object handle(Request request, Response response) throws Exception {

        String username = request.session().attribute("user");
        logger.debug("Login Request for user " + username);
        if(username == null) {
            request.session().invalidate();
            //logger.error("null User name, returning 400");
            Spark.halt(400,"login again");
            return "";
        }

        logger.debug("Fetching user details from database for " + username);

        User user = db.getUserInfoManager().getUserFromUserName(username);

        if(user == null) {
            logger.debug("user details don't exist in the table for " + username);
            request.session().invalidate();
            Spark.halt(400,"login again");
            return "";
        }

        StringBuilder htmlBuilder = new StringBuilder();

        htmlBuilder.append("<html><body>");
        htmlBuilder.append("<p>Hello " + user.getFirstName() + " " + user.getLastName() + "</p>");
        htmlBuilder.append("<hr/>");
        htmlBuilder.append("<h6>Create Channel</h6>");
        htmlBuilder.append("<form onsubmit=\"this.action = '/create/' + this.channelName.value;\" method=\"GET\">");
        htmlBuilder.append("Channel : <input type=\"text\" name=\"channelName\" />");
        htmlBuilder.append("<br/>");
        htmlBuilder.append("XPath : <input type=\"text\" name=\"xpath\" />");
        htmlBuilder.append("<br/>");
        htmlBuilder.append("<button type=\"submit\"> Submit </button>");
        htmlBuilder.append("</form>");
        htmlBuilder.append("<br/>");
        htmlBuilder.append("<hr/>");
        htmlBuilder.append("<h6> All Channels </h6>");
        htmlBuilder.append("<ul>");
        List<UserChannelInfo> userChannelInfoList = db.getUserChannelDataManager().getAllChannels();
        for(UserChannelInfo userChannelInfo : userChannelInfoList) {
            htmlBuilder.append("<li>");
            htmlBuilder.append("<p><a href=\"/show?channel=" + userChannelInfo.getChannelName() + "\">" + userChannelInfo.getChannelName() + "</a></p>");
            htmlBuilder.append("</li>");
        }

        htmlBuilder.append("</ul>");

        htmlBuilder.append("<hr/><br/>");
        htmlBuilder.append("<h6> Subscribed Channels </h6>");
        htmlBuilder.append("<ul>");
        List<SubscriptionValue> subscriptionValues = db.getSubcriptionManger().getSubList(user.getUserId());

        for(SubscriptionValue subscriptionValue : subscriptionValues) {
            htmlBuilder.append("<li>");
            htmlBuilder.append("<p><a href=\"/show?channel=" + subscriptionValue.getChannelName() + "\">" + subscriptionValue.getChannelName() + "</a></p>");
            htmlBuilder.append("</li>");
        }
        htmlBuilder.append("</ul>");


        htmlBuilder.append("<hr/><br/>");
        htmlBuilder.append("<h6> Subscribe to new Channels </h6>");
        htmlBuilder.append("<form onsubmit=\"this.action = '/subscribe';\" method=\"GET\">");
        htmlBuilder.append("Channel : <select name=\"channel\" >");

        for(UserChannelInfo userChannelInfo : userChannelInfoList) {
            htmlBuilder.append("<option value=\"" + userChannelInfo.getChannelName()  + "\">");
            htmlBuilder.append(userChannelInfo.getChannelName());
            htmlBuilder.append("</option>");
        }


        htmlBuilder.append("</select>");
        htmlBuilder.append("<button type=\"submit\"> Submit </button>");
        htmlBuilder.append("</form>");



        htmlBuilder.append("</body></html>");


        String body = htmlBuilder.toString();
        logger.debug("Returning information from hello handler");
        logger.debug(body);

        return body;
    }
}
