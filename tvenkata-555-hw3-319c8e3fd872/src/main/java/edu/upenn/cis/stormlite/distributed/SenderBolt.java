package edu.upenn.cis.stormlite.distributed;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.StreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.UUID;

/**
 * This is a virtual bolt that is used to route data to the WorkerServer
 * on a different worker.
 *
 * @author zives
 */
public class SenderBolt implements IRichBolt {

    static Logger log = LogManager.getLogger(SenderBolt.class);

    /**
     * To make it easier to debug: we have a unique ID for each
     * instance of the WordCounter, aka each "executor"
     */
    String executorId = UUID.randomUUID().toString();

    Fields schema = new Fields("key", "value");

    String stream;
    String address;
    ObjectMapper mapper = new ObjectMapper();
    URL url;

    TopologyContext context;

    boolean isEndOfStream = false;


    public SenderBolt(String address, String stream) {
        this.stream = stream;
        this.address = address;
    }

    /**
     * Initialization, just saves the output stream destination
     */
    @Override
    public void prepare(Map<String, String> stormConf,
                        TopologyContext context, OutputCollector collector) {
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        this.context = context;
        try {
            url = new URL(address + "/pushdata/" + stream);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            throw new RuntimeException("Unable to create remote URL");
        }
    }

    /**
     * Process a tuple received from the stream, incrementing our
     * counter and outputting a result
     */
    @Override
    public synchronized boolean execute(Tuple input) {
        try {
            send(input);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    /**
     * Sends the data along a socket
     *
     * @param
     * @param tuple
     * @throws IOException
     */
    private void send(Tuple tuple) throws IOException {

        isEndOfStream = tuple.isEndOfStream();

        log.info("Sender is routing " + tuple.toString() + " from " + tuple.getSourceExecutor() + " to " + address + "/" + stream);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/json");
        String jsonForTuple = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(tuple);


        // TODO: send this to /pushdata/{stream} as a POST!

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        OutputStream os = conn.getOutputStream();
        byte[] toSend = jsonForTuple.getBytes();
        os.write(toSend);
        os.flush();
        ///////////
        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        for (int c; (c = in.read()) >= 0;)
            System.out.print((char)c);

        conn.disconnect();
        log.info(url.toString());
        log.info(jsonForTuple);
        log.info("Sent Info Through Connection");
    }

    /**
     * Shutdown, just frees memory
     */
    @Override
    public void cleanup() {
    }

    /**
     * Lets the downstream operators know our schema
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(schema);
    }

    /**
     * Used for debug purposes, shows our executor/operator's unique ID
     */
    @Override
    public String getExecutorId() {
        return executorId;
    }

    /**
     * Called during topology setup, sets the router to the next
     * bolt
     */
    @Override
    public void setRouter(StreamRouter router) {
        // NOP for this, since its destination is a socket
    }

    /**
     * The fields (schema) of our output stream
     */
    @Override
    public Fields getSchema() {
        return schema;
    }

    public static void main(String[] args) throws IOException {

        URL url = new URL("http://127.0.0.1:8002/pushdata/MAP_BOLT");

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Content-Type", "application/json");
        String jsonForTuple = "[ \"edu.upenn.cis.stormlite.tuple.Tuple\", {\n" +
                "  \"fields\" : [ \"edu.upenn.cis.stormlite.tuple.Fields\", [ \"key\", \"value\" ] ],\n" +
                "  \"values\" : [ \"edu.upenn.cis.stormlite.tuple.Values\", [ \"12\", \"barked\" ] ],\n" +
                "  \"sourceExecutor\" : \"cb883c40-f20c-4fcf-a32c-616e477f1722\",\n" +
                "  \"endOfStream\" : false\n" +
                "} ]";


        // TODO: send this to /pushdata/{stream} as a POST!

        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        OutputStream os = conn.getOutputStream();
        byte[] toSend = jsonForTuple.getBytes();
        os.write(toSend);
        os.flush();
        ///////////
        //conn.disconnect();



    }
}
