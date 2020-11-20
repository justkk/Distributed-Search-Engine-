package edu.upenn.cis.stormlite.bolt;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.distributed.ConsensusTracker;
import edu.upenn.cis.stormlite.routers.StreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis455.mapreduce.Job;
import edu.upenn.cis455.mapreduce.worker.WorkerServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * A simple adapter that takes a MapReduce "Job" and calls the "reduce"
 * on a per-tuple basis
 */

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public class PrintBolt implements IRichBolt {
    static Logger log = LogManager.getLogger(ReduceBolt.class);


    Job reduceJob;

    /**
     * This object can help determine when we have
     * reached enough votes for EOS
     */
    ConsensusTracker votesForEos;

    /**
     * To make it easier to debug: we have a unique ID for each
     * instance of the WordCounter, aka each "executor"
     */
    String executorId = UUID.randomUUID().toString();

    Fields schema = new Fields("key", "value");

    boolean sentEos = false;

    /**
     * Buffer for state, by key
     */
    Map<String, List<String>> stateByKey = new HashMap<>();

    /**
     * This is where we send our output stream
     */
    private OutputCollector collector;

    private TopologyContext context;

    int neededVotesToComplete = 0;
    int currentVoteCount = 0;

    Set<String> votedEos = new HashSet<>();

    private WorkerServer workerServer;

    BufferedWriter output = null;

    public PrintBolt() {
        workerServer = WorkerServer.currentMachineWorker;
    }

    /**
     * Initialization, just saves the output stream destination
     */
    @Override
    public void prepare(Map<String, String> stormConf,
                        TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.context = context;

        if (!stormConf.containsKey("reduceExecutors")) {
            throw new RuntimeException("Print class doesn't know how many reduce bolt executors");
        }

        // you want to handle consensus)

        int n = Integer.valueOf(stormConf.get("workerCount"));
        int k = Integer.valueOf(stormConf.get("mapExecutors"));
        int l = Integer.valueOf(stormConf.get("reduceExecutors"));

        //int eosVoteCount = (n - 1) * k * l + k;

        int eosVoteCount = n * k;
        neededVotesToComplete = eosVoteCount;
        votesForEos = new ConsensusTracker(eosVoteCount);

        String fileName = stormConf.get("outputPath");
        try {
            File file = new File(fileName);
            output = new BufferedWriter(new FileWriter(file));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Process a tuple received from the stream, buffering by key
     * until we hit end of stream
     */
    @Override
    public synchronized boolean execute(Tuple input) {
        if (sentEos) {
            if (!input.isEndOfStream())
                throw new RuntimeException("We received data after we thought the stream had ended!");
            return false;
        } else if (input.isEndOfStream()) {
            try {
                if (votedEos.contains(input.getSourceExecutor())) {
                    return true;
                }

                votedEos.add(input.getSourceExecutor());

                if (votesForEos.voteForEos(input.getSourceExecutor())) {
                    synchronized (workerServer) {
                        workerServer.getCurrentWorkerStats().printDone();
                    }
                    try {
                        output.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

            try {
                String outputString = input.getObjectByField("key").toString() + " : " + input.getObjectByField("value").toString();
                System.out.println(outputString);

                if (output != null) {
                    try {
                        output.write(outputString);
                        output.write("\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return true;
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
     * Used for debug purposes, shows our exeuctor/operator's unique ID
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
        this.collector.setRouter(router);
    }

    /**
     * The fields (schema) of our output stream
     */
    @Override
    public Fields getSchema() {
        return schema;
    }

}

