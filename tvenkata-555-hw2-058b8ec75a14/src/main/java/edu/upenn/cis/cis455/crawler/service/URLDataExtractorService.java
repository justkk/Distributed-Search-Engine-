package edu.upenn.cis.cis455.crawler.service;

import edu.upenn.cis.cis455.ConstantsHW2;
import edu.upenn.cis.cis455.crawler.info.URLInfo;
import edu.upenn.cis.cis455.model.representationModels.URLResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class URLDataExtractorService {


    private final String HTTP = "http";
    private final String HTTPS = "https";
    int size = 100000000;

    private Logger logger = LogManager.getLogger(URLDataExtractorService.class);

    public URLDataExtractorService() {
    }

    public URLDataExtractorService(int size) {
        this.size = size;
    }

    public URLResponse getUrlData(URL url, String method, Map<String, String> headers) {

        if (url == null) {
            return null;
        }

        if (url.getProtocol().equals(HTTP)) {
            return getHttpUrlData(url, method, headers);
        } else if (url.getProtocol().equals(HTTPS)) {
            return getHttpsUrlData(url, method, headers);
        }
        return null;
    }

    public URLResponse getUrlInfoData(URLInfo urlInfo, String method, Map<String, String> headers) {

        URL url = null;
        try {
            if (urlInfo.isSecure()) {
                url = new URL(HTTPS, urlInfo.getHostName(), urlInfo.getPortNo(), urlInfo.getFilePath());
            } else {
                url = new URL(HTTP, urlInfo.getHostName(), urlInfo.getPortNo(), urlInfo.getFilePath());
            }
        } catch (MalformedURLException e) {
            logger.debug("BAD URL");
        }

        return getUrlData(url, method, headers);
    }


    private URLResponse getHttpUrlData(URL url, String method, Map<String, String> headers) {

        try {
            logger.debug("Fetching information for " + url.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            return getUrlResponse(url, method, headers, connection);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private URLResponse getUrlResponse(URL url, String method, Map<String, String> headers, HttpURLConnection connection) throws IOException {
        connection.setRequestMethod(method);
        connection.setConnectTimeout(5000);
        boolean redirectFlag = false;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if(entry.getKey().equals("####RedirectFlag####")) {
                redirectFlag = true;
                continue;
            }
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
        connection.setReadTimeout(ConstantsHW2.getInstance().getHttpConnectionTimeOut());
        connection.setConnectTimeout(ConstantsHW2.getInstance().getHttpConnectionTimeOut());
        connection.setInstanceFollowRedirects(redirectFlag);
        connection.connect();
        String data = readDataFromConnectionInputStream(connection.getInputStream());
        int statusCode = connection.getResponseCode();
        String contentType = connection.getContentType();
        int contentLength = connection.getContentLength();
        long lastModifiedTime = connection.getLastModified();

        if(method.equals("GET") && contentLength == -1) {
            contentLength = data.getBytes("UTF-8").length;
        }

        logger.debug("request " + url.toString() + " status code " + statusCode);
        logger.debug("request " + url.toString() + " contentType " + contentType);
        logger.debug("request " + url.toString() + " contentLength " + contentLength);
        logger.debug("request " + url.toString() + " lastModifiedTime " + lastModifiedTime);
        logger.debug("request " + url.toString() + " data " + data);


        return new URLResponse(statusCode, data, contentType, contentLength, lastModifiedTime,
                modifyHeaderMaps(connection.getHeaderFields()));
    }

    private URLResponse getHttpsUrlData(URL url, String method, Map<String, String> headers) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            return getUrlResponse(url, method, headers, connection);
//            connection.setRequestMethod(method);
//            for (Map.Entry<String, String> entry : headers.entrySet()) {
//                connection.setRequestProperty(entry.getKey(), entry.getValue());
//            }
//            connection.setReadTimeout(Constants.getInstance().getHttpsConnectionTimeOut());
//            connection.setConnectTimeout(Constants.getInstance().getHttpsConnectionTimeOut());
//            connection.setInstanceFollowRedirects(false);
//            connection.connect();
//            String data = readDataFromConnectionInputStream(connection.getInputStream());
//            int statusCode = connection.getResponseCode();
//            String contentType = connection.getContentType();
//            int contentLength = connection.getContentLength();
//            long lastModifiedTime = connection.getLastModified();
//
//            logger.debug("request " + url.toString() + " status code " + statusCode);
//            logger.debug("request " + url.toString() + " contentType " + contentType);
//            logger.debug("request " + url.toString() + " contentLength " + contentLength);
//            logger.debug("request " + url.toString() + " lastModifiedTime " + lastModifiedTime);
//            logger.debug("request " + url.toString() + " data " + data);
//
//            return new URLResponse(statusCode, data, contentType, contentLength, lastModifiedTime,
//                    modifyHeaderMaps(connection.getHeaderFields()));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String readDataFromConnectionInputStream(InputStream inputStream) throws IOException {

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder inputStreamData = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            inputStreamData.append(line);
            inputStreamData.append("\n");
            if(inputStreamData.toString().getBytes("UTF-8").length > size) {
                break;
            }

        }
        return inputStreamData.toString();
    }

    public Map<String, List<String>> modifyHeaderMaps(Map<String, List<String>> headerMaps) {

        Map<String, List<String>> newMap = new HashMap<>();
        if(headerMaps.keySet() == null) {
            return newMap;
        }
        for(String key : headerMaps.keySet()) {
            if(key == null) {
                continue;
            }
            newMap.put(key.toLowerCase(), headerMaps.get(key));
        }
        return newMap;
    }

    public static void main(String[] args) throws MalformedURLException {
        URLDataExtractorService urlDataExtractorService = new URLDataExtractorService();
        URL url = new URL("https", "web.iiit.ac.in", 443, "/~rashmivilas.tonge/");
        Map<String, String> headers = new HashMap<>();
        SimpleDateFormat formatter1 = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz");
        formatter1.setTimeZone(TimeZone.getTimeZone("GMT"));
        headers.put("If-Modified-Since", formatter1.format(new Date()));
        URLResponse urlResponse = urlDataExtractorService.getUrlData(url, "HEAD", headers);
        //System.out.println("Done");


    }


}
