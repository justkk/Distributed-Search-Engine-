package edu.upenn.cis.cis455.handlers.impl;

import edu.upenn.cis.cis455.handlers.Filter;
import edu.upenn.cis.cis455.m1.server.http.RequestTypeEnum;
import edu.upenn.cis.cis455.m1.server.implementations.HttpRequest;
import edu.upenn.cis.cis455.m2.server.interfaces.Request;
import edu.upenn.cis.cis455.m2.server.interfaces.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PostRequestDataFilter implements Filter {
    @Override
    public void handle(Request request, Response response) throws Exception {

        if(request.headers("content-type")!=null &&
                "application/x-www-form-urlencoded".equals(request.headers("content-type"))) {

            HttpRequest httpRequest = (HttpRequest) request;
            String body = request.body();
            String[] params = body.split("&");

            Map<String, List<String>> queryParams = httpRequest.getQueryParams();

            for(String param : params) {

                String[] keyValues = param.split("=");
                String key = keyValues[0];
                String value = keyValues[1];
                if(!queryParams.containsKey(key)) {
                    queryParams.put(key, new ArrayList<>());
                }
                queryParams.get(key).add(value);
            }

        }

    }
}
