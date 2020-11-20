package edu.upenn.cis455.mapreduce;

import java.util.HashMap;
import java.util.Map;

public class Constants {

    private String workerConfig = "[127.0.0.1:9000,127.0.0.1:9001,127.0.0.1:9002]";
    private int workersLength = 2;
    private String masterHost = "127.0.0.1:8888";

    private int mapperCountPerNode = 2;
    private int reduceCountPerNode = 2;
    private int printCountPerNode = 1;

    private int blockingQueueSize = 10;
    private String htmlString = "<html>\n" +
            "<head>\t\n" +
            "</head>\n" +
            "<body>\n" +
            "<p> SEAS login name : tvenkata </p>\n" +
            "\t<p> Status: ####status#### </p>\n" +
            "\t<form action=\"/defineJob\" method=\"POST\">\n" +
            "\t\t<table>\n" +
            "\t\t\t<tr>\n" +
            "\t\t\t\t<td>Input Directory</td>\n" +
            "\t\t\t\t<td><input type=\"text\" name=\"inputDirectory\" /></td>\n" +
            "\t\t\t</tr>\n" +
            "\t\t\t<tr>\n" +
            "\t\t\t\t<td>Output Directory</td>\n" +
            "\t\t\t\t<td><input type=\"text\" name=\"outputDirectory\" /></td>\n" +
            "\t\t\t</tr>\n" +
            "\t\t\t<tr>\n" +
            "\t\t\t\t<td>Job Class Name</td>\n" +
            "\t\t\t\t<td><input type=\"text\" name=\"jobClassName\" /></td>\n" +
            "\t\t\t</tr>\n" +
            "\t\t\t<tr>\n" +
            "\t\t\t\t<td>Job Name</td>\n" +
            "\t\t\t\t<td><input type=\"text\" name=\"jobname\" /></td>\n" +
            "\t\t\t</tr>\n" +
            "\t\t\t<tr>\n" +
            "\t\t\t\t<td>Map Executors Count</td>\n" +
            "\t\t\t\t<td><input type=\"text\" name=\"mapExecutors\" /></td>\n" +
            "\t\t\t</tr>\n" +
            "\t\t\t<tr>\n" +
            "\t\t\t\t<td>Reduce Executors Count</td>\n" +
            "\t\t\t\t<td><input type=\"text\" name=\"reduceExecutors\" /></td>\n" +
            "\t\t\t</tr>\n" +
            "\t\t\t\n" +
            "\t\t\t<tr>\n" +
            "\t\t\t\t<td><button type=\"submit\" ####disabled####>Start Job</button></td>\n" +
            "\t\t\t</tr>\n" +
            "\t\t</table>\n" +
            "\t</form>\n" +
            "\n" +
            "\n" +
            "\t<table>\n" +
            "\t\t<tr>\n" +
            "\t\t\t<th>\n" +
            "\t\t\t\tWorker ID\n" +
            "\t\t\t</th>\n" +
            "\t\t\t<th>\n" +
            "\t\t\t\tWorker IP\n" +
            "\t\t\t</th>\n" +
            "\t\t\t<th>\n" +
            "\t\t\t\tWorker Status\n" +
            "\t\t\t</th>\n" +
            "\t\t\t<th>\n" +
            "\t\t\t\tKeysRead\n" +
            "\t\t\t</th>\n" +
            "\t\t\t<th>\n" +
            "\t\t\t\tKeysWritten\n" +
            "\t\t\t</th>\n" +
            "\t\t\t<th>\n" +
            "\t\t\t\tSample Output\n" +
            "\t\t\t</th>\n" +
            "\t\t</tr>\n" +
            "\t\t####WorkerStatus####\n" +
            "\t</table>\n" +
            "\t\n" +
            "</body>\n" +
            "</html>";


    private Map<String, String> networkConfig = new HashMap<>();



    public Constants() {
        networkConfig.put("workerList", workerConfig);
        networkConfig.put("workerCount", String.valueOf(workersLength));
        networkConfig.put("masterHost", masterHost);
    }

    private static Constants constantInstance = null;
    public synchronized static Constants getInstance() {
        if(constantInstance == null) {
             constantInstance = new Constants();
        }
        return constantInstance;
    }


    public Map<String, String> getNetworkConfig() {
        return networkConfig;
    }

    public int getMapperCountPerNode() {
        return mapperCountPerNode;
    }

    public int getReduceCountPerNode() {
        return reduceCountPerNode;
    }

    public int getPrintCountPerNode() {
        return printCountPerNode;
    }

    public int getBlockingQueueSize() {
        return blockingQueueSize;
    }

    public String getHtmlString() {
        return htmlString;
    }
}
