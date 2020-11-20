package edu.upenn.cis455.mapreduce.master;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.Topology;
import edu.upenn.cis.stormlite.TopologyBuilder;
import edu.upenn.cis.stormlite.bolt.MapBolt;
import edu.upenn.cis.stormlite.bolt.PrintBolt;
import edu.upenn.cis.stormlite.bolt.ReduceBolt;
import edu.upenn.cis.stormlite.distributed.WorkerHelper;
import edu.upenn.cis.stormlite.distributed.WorkerJob;
import edu.upenn.cis.stormlite.spout.FileSpout;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis455.mapreduce.Constants;
import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.MasterServerContext;
import edu.upenn.cis455.mapreduce.Utils;
import edu.upenn.cis455.mapreduce.job.WordSpout;
import edu.upenn.cis455.mapreduce.pojo.CurrentWorkerStatsEnum;
import edu.upenn.cis455.mapreduce.pojo.ResultPojo;
import edu.upenn.cis455.mapreduce.restModels.WorkerStatus;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static spark.Spark.*;

public class MasterServer {

    static final long serialVersionUID = 455555001;
    static final int myPort = 8000;
    static final int TIME_OUT = 120000;

    public static MasterServer masterServerInstance;

    static final ObjectMapper om = new ObjectMapper();

    static {
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
    }

    private MasterServerContext masterServerContext;

    private String storageLocation;

    Map<String, WorkerJob> workerJobMap = new ConcurrentHashMap<>();
    Map<String, List<String>> workIpMapping = new HashMap<>();

    private Queue<String> jobQueue = new LinkedList<>();


    private static final String WORD_SPOUT = "WORD_SPOUT";
    private static final String MAP_BOLT = "MAP_BOLT";
    private static final String REDUCE_BOLT = "REDUCE_BOLT";
    private static final String PRINT_BOLT = "PRINT_BOLT";

    private boolean isJobRunning = false;

    private WorkerJob currentWorkingJob = null;

    private boolean isActive = true;

    private LinkedHashMap<String, WorkerStatus> workerStatusLinkedHashMap = new LinkedHashMap<>();

    static final ObjectMapper om2 = new ObjectMapper();


    public MasterServer(String[] args) throws MalformedURLException {
        masterServerInstance = this;
        Config networkConfig = new Config();
        storageLocation = "./store";
        networkConfig.put("master", "http://127.0.0.1:" + String.valueOf(myPort));
        URL url = new URL(networkConfig.get("master"));
        this.masterServerContext = new MasterServerContext(networkConfig);
        masterServerContext.getConfig().put("storageLocation", storageLocation);
        //WorkerServer workerServer = new WorkerServer(myPort, 0, url);
        //workerServer.isMasterWorker = true;
        port(myPort);
        MasterCleaner masterCleaner = new MasterCleaner(this);
        new Thread(masterCleaner).start();
        Utils.createDirectory(storageLocation);
        Utils.checkDirectory(storageLocation);
    }

    public void updateJobStatus(WorkerStatus workerStatus) {

        if (workerStatus.getJobId() == null) {
            return;
        }
        WorkerJob workerJob = workerJobMap.get(workerStatus.getJobId());
        int workerCountRequired = Integer.valueOf(workerJob.getConfig().get("workerCount"));
        List<String> requiredList = masterServerInstance.getWorkIpMapping().get(workerJob.getConfig().get("jobId"));
        int currentCount = 0;
        synchronized (this) {
            for (Map.Entry<String, WorkerStatus> workerStatusEntry : workerStatusLinkedHashMap.entrySet()) {
                if (workerStatus.getJobId().equals(workerStatusEntry.getValue().getJobId())
                        && workerStatusEntry.getValue().getCurrentWorkerStatsEnum() == CurrentWorkerStatsEnum.IDLE_PHASE) {
                    currentCount += 1;
                }
            }
            if (currentCount == workerCountRequired) {
                isJobRunning = false;
                masterServerInstance.currentWorkingJob = null;
            }
//            for(String:)
        }

    }

    public static WorkerJob buildJob(Config jobConfig) {
        FileSpout spout = new WordSpout();
        MapBolt bolt = new MapBolt();
        ReduceBolt bolt2 = new ReduceBolt();
        PrintBolt printer = new PrintBolt();

        TopologyBuilder builder = new TopologyBuilder();

        // Only one source ("spout") for the words
        builder.setSpout(WORD_SPOUT, spout, Integer.valueOf(jobConfig.get("spoutExecutors")));

        // Parallel mappers, each of which gets specific words
        builder.setBolt(MAP_BOLT, bolt, Integer.valueOf(jobConfig.get("mapExecutors"))).fieldsGrouping(WORD_SPOUT, new Fields("value"));

        // Parallel reducers, each of which gets specific words
        builder.setBolt(REDUCE_BOLT, bolt2, Integer.valueOf(jobConfig.get("reduceExecutors"))).fieldsGrouping(MAP_BOLT, new Fields("key"));

        // Only use the first printer bolt for reducing to a single point
        builder.setBolt(PRINT_BOLT, printer, 1).firstGrouping(REDUCE_BOLT);

        Topology topo = builder.createTopology();

        WorkerJob job = new WorkerJob(topo, jobConfig);
        return job;
    }

    private void defineJob(WorkerJob job) throws IOException {

        synchronized (this) {
            if (isJobRunning) {
                throw new RuntimeException("Currently a job is running");
            }
        }

        String[] workers = WorkerHelper.getWorkers(job.getConfig());
        job.getConfig().put("workerCount", String.valueOf(workers.length));
        job.getConfig().remove("storageLocation");
        int i = 0;
        for (String dest : workers) {
            job.getConfig().put("workerIndex", String.valueOf(i++));
            if (sendJob(dest, "POST", "definejob",
                    om.writerWithDefaultPrettyPrinter().writeValueAsString(job)).getResponseCode() !=
                    HttpURLConnection.HTTP_OK) {
                throw new RuntimeException("Job definition request failed");
            }
        }

    }

    private void shutdown() {
        synchronized (this) {
            for (String s : masterServerInstance.getWorkerStatusLinkedHashMap().keySet()) {
                try {
                    URL url = new URL(s + "/" + "shutdown");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("GET");
                    conn.getResponseCode();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            masterServerInstance.setActive(false);
        }
    }


    private void submitJob(WorkerJob job) throws IOException {

        String[] workers = WorkerHelper.getWorkers(job.getConfig());
        job.getConfig().remove("storageLocation");
        String jobString = "runjob?jobIndex=" + job.getConfig().get("jobId");

        synchronized (masterServerInstance) {
            isJobRunning = true;
            masterServerInstance.currentWorkingJob = job;
            workIpMapping.put(job.getConfig().get("jobId"), Arrays.asList(workers));
            for(String dest : workers) {
                WorkerStatus workerStatus = masterServerInstance.getWorkerStatusLinkedHashMap().get(dest);
                if(workerStatus!=null) {
                    workerStatus.setCurrentWorkerStatsEnum(CurrentWorkerStatsEnum.MAP_PHASE);
                }
            }
        }

        int i = 0;

        try {
            for (String dest : workers) {
                job.getConfig().put("workerIndex", String.valueOf(i++));

                if (sendJob(dest, "POST", jobString,
                        om.writerWithDefaultPrettyPrinter().writeValueAsString(job)).getResponseCode() !=
                        HttpURLConnection.HTTP_OK) {
                    throw new RuntimeException("Job definition request failed");
                }
            }
        } catch (RuntimeException e) {
            if (i == 1) {
                synchronized (this) {
                    isJobRunning = false;
                    masterServerInstance.currentWorkingJob = null;
                }
            }
            synchronized (this) {
                int k = 0;
                while (k < i) {
                    workIpMapping.put(job.getConfig().get("jobId"), new ArrayList<>());
                    workIpMapping.get(job.getConfig().get("jobId")).add(workers[k]);
                    k += 1;
                }
                masterServerInstance.currentWorkingJob = job;
            }

            job.getConfig().put("workerCount", String.valueOf(i - 1));
            throw new RuntimeException("Submitted partially: " + String.valueOf(i - 1));
        }

    }

    static HttpURLConnection sendJob(String dest, String reqType, String job, String parameters) throws IOException {
        URL url = new URL(dest + "/" + job);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod(reqType);

        if (reqType.equals("POST")) {
            conn.setRequestProperty("Content-Type", "application/json");
            OutputStream os = conn.getOutputStream();
            byte[] toSend = parameters.getBytes();
            os.write(toSend);
            os.flush();
        } else
            conn.getOutputStream();

        return conn;
    }

    public static List<String> extractWorkerAddress(String[] args) {

        if (args.length < 2) {
            return null;
        }
        if (args.length == 2) {
            return new ArrayList<>();
        }
        ArrayList<String> workerAddress = new ArrayList<>();
        int index = 2;
        while (index < args.length) {
            int workerPort = Integer.valueOf(args[index]);
            String url = "http://127.0.0.1:" + String.valueOf(workerPort);
            workerAddress.add(url);
            index += 1;
        }

        return workerAddress;
    }

    public static String workerList(List<String> workerList) {

        if (workerList == null) {
            return "[]";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('[');
        int i = 0;
        while (i < workerList.size()) {
            stringBuilder.append(workerList.get(i));
            if (i != workerList.size() - 1) {
                stringBuilder.append(",");
            }
            i += 1;
        }
        stringBuilder.append(']');
        return stringBuilder.toString();

    }

    public static void registerStatusPage() {
        get("/status", (request, response) -> {
            response.type("text/html");
            System.out.println(masterServerInstance.getWorkerStatusLinkedHashMap().size());
            StringBuilder stringBuilder = new StringBuilder();
            String statusString = String.valueOf(masterServerInstance.isJobRunning);
            String disableString = masterServerInstance.isJobRunning ? "disable" : "";
            StringBuilder workerStatus = new StringBuilder();
            synchronized (masterServerInstance) {
                int i = 0;
                for (Map.Entry<String, WorkerStatus> entry : masterServerInstance.workerStatusLinkedHashMap.entrySet()) {

                    StringBuilder perWorkerStatus = new StringBuilder();
                    perWorkerStatus.append("<tr>");

                    perWorkerStatus.append("<td>");
                    perWorkerStatus.append(String.valueOf(i));
                    perWorkerStatus.append("</td>");

                    perWorkerStatus.append("<td>");
                    perWorkerStatus.append(entry.getKey());
                    perWorkerStatus.append("</td>");

                    perWorkerStatus.append("<td>");
                    perWorkerStatus.append(entry.getValue().getCurrentWorkerStatsEnum().toString());
                    perWorkerStatus.append("</td>");

                    perWorkerStatus.append("<td>");
                    perWorkerStatus.append(String.valueOf(entry.getValue().getKeysRead()));
                    perWorkerStatus.append("</td>");

                    perWorkerStatus.append("<td>");
                    perWorkerStatus.append(String.valueOf(entry.getValue().getKeysWritten()));
                    perWorkerStatus.append("</td>");

                    perWorkerStatus.append("<td>");
                    perWorkerStatus.append("<table>");

                    perWorkerStatus.append("<tr>");
                    perWorkerStatus.append("<th>");
                    perWorkerStatus.append("key");
                    perWorkerStatus.append("</th>");
                    perWorkerStatus.append("<th>");
                    perWorkerStatus.append("value");
                    perWorkerStatus.append("</th>");
                    perWorkerStatus.append("</tr>");

                    for (String jsonString : entry.getValue().getResults()) {

                        ResultPojo resultPojo = om.readValue(jsonString, ResultPojo.class);
                        if (resultPojo != null) {
                            perWorkerStatus.append("<tr>");
                            perWorkerStatus.append("<td>");
                            perWorkerStatus.append(resultPojo.getKey());
                            perWorkerStatus.append("</td>");
                            perWorkerStatus.append("<td>");
                            perWorkerStatus.append(resultPojo.getValue());
                            perWorkerStatus.append("</td>");
                            perWorkerStatus.append("</tr>");
                        }
                    }
                    perWorkerStatus.append("</table>");
                    perWorkerStatus.append("</td>");
                    perWorkerStatus.append("</tr>");
                    workerStatus.append(perWorkerStatus.toString());
                    i += 1;
                }
            }

            String htmlString = Constants.getInstance().getHtmlString();
            htmlString = htmlString.replace("####status####", statusString);
            htmlString = htmlString.replace("####disabled####", disableString);
            htmlString = htmlString.replace("####WorkerStatus####", workerStatus.toString());
            return htmlString;
        });
    }

    /**
     * The mainline for launching a MapReduce Master.  This should
     * handle at least the status and workerstatus routes, and optionally
     * initialize a worker as well.
     *
     * @param args
     */
    public static void main(String[] args) throws MalformedURLException {

        MasterServer masterServer = new MasterServer(args);

        //port(myPort);
        System.out.println("Master node startup");
        registerStatusPage();

        get("/shutdown", (request, response) -> {
            masterServerInstance.shutdown();
            return "ok";
        });

        get("/workerstatus", (request, response) -> {

            String workerIp = request.ip();

            if (request.queryParams("port") == null || request.queryParams("status") == null
                    || request.queryParams("keysRead") == null || request.queryParams("keysWritten") == null) {
                halt(400, "no port");

            }
            int port = Integer.valueOf(request.queryParams("port"));
            CurrentWorkerStatsEnum currentWorkerStatsEnum = CurrentWorkerStatsEnum.valueOf(request.queryParams("status"));
            int keysRead = Integer.valueOf(request.queryParams("keysRead"));
            int keysWritten = Integer.valueOf(request.queryParams("keysWritten"));
            List<String> arrayList = new ArrayList<>();
            String jobId = request.queryParams("jobId");

            if (request.queryParamsValues("results") != null) {
                String[] values = request.queryParamsValues("results");
                arrayList = Arrays.asList(values);
            }
            request.queryParamsValues("results");
            WorkerStatus workerStatus = new WorkerStatus("");
            workerStatus.setKeysRead(keysRead);
            workerStatus.setKeysWritten(keysWritten);
            workerStatus.setPort(port);
            workerStatus.setRequestDate(new Date());
            workerStatus.setResults(arrayList);
            workerStatus.setCurrentWorkerStatsEnum(currentWorkerStatsEnum);
            if(currentWorkerStatsEnum == CurrentWorkerStatsEnum.PRINT_PHASE) {
                workerStatus.setCurrentWorkerStatsEnum(CurrentWorkerStatsEnum.IDLE_PHASE);
            }
            workerStatus.setJobId(jobId);
            String workerUrl = "http://" + workerIp + ":" + String.valueOf(port);
            masterServerInstance.getWorkerStatusLinkedHashMap().put(workerUrl, workerStatus);
            //masterServerInstance.updateJobStatus(workerStatus);
            System.out.println("status update " + workerUrl);
            return "";
        });

        post("/defineJob", ((request, response) -> {
            String jobId = UUID.randomUUID().toString();
            String inputDirectory = request.queryParams("inputDirectory");
            String outputDirectory = request.queryParams("outputDirectory");
            String jobClass = request.queryParams("jobClassName");
            String jobname = request.queryParams("jobname");
            int mapExecutors = Integer.valueOf(request.queryParams("mapExecutors"));
            int reduceExecutors = Integer.valueOf(request.queryParams("reduceExecutors"));
            Job tempJob = (Job) Class.forName(jobClass).newInstance();
            if (tempJob == null) {
                halt(400, "Class not found");
            }
            Config jobConfig = new Config();
            Utils.addMap(jobConfig, masterServer.masterServerContext.getConfig());
            jobConfig.put("mapClass", jobClass);
            jobConfig.put("reduceClass", jobClass);
            jobConfig.put("spoutExecutors", "1");
            jobConfig.put("mapExecutors", String.valueOf(mapExecutors));
            jobConfig.put("reduceExecutors", String.valueOf(reduceExecutors));
            jobConfig.put("printExecutors", "1");
            jobConfig.put("inputDirectory", inputDirectory);
            jobConfig.put("outputDirectory", outputDirectory);
            jobConfig.put("job", jobname);
            jobConfig.put("workerList", workerList(masterServer.getArrayList()));
            jobConfig.put("jobId", jobId);
            WorkerJob job = buildJob(jobConfig);
            masterServer.defineJob(job);
            masterServer.getWorkerJobMap().put(jobId, job);
//            request.session().attribute("job", job);

            // start the job
            masterServer.submitJob(job);
            return "OK";
        }));


    }

    public List<String> getArrayList() {
        List<String> workers = new ArrayList<>();
        List<String> expiredWorkers = new ArrayList<>();
        Set s;
        synchronized (this) {
            s = workerStatusLinkedHashMap.entrySet();
            Iterator i = s.iterator();
            Date currentDate = new Date();
            while (i.hasNext()) {
                Map.Entry<String, WorkerStatus> me = (Map.Entry) i.next();
                WorkerStatus workerStatus = me.getValue();
                String key = me.getKey();
                if (currentDate.getTime() - workerStatus.getRequestDate().getTime() < TIME_OUT) {
                    workers.add(key);
                } else {
                    expiredWorkers.add(key);
                }
            }
            for (String temp : expiredWorkers) {
                workerStatusLinkedHashMap.remove(temp);
            }
        }

        return workers;

    }

    public LinkedHashMap<String, WorkerStatus> getWorkerStatusLinkedHashMap() {
        return workerStatusLinkedHashMap;
    }

    public void setWorkerStatusLinkedHashMap(LinkedHashMap<String, WorkerStatus> workerStatusLinkedHashMap) {
        this.workerStatusLinkedHashMap = workerStatusLinkedHashMap;
    }

    public Map<String, WorkerJob> getWorkerJobMap() {
        return workerJobMap;
    }

    public void setWorkerJobMap(Map<String, WorkerJob> workerJobMap) {
        this.workerJobMap = workerJobMap;
    }

    public Map<String, List<String>> getWorkIpMapping() {
        return workIpMapping;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public static int getMyPort() {
        return myPort;
    }

    public static int getTimeOut() {
        return TIME_OUT;
    }

    public static MasterServer getMasterServerInstance() {
        return masterServerInstance;
    }

    public static ObjectMapper getOm() {
        return om;
    }

    public MasterServerContext getMasterServerContext() {
        return masterServerContext;
    }

    public String getStorageLocation() {
        return storageLocation;
    }

    public Queue<String> getJobQueue() {
        return jobQueue;
    }

    public static String getWordSpout() {
        return WORD_SPOUT;
    }

    public static String getMapBolt() {
        return MAP_BOLT;
    }

    public static String getReduceBolt() {
        return REDUCE_BOLT;
    }

    public static String getPrintBolt() {
        return PRINT_BOLT;
    }

    public boolean isJobRunning() {
        return isJobRunning;
    }

    public WorkerJob getCurrentWorkingJob() {
        return currentWorkingJob;
    }

    public boolean isActive() {
        return isActive;
    }

    public static ObjectMapper getOm2() {
        return om2;
    }

    public static void setMasterServerInstance(MasterServer masterServerInstance) {
        MasterServer.masterServerInstance = masterServerInstance;
    }

    public void setMasterServerContext(MasterServerContext masterServerContext) {
        this.masterServerContext = masterServerContext;
    }

    public void setStorageLocation(String storageLocation) {
        this.storageLocation = storageLocation;
    }

    public void setWorkIpMapping(Map<String, List<String>> workIpMapping) {
        this.workIpMapping = workIpMapping;
    }

    public void setJobQueue(Queue<String> jobQueue) {
        this.jobQueue = jobQueue;
    }

    public void setJobRunning(boolean jobRunning) {
        isJobRunning = jobRunning;
    }

    public void setCurrentWorkingJob(WorkerJob currentWorkingJob) {
        this.currentWorkingJob = currentWorkingJob;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
  
