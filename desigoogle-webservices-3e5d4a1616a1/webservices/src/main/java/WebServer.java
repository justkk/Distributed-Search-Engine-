import com.google.gson.Gson;
import spark.Filter;
import spark.Spark;

import java.util.List;

public class WebServer {


    public static void main(String[] args) {

        Gson gson = new Gson();

        Spark.port(8088);
        final DbFetcherService dbFetcherService = new DbFetcherService();

        Spark.after((Filter) (request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, PUT, POST, DELETE, HEAD, OPTIONS");
        });


        Spark.get("/search", (request, response) -> {
            String query = request.queryParams("query");
            List<UiResponse> responseList = dbFetcherService.getResponseForQuery(query);
            return gson.toJson(responseList);
        });

    }


}
